package com.github.secretx33.codebench

import java.net.NetworkInterface
import java.security.SecureRandom
import java.time.Instant

/**
 * Distributed Sequence Generator.
 * Inspired by Twitter snowflake: https://github.com/twitter/snowflake/tree/snowflake-2010
 *
 * This class should be used as a Singleton.
 * Make sure that you create and reuse a Single instance of Snowflake per node in your distributed system cluster.
 */
class Snowflake {

    private val nodeId: Long
    private val customEpoch: Long
    @Volatile
    private var lastTimestamp = -1L
    @Volatile
    private var sequence = 0L

    // Create Snowflake with a nodeId and custom epoch
    @JvmOverloads
    constructor(nodeId: Long, customEpoch: Long = DEFAULT_CUSTOM_EPOCH) {
        require(nodeId !in 0..MAX_NODE_ID) { "NodeId must be between 0 and $MAX_NODE_ID" }
        this.nodeId = nodeId
        this.customEpoch = customEpoch
    }

    // Let Snowflake generate a nodeId
    constructor() {
        nodeId = createNodeId()
        customEpoch = DEFAULT_CUSTOM_EPOCH
    }

    @Synchronized
    fun nextLong(): Long {
        var currentTimestamp = timestamp()
        check(currentTimestamp >= lastTimestamp) { "Invalid System Clock!" }

        if (currentTimestamp == lastTimestamp) {
            sequence = sequence + 1 and MAX_SEQUENCE
            if (sequence == 0L) {
                // Sequence Exhausted, wait till next millisecond.
                currentTimestamp = waitNextMillis(currentTimestamp)
            }
        } else {
            // Reset sequence to start with zero for the next millisecond
            sequence = 0
        }

        lastTimestamp = currentTimestamp

        return (currentTimestamp shl NODE_ID_BITS + SEQUENCE_BITS or (nodeId shl SEQUENCE_BITS) or sequence)
    }

    // Get current timestamp in milliseconds, adjust for the custom epoch.
    private fun timestamp(): Long = Instant.now().toEpochMilli() - customEpoch

    // Block and wait till next millisecond
    private fun waitNextMillis(currentTimestamp: Long): Long {
        var currentTimestamp = currentTimestamp
        while (currentTimestamp == lastTimestamp) {
            currentTimestamp = timestamp()
        }
        return currentTimestamp
    }

    private fun createNodeId(): Long {
        var nodeId = try {
            val sb = StringBuilder()
            val networkInterfaces = NetworkInterface.getNetworkInterfaces()
            networkInterfaces.iterator().forEach {
                val mac = it.hardwareAddress ?: byteArrayOf()
                for (macPort in mac) {
                    sb.append(String.format("%02X", macPort))
                }
            }
            sb.toString().hashCode().toLong()
        } catch (e: Exception) {
            SecureRandom().nextInt().toLong()
        }

        // Limit 'nodeId' to a maximum of 'MAX_NODE_ID' without breaking the randomness
        nodeId = nodeId and MAX_NODE_ID
        return nodeId
    }

    fun parse(id: Long): LongArray {
        val maskNodeId = (1L shl NODE_ID_BITS) - 1 shl SEQUENCE_BITS
        val maskSequence = (1L shl SEQUENCE_BITS) - 1
        val timestamp = (id shr NODE_ID_BITS + SEQUENCE_BITS) + customEpoch
        val nodeId = id and maskNodeId shr SEQUENCE_BITS
        val sequence = id and maskSequence
        return longArrayOf(timestamp, nodeId, sequence)
    }

    override fun toString(): String = "Snowflake Settings [EPOCH_BITS=$EPOCH_BITS, NODE_ID_BITS=$NODE_ID_BITS, SEQUENCE_BITS=$SEQUENCE_BITS, CUSTOM_EPOCH=$customEpoch, NodeId=$nodeId]"

    private companion object {
        const val UNUSED_BITS = 1 // Sign bit, Unused (always set to 0)
        const val EPOCH_BITS = 41
        const val NODE_ID_BITS = 10
        const val SEQUENCE_BITS = 12
        const val MAX_NODE_ID = (1L shl NODE_ID_BITS) - 1
        const val MAX_SEQUENCE = (1L shl SEQUENCE_BITS) - 1

        // Custom Epoch (January 1, 2015 Midnight UTC = 2015-01-01T00:00:00Z)
        const val DEFAULT_CUSTOM_EPOCH = 1420070400000L
    }
}

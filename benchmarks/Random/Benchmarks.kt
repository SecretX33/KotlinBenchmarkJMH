package com.github.secretx33.codebench

import it.unimi.dsi.util.XoShiRo256PlusPlusRandom
import it.unimi.dsi.util.XoShiRo256PlusPlusRandomGenerator
import it.unimi.dsi.util.XoShiRo256PlusRandom
import it.unimi.dsi.util.XoShiRo256PlusRandomGenerator
import it.unimi.dsi.util.XoShiRo256StarStarRandom
import it.unimi.dsi.util.XoShiRo256StarStarRandomGenerator
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Warmup
import org.openjdk.jmh.infra.Blackhole
import java.security.SecureRandom
import java.util.Random
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit

/**
 * Here is where all the action happens. Annotate your methods with [@Benchmark][Benchmark] to make them appear
 * on your benchmark results.
 *
 * Remember to always either return the result of the operation or blackhole it using a [Blackhole] so the JIT
 * compiler won't remove your code and make you benchmark an empty method instead ;).
 *
 * The test can be tweaked to your likings and benchmark case, but these are good start points for anybody
 * that has just got into benchmarking and know nothing about it.
 */
@Fork(1, jvmArgsAppend = ["-XX:+UseG1GC", "-XX:+AlwaysPreTouch", "-Xms4G", "-Xmx4G"])
@Warmup(iterations = 15, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 15, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
class Benchmarks {

    @Benchmark
    fun localRandom(holder: ConfigHolder) = holder.threadLocalRandom.nextBytes(holder.buffer)

    @Benchmark
    fun javaRandom(holder: ConfigHolder) = holder.javaRandom.nextBytes(holder.buffer)

    @Benchmark
    fun kotlinRandom(holder: ConfigHolder) = holder.kotlinRandom.nextBytes(holder.buffer)

    @Benchmark
    fun secureRandom(holder: ConfigHolder) = holder.secureRandom.nextBytes(holder.buffer)

    @Benchmark
    fun plus(holder: ConfigHolder) = holder.plus.nextBytes(holder.buffer)

    @Benchmark
    fun plusGen(holder: ConfigHolder) = holder.plusGen.nextBytes(holder.buffer)

    @Benchmark
    fun plusPlus(holder: ConfigHolder) = holder.plusPlus.nextBytes(holder.buffer)

    @Benchmark
    fun plusPlusGen(holder: ConfigHolder) = holder.plusPlusGen.nextBytes(holder.buffer)

    @Benchmark
    fun starStar(holder: ConfigHolder) = holder.starStar.nextBytes(holder.buffer)

    @Benchmark
    fun starStarGen(holder: ConfigHolder) = holder.starStarGen.nextBytes(holder.buffer)

    @State(Scope.Thread)
    open class ConfigHolder {

        lateinit var threadLocalRandom: Random
        lateinit var javaRandom: Random
        lateinit var kotlinRandom: kotlin.random.Random
        lateinit var secureRandom: SecureRandom
        lateinit var plus: XoShiRo256PlusRandom
        lateinit var plusGen: XoShiRo256PlusRandomGenerator
        lateinit var plusPlus: XoShiRo256PlusPlusRandom
        lateinit var plusPlusGen: XoShiRo256PlusPlusRandomGenerator
        lateinit var starStar: XoShiRo256StarStarRandom
        lateinit var starStarGen: XoShiRo256StarStarRandomGenerator
        lateinit var buffer: ByteArray

        @Setup(Level.Trial)
        fun shuffleConfig() {
            threadLocalRandom = ThreadLocalRandom.current()
            javaRandom = Random()
            kotlinRandom = kotlin.random.Random.Default
            secureRandom = SecureRandom()
            plus = XoShiRo256PlusRandom()
            plusGen = XoShiRo256PlusRandomGenerator()
            plusPlus = XoShiRo256PlusPlusRandom()
            plusPlusGen = XoShiRo256PlusPlusRandomGenerator()
            starStar = XoShiRo256StarStarRandom()
            starStarGen = XoShiRo256StarStarRandomGenerator()
            buffer = ByteArray(64)
        }

    }

}

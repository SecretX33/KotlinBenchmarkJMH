package com.github.secretx33.codebench

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
@Warmup(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 30, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
class Benchmarks {

    @Benchmark
    fun multiplicationArithmetic(holder: ConfigHolder): Int =
        holder.multiplicationX * holder.randomArithmeticValue()

    @Benchmark
    fun multiplicationBitshift(holder: ConfigHolder) =
        holder.multiplicationX shl holder.randomBitshiftValue()

    @Benchmark
    fun divisionArithmetic(holder: ConfigHolder): Int =
        holder.divisionX / holder.randomArithmeticValue()

    @Benchmark
    fun divisionBitshift(holder: ConfigHolder) =
        holder.divisionX ushr holder.randomBitshiftValue()

    @Benchmark
    fun moduleArithmetic(holder: ConfigHolder): Int =
        holder.moduleX % holder.randomModuleValue()

    @Benchmark
    fun moduleBitshift(holder: ConfigHolder) =
        holder.moduleX and (holder.randomModuleValue() - 1)

    @State(Scope.Thread)
    open class ConfigHolder {

        lateinit var random: Random

        /**
         * All these are prime numbers.
         */
        val multiplicationX = 1021
        val divisionX = 16369
        val moduleX = 1031

        @Setup(Level.Trial)
        fun setup() {
            random = Random(1337L)
        }

        fun randomBitshiftValue(): Int = random.nextInt(1, 11)

        fun randomArithmeticValue(): Int = random.nextInt(1, 1025)

        fun randomModuleValue(): Int = random.nextInt(3, 350)

    }

}

package com.rendly.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Startup Benchmark for Rendly App
 * 
 * Measures cold, warm, and hot startup times with different compilation modes.
 * Run with: ./gradlew :benchmark:connectedBenchmarkAndroidTest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class StartupBenchmark {

    @get:Rule
    val rule = MacrobenchmarkRule()

    /**
     * Cold start with no pre-compilation (worst case scenario)
     */
    @Test
    fun startupColdNoCompilation() = startup(
        compilationMode = CompilationMode.None(),
        startupMode = StartupMode.COLD
    )

    /**
     * Cold start with Baseline Profile (target scenario)
     */
    @Test
    fun startupColdBaselineProfile() = startup(
        compilationMode = CompilationMode.Partial(),
        startupMode = StartupMode.COLD
    )

    /**
     * Cold start with full AOT compilation (best possible scenario)
     */
    @Test
    fun startupColdFullCompilation() = startup(
        compilationMode = CompilationMode.Full(),
        startupMode = StartupMode.COLD
    )

    /**
     * Warm start (app process exists but activity destroyed)
     */
    @Test
    fun startupWarm() = startup(
        compilationMode = CompilationMode.Partial(),
        startupMode = StartupMode.WARM
    )

    /**
     * Hot start (activity just stopped)
     */
    @Test
    fun startupHot() = startup(
        compilationMode = CompilationMode.Partial(),
        startupMode = StartupMode.HOT
    )

    private fun startup(
        compilationMode: CompilationMode,
        startupMode: StartupMode
    ) {
        rule.measureRepeated(
            packageName = "com.rendly.app",
            metrics = listOf(StartupTimingMetric()),
            compilationMode = compilationMode,
            startupMode = startupMode,
            iterations = 5
        ) {
            pressHome()
            startActivityAndWait()
            
            // Wait for app to be fully interactive
            device.waitForIdle()
        }
    }
}

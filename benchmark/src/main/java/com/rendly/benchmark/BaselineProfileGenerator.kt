package com.rendly.benchmark

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Baseline Profile Generator for Rendly App
 * 
 * This generates a baseline profile that pre-compiles critical code paths,
 * significantly reducing cold start time by avoiding JIT compilation.
 * 
 * Run with: ./gradlew :benchmark:pixel6Api31BenchmarkAndroidTest -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.enabledRules=BaselineProfile
 * Or connect a physical device and run: ./gradlew :app:generateBaselineProfile
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generateBaselineProfile() {
        rule.collect(
            packageName = "com.rendly.app",
            maxIterations = 5,
            stableIterations = 3
        ) {
            // Cold start - critical path
            pressHome()
            startActivityAndWait()
            
            // Wait for app to fully load
            device.waitForIdle()
            Thread.sleep(2000)
            
            // Scroll feed (if visible) to warm up Compose lazy lists
            scrollFeedIfVisible()
            
            // Navigate to common screens to pre-compile their code
            navigateToCommonScreens()
        }
    }
    
    private fun MacrobenchmarkScope.scrollFeedIfVisible() {
        try {
            val feedList = device.findObject(
                androidx.test.uiautomator.By.scrollable(true)
            )
            feedList?.let {
                it.scroll(androidx.test.uiautomator.Direction.DOWN, 0.5f)
                Thread.sleep(500)
                it.scroll(androidx.test.uiautomator.Direction.UP, 0.5f)
            }
        } catch (_: Exception) {
            // Feed not visible, continue
        }
    }
    
    private fun MacrobenchmarkScope.navigateToCommonScreens() {
        // Simulate basic user journeys to warm up common code paths
        device.waitForIdle()
        
        // These interactions help compile commonly used Compose components
        try {
            // Try to find and click common navigation elements
            val homeButton = device.findObject(
                androidx.test.uiautomator.By.desc("Home")
            )
            homeButton?.click()
            device.waitForIdle()
            
            Thread.sleep(1000)
        } catch (_: Exception) {
            // Navigation element not found, continue
        }
    }
}

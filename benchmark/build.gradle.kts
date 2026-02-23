import com.android.build.api.dsl.ManagedVirtualDevice
import groovy.json.JsonSlurper

plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.androidx.baselineprofile)
}

android {
    namespace = "com.example.islam.benchmark"
    compileSdk = 35

    defaultConfig {
        minSdk = 28
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["androidx.benchmark.suppressErrors"] =
            "EMULATOR,LOW-BATTERY"
        testInstrumentationRunnerArguments["clearPackageData"] = "true"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    targetProjectPath = ":app"
    experimentalProperties["android.experimental.self-instrumenting"] = true

    testOptions {
        managedDevices {
            allDevices {
                create<ManagedVirtualDevice>("pixel6Api31") {
                    device = "Pixel 6"
                    apiLevel = 31
                    systemImageSource = "aosp"
                }
            }
        }
    }
}

baselineProfile {
    useConnectedDevices = true
    managedDevices += "pixel6Api31"
}

dependencies {
    implementation(libs.androidx.test.runner)
    implementation(libs.androidx.junit)
    implementation(libs.androidx.benchmark.macro.junit4)
    implementation(libs.androidx.uiautomator)
}

fun collectMetricMedians(node: Any?, metricKey: String, output: MutableList<Double>) {
    when (node) {
        is Map<*, *> -> {
            val metricNode = node[metricKey]
            if (metricNode is Map<*, *>) {
                val median = (metricNode["median"] as? Number)?.toDouble()
                if (median != null) output += median
            }
            node.values.forEach { collectMetricMedians(it, metricKey, output) }
        }

        is Iterable<*> -> node.forEach { collectMetricMedians(it, metricKey, output) }
        is Array<*> -> node.forEach { collectMetricMedians(it, metricKey, output) }
    }
}

tasks.register("verifyReleasePerformance") {
    group = "verification"
    description = "Macrobenchmark çıktısını eşiklerle doğrular."
    dependsOn("connectedNonMinifiedReleaseAndroidTest")

    doLast {
        val startupThresholdMs = (findProperty("perf.startup.max.median.ms") as String?)
            ?.toDoubleOrNull() ?: 1200.0
        val frameThresholdMs = (findProperty("perf.frame.max.median.ms") as String?)
            ?.toDoubleOrNull() ?: 16.0

        val resultFiles = fileTree(layout.buildDirectory.get().asFile) {
            include("**/benchmarkData*.json")
            include("**/*benchmark*.json")
        }.files

        if (resultFiles.isEmpty()) {
            throw GradleException(
                "Benchmark sonucu bulunamadı. connected benchmark testleri tamamlanmadı."
            )
        }

        val jsonSlurper = JsonSlurper()
        val startupMedians = mutableListOf<Double>()
        val frameMedians = mutableListOf<Double>()

        resultFiles.forEach { file ->
            runCatching { jsonSlurper.parse(file) }.onSuccess { parsed ->
                collectMetricMedians(parsed, "startupMs", startupMedians)
                collectMetricMedians(parsed, "frameOverrunMs", frameMedians)
            }
        }

        if (startupMedians.isEmpty()) {
            throw GradleException(
                "startupMs metriği bulunamadı. StartupBenchmark test çıktısını kontrol edin."
            )
        }

        val worstStartupMedian = startupMedians.maxOrNull() ?: Double.MAX_VALUE
        if (worstStartupMedian > startupThresholdMs) {
            throw GradleException(
                "Startup gate FAILED: median=${"%.1f".format(worstStartupMedian)}ms > " +
                    "threshold=${"%.1f".format(startupThresholdMs)}ms"
            )
        }

        if (frameMedians.isNotEmpty()) {
            val worstFrameMedian = frameMedians.maxOrNull() ?: Double.MAX_VALUE
            if (worstFrameMedian > frameThresholdMs) {
                throw GradleException(
                    "Frame gate FAILED: median=${"%.1f".format(worstFrameMedian)}ms > " +
                        "threshold=${"%.1f".format(frameThresholdMs)}ms"
                )
            }
        }

        println(
            "Release performance gate PASSED: startupMedian=" +
                "${"%.1f".format(worstStartupMedian)}ms"
        )
    }
}

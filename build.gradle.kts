plugins {
    kotlin("multiplatform") version "1.6.0"
}

group = "com.martmists"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    nativeTarget.apply {
        val main by compilations.getting
        val python by main.cinterops.creating { }
        val bs2b by main.cinterops.creating { }
        val freeverb by main.cinterops.creating { }

        binaries {
            staticLib {
                binaryOptions["memoryModel"] = "experimental"
            }
        }
    }
}

val cinteropBs2bNative by tasks.getting {
    dependsOn("libraries:bs2b:build")
}

val cinteropFreeverbNative by tasks.getting {
    dependsOn("libraries:freeverb:build")
}

val compileKotlinNative by tasks.getting {
    dependsOn("cinteropBs2bNative")
    dependsOn("cinteropFreeverbNative")
    dependsOn("cinteropPythonNative")
}

val install by tasks.register<Exec>("install") {
    commandLine = listOf("pip3", "install", "-U", ".")
}

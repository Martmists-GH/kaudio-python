import com.martmists.kpy.plugin.PythonVersion

plugins {
    kotlin("multiplatform") version "1.7.0"
    id("com.martmists.kpy.kpy-plugin") version "0.3.7-1.7.0"
}

version = "0.0.1"

kotlin {
    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    sourceSets {
        val main by creating {

        }

        val nativeMain by getting {

        }
    }

    nativeTarget.apply {
        val main by compilations.getting {
            cinterops {
                val bs2b by creating { }
                val freeverb by creating { }
                val utilities by creating { }
            }

            afterEvaluate {
                cinterops.all {
                    tasks.named("cinterop${name.capitalize()}Native") {
                        dependsOn(":libraries:${this@all.name}:build")
                    }
                }
            }
        }

        binaries {
            staticLib {
                binaryOptions["memoryModel"] = "experimental"
            }
        }
    }
}

allprojects {
    buildDir = file("${rootProject.rootDir.absolutePath}/build/${project.name}")
}

kpy {
    pyVersion = PythonVersion.Py310
    generateStubs = true
}

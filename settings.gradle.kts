rootProject.name = "kaudio"

buildscript {
    repositories {
        mavenCentral()
        maven("https://maven.martmists.com/releases")
    }
    dependencies {
        classpath("com.martmists.commons:commons-gradle:1.0.1")
    }
}

include("libraries:bs2b")
include("libraries:freeverb")
include("libraries:utilities")

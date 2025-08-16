plugins {
    java
    kotlin("jvm") version "2.2.10"
}

group = "com.bindglam.konlpj"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks {
    test {
        useJUnitPlatform()

        testLogging {
            showStandardStreams = true
        }
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

kotlin {
    jvmToolchain(21)
}
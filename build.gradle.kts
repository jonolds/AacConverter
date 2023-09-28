plugins {
    kotlin("jvm") version "1.9.20-Beta2"
    application
}

group = "com.jonolds"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {

    testImplementation(kotlin("test","1.9.20-Beta2"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("com.jonolds.AacKt")
}
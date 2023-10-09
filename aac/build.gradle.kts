plugins {
    alias(libs.plugins.kotlin.jvm)
    application
    alias(libs.plugins.shadow)
}

group = "com.jonolds"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.coroutines)
    implementation(libs.bundles.kotlin.main)
}


kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
}



application {
    mainClass.set("com.jonolds.AacKt")
}


configure(listOf(tasks.distTar, tasks.shadowDistTar, tasks.distZip, tasks.shadowDistZip)) {
    get().enabled = false
}
tasks.jar.configure {

    manifest {
        attributes(mapOf("Manifest-Version" to "1.0",
            "Main-Class" to application.mainClass.get()))
    }
}

tasks.shadowJar.configure {
    archiveFileName = "${application.mainClass.get().removePrefix("com.jonolds.").removeSuffix("Kt")}.jar"
    archiveClassifier = ""
    destinationDirectory = file("$rootDir/bin")
}
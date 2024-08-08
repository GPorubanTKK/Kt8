plugins {
    kotlin("jvm") version "2.0.0"
}

group = "com.rld"
version = "1.6"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(20)
}
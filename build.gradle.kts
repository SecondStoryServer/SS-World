import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.70"
}

group = "me.syari.ss.world"

repositories {
    mavenCentral()
    maven ("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    implementation("me.syari.ss:core")
    implementation("com.destroystokyo.paper:paper-api:1.15.2-R0.1-SNAPSHOT")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

val jar by tasks.getting(Jar::class) {
    from(configurations.compile.get().map { if (it.isDirectory) it else zipTree(it) })
    exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
}
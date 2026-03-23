plugins {
    java
    application
}

group = "com.aistocks"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("info.picocli:picocli:4.7.6")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

application {
    mainClass.set("com.aistocks.AiStocksApplication")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.named<Jar>("jar") {
    manifest {
        attributes["Main-Class"] = "com.aistocks.AiStocksApplication"
    }
}

tasks.register<Jar>("fatJar") {
    group = "build"
    description = "Runnable JAR with dependencies"
    archiveClassifier.set("all")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
    manifest {
        attributes["Main-Class"] = "com.aistocks.AiStocksApplication"
    }
}

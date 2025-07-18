import org.codehaus.groovy.runtime.DefaultGroovyMethods.mixin

plugins {
    id("java")
    id("architectury-plugin") version("3.4-SNAPSHOT")
    id("dev.architectury.loom") version("1.7-SNAPSHOT")

    kotlin("jvm") version "1.9.23"
}

group = "dev.elrol.arrow"
version = "1.7.0"

architectury {
    platformSetupLoomIde()
    fabric()
}

loom {
    silentMojangMappingsLicense()

    mixin {
        defaultRefmapName.set("mixins.${project.name}.refmap.json")
    }
}

repositories {
    mavenCentral()
    maven(url = "https://maven.nucleoid.xyz")
    maven(url = "https://maven.tomalbrc.de")
    maven(url = "https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/")
    maven(url = "https://maven.impactdev.net/repository/development/")
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    maven(url = "https://maven.nucleoid.xyz/") { name = "Nucleoid" }
}

dependencies {
    minecraft ("com.mojang:minecraft:1.21.1")
    mappings ("net.fabricmc:yarn:1.21.1+build.3:v2")
    modImplementation("net.fabricmc:fabric-loader:0.16.10")

    modImplementation("net.fabricmc.fabric-api:fabric-api:0.104.0+1.21.1")
    modImplementation(fabricApi.module("fabric-command-api-v2", "0.104.0+1.21.1"))

    modImplementation("eu.pb4:polymer-core:0.9.18+1.21.1")
    modImplementation("eu.pb4:polymer-resource-pack:0.9.18+1.21.1")
    modImplementation("eu.pb4:polymer-autohost:0.9.18+1.21.1")
    modImplementation("de.tomalbrc:filament:0.14.7+1.21.1")

    modImplementation("net.fabricmc:fabric-language-kotlin:1.12.3+kotlin.2.0.21")
    modImplementation("eu.pb4:sgui:1.6.1+1.21.1")
    modImplementation("com.cobblemon:fabric:1.6.0+1.21.1-SNAPSHOT")
    modImplementation(files("libs/ArrowCore-1.7.6.jar"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")

    implementation("com.mysql", "mysql-connector-j","9.2.0")
    //implementation("net.objecthunter", "exp4j", "0.4.8")
    modImplementation("net.impactdev.impactor.api:economy:5.3.0")

    compileOnly("net.luckperms:api:5.4")
    runtimeOnly("net.luckperms:api:5.4")
    modImplementation("me.lucko:fabric-permissions-api:0.3.1")
    modImplementation("eu.pb4:placeholder-api:2.4.2+1.21")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand(project.properties)
    }
}

fabricApi {
    configureDataGeneration()
}
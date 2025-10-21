plugins {
    id("com.gradleup.shadow") version "9.2.2"
    id("de.eldoria.plugin-yml.bukkit") version "0.7.1"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation(project(":core"))
    compileOnly("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")
}

bukkit {
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.POSTWORLD
    main = "net.momirealms.sparrow.redis.messagebroker.plugin.SparrowRedisMessageBrokerPlugin"
    version = rootProject.properties["project_version"] as String
    name = "SparrowRedisMessageBroker"
    apiVersion = "1.20"
    authors = listOf("XiaoMoMi")
    foliaSupported = true
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
    dependsOn(tasks.clean)
}

tasks {
    shadowJar {
        archiveFileName = "sparrow-redis-message-broker-${rootProject.properties["project_version"]}.jar"
        destinationDirectory.set(file("$rootDir/target"))
    }
}
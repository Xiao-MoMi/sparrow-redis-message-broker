plugins {
    id("java-library")
    id("maven-publish")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.test)
    compileOnly(libs.lettuce.core)
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.guava)
    compileOnly(libs.caffeine)
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    repositories {
        maven {
            url = uri("https://repo.momirealms.net/releases")
            credentials(PasswordCredentials::class) {
                username = System.getenv("REPO_USERNAME")
                password = System.getenv("REPO_PASSWORD")
            }
        }
    }
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "net.momirealms"
            artifactId = "sparrow-redis-message-broker"
            version = rootProject.properties["project_version"].toString()
            from(components["java"])
        }
    }
}
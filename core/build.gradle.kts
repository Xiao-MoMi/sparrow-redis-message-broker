plugins {
    id("java-library")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.test)
    api(libs.lettuce.core)
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.guava)
}

tasks.test {
    useJUnitPlatform()
}
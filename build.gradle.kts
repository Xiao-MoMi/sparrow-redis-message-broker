plugins {
    id("java")
}

subprojects {
    apply(plugin = "java")
    tasks.processResources {
        filteringCharset = "UTF-8"
        filesMatching(arrayListOf("plugin.properties")) {
            expand(rootProject.properties)
        }
    }
}
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java")
    alias(libs.plugins.kotlin)
    alias(libs.plugins.intelliJPlatform)
}

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
        snapshots()
    }
}

sourceSets {
    test {
        resources {
            setSrcDirs(listOf("src/test/testData"))
        }
    }
}

dependencies {
    testImplementation(libs.junit)

    intellijPlatform {

        jetbrainsRuntime()
        webstorm("251-EAP-SNAPSHOT", useInstaller = false)

        bundledPlugins("JavaScript")

        testFramework(TestFrameworkType.Platform)
        testFramework(TestFrameworkType.Plugin.JavaScript)
    }
}

intellijPlatform {
    pluginConfiguration {
        name = "symbols-api-workshop"
        version = "1.0.0"
    }
}

tasks {
    wrapper {
        gradleVersion = providers.gradleProperty("gradleVersion").get()
    }
}

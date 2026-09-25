pluginManagement {
  repositories {
    google {
      content {
        includeGroupByRegex("com\\.android.*")
        includeGroupByRegex("com\\.google.*")
        includeGroupByRegex("androidx.*")
      }
    }
    mavenCentral()
    gradlePluginPortal()
  }
}

plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" }

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    // مخزن اختصاصی تپسل
    maven { url = uri("https://maven.tapsell.ir") }

    google()
    mavenCentral()

    // برای Poolakey (کافه‌بازار)
    maven { url = uri("https://jitpack.io") }
  }
}

rootProject.name = "CodeVault"

include(":app")

import java.net.URI

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = URI.create("https://jitpack.io") }
    }
}

rootProject.name = "Primal"

include(":app")

include(":core:utils")
include(":core:app-config")
include(":core:networking-http")
include(":core:networking-primal")
include(":core:networking-upload")

include(":data:local-caching")
include(":data:remote-caching")
include(":data:repository-caching")

include(":domain:nostr")
include(":domain:primal")
include(":domain:primal-wallet")

include(":shared")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Mercado Pago SDK repository
        maven { url = uri("https://artifacts.mercadolibre.com/repository/android-releases") }
        // Mapbox Maven repository
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            credentials.username = "mapbox"
            credentials.password = providers.gradleProperty("MAPBOX_DOWNLOADS_TOKEN").getOrElse("")
            authentication.create<BasicAuthentication>("basic")
        }
    }
}

rootProject.name = "Merqora"
include(":app")
include(":benchmark")

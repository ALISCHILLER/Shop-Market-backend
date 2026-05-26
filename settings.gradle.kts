pluginManagement {
    repositories {
        maven(url = uri("https://maven.aliyun.com/repository/google"))
        maven(url = uri("https://maven.aliyun.com/repository/gradle-plugin"))
        maven(url = uri("https://maven.aliyun.com/repository/public"))
        maven(url = uri("https://maven.aliyun.com/repository/central"))

        gradlePluginPortal()
        mavenCentral()
        maven(url = uri("https://mvnhub.ir/"))
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven(url = uri("https://maven.aliyun.com/repository/google"))
        maven(url = uri("https://maven.aliyun.com/repository/gradle-plugin"))
        maven(url = uri("https://maven.aliyun.com/repository/public"))
        maven(url = uri("https://maven.aliyun.com/repository/central"))

        mavenCentral()
        maven(url = uri("https://mvnhub.ir/"))
    }
}

rootProject.name = "eshop-backend"

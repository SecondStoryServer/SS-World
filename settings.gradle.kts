rootProject.name = "SS-World"

includeBuild("C:\\Folder\\SS\\SS-Core") {
    dependencySubstitution {
        substitute(module("me.syari.ss:core")).with(project(":"))
    }
}
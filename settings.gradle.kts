//Include all the existent modules in the project only those which have build.gradle.kts
rootDir
    .walk()
    .maxDepth(1)
    .filter {
        it.name != "buildSrc" && it.isDirectory &&
                file("${it.absolutePath}/build.gradle.kts").exists()
    }
    .forEach {
        include(":${it.name}")
    }

include(
    "app"
)
include(":ceibrocamera")
include(":mypdfviewer")

tasks {
    register("clean", Delete::class) {
        delete(project.layout.buildDirectory)
    }
}
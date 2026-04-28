allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

val newBuildDir: Directory =
    rootProject.layout.buildDirectory
        .dir("../../build")
        .get()
rootProject.layout.buildDirectory.value(newBuildDir)

subprojects {
    val newSubprojectBuildDir: Directory = newBuildDir.dir(project.name)
    project.layout.buildDirectory.value(newSubprojectBuildDir)
}
subprojects {
    project.evaluationDependsOn(":app")
}

subprojects {
    val setupNamespace = {
        val subProject = this
        if (subProject.hasProperty("android")) {
            val androidExtension = subProject.extensions.findByName("android") as? com.android.build.gradle.BaseExtension
            if (androidExtension != null && androidExtension.namespace == null) {
                // Sets the namespace based on the group name to satisfy AGP 8.0+
                val groupName = subProject.group.toString()
                if (groupName.isNotEmpty()) {
                    androidExtension.namespace = groupName
                } else {
                    // Fallback if group is empty
                    androidExtension.namespace = "com.plugin.${subProject.name}"
                }
            }
        }
    }

    // Check if project is already evaluated to prevent the "Cannot run afterEvaluate" error
    if (state.executed) {
        setupNamespace()
    } else {
        afterEvaluate {
            setupNamespace()
        }
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}

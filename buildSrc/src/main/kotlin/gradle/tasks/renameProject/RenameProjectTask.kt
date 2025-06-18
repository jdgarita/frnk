package gradle.tasks.renameProject

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

/**
 * @author Vivien Mahe
 * @since 27/11/2024
 */

abstract class RenameProjectTask @Inject constructor(objects: ObjectFactory) : DefaultTask() {

    companion object {
        private const val PROJECT_NAME_TASK_PARAM = "projectName"
        private const val PACKAGE_NAME_TASK_PARAM = "packageName"

        internal const val ACTUAL_PROJECT_NAME = "kmpship"
    }

    @get:Input
    @get:Optional
    abstract val projectName: Property<String>

    @get:Input
    @get:Optional
    abstract val packageName: Property<String>

    @get:Input
    @get:Optional
    abstract val dryRun: Property<Boolean>

    @get:InputDirectory
    abstract val projectDir: DirectoryProperty

    init {
        group = "custom"
        description =
            "Renames the project's directories and updates references in files. Usage: ./gradlew renameProject -$PROJECT_NAME_TASK_PARAM=MyApp -$PACKAGE_NAME_TASK_PARAM=org.example.myapp"
    }

    @TaskAction
    fun renameProject() {
        // Get projectName property and validate it
        val projectName = projectName.get()
            ?: throw IllegalArgumentException("You must pass the '$PROJECT_NAME_TASK_PARAM' property. Example: ./gradlew renameProject -P$PROJECT_NAME_TASK_PARAM=MyApp")

        require(projectName.matches(Regex("^[a-zA-Z0-9_]+$"))) {
            "Invalid project name: $projectName. Only alphanumeric characters and underscores are allowed."
        }

        val validProjectName = projectName.lowercase()

        // Get packageName property and validate it
        val packageName = packageName.get()
            ?: throw IllegalArgumentException("You must pass the '$PACKAGE_NAME_TASK_PARAM' property. Example: ./gradlew renameProject -P$PACKAGE_NAME_TASK_PARAM=org.example.myapp")

        require(packageName.matches(Regex("^[a-zA-Z][a-zA-Z0-9_.]+$"))) {
            "Invalid package name: $packageName. It must be a valid Java package name."
        }

        // Get dryRun property
        val dryRun = dryRun.getOrElse(false)

        // Get root project directory
        val root = projectDir.get().asFile

        println("Starting project rename task...")
        println("Target project name: $projectName")
        println("Valid project name: $validProjectName")
        println("Target package name: $packageName")
        println(if (dryRun) "Dry run enabled. No changes will be applied." else "Applying changes...")

        // Step 1: Rename directories
        val renamedDirectories = mutableListOf<String>()
        RenameDirectories().execute(projectDir = root, projectName = validProjectName, packageName = packageName, dryRun = dryRun, renamedDirectories = renamedDirectories)

        // Step 2: Update files
        val updatedFiles = mutableListOf<String>()
        ReplaceWordsInFiles().execute(projectDir = root, projectName = projectName, moduleName = validProjectName, packageName = packageName, dryRun = dryRun, updatedFiles = updatedFiles)

        // Step 3: Print summary
        printSummary(renamedDirectories = renamedDirectories, updatedFiles = updatedFiles, dryRun = dryRun)
    }

    private fun printSummary(renamedDirectories: List<String>, updatedFiles: List<String>, dryRun: Boolean) {
        println("\n--- Summary (${if (dryRun) "Dry Run" else "Actual Run"}) ---")

        println("Renamed directories:")
        if (renamedDirectories.isEmpty()) {
            println("None")
        } else {
            renamedDirectories.forEach { println(it) }
        }

        println("\nUpdated files:")
        if (updatedFiles.isEmpty()) {
            println("None")
        } else {
            updatedFiles.forEach {
                if (it.contains("(error:")) {
                    println("Error: $it")
                } else {
                    println(it)
                }
            }
        }

        println("----------------")
        println(if (dryRun) "Dry run completed successfully!" else "Task completed successfully!")
    }
}

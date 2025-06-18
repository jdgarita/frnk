package gradle.tasks.renameProject

import gradle.tasks.renameProject.RenameProjectTask.Companion.ACTUAL_PROJECT_NAME
import java.io.File

/**
 * @author Vivien Mahe
 * @since 30/11/2024
 */
internal class RenameDirectories {

    companion object {
        private const val SHARED_MODULE_NAME = "shared"
        private const val ANDROID_MODULE_NAME = "androidApp"
        private val ACTUAL_PROJECT_PATH = "com${File.separator}tweener${File.separator}$ACTUAL_PROJECT_NAME"
    }

    fun execute(projectDir: File, projectName: String, packageName: String, dryRun: Boolean, renamedDirectories: MutableList<String>) {
        println("\n--- Renaming directories ---")

        // Convert packageName to directory path (e.g., "org.example.myapp" -> "org/example/myapp")
        val packagePath = packageName.replace(".", File.separator)

        fun processAndRenameDirectory(baseDir: File, oldHierarchy: String, newHierarchy: String) {
            baseDir.walkTopDown()
                .filter { file -> file.isDirectory && file.path.contains(oldHierarchy) }
                .forEach { oldDir ->
                    // Replace only the segment starting from `oldHierarchy`
                    val updatedPath = oldDir.path.replace(oldHierarchy, newHierarchy)
                    val newDirPath = File(updatedPath)

                    // Ensure the full path to the new directory exists
                    if (!newDirPath.parentFile.exists()) {
                        if (dryRun) {
                            println("\n------ Dry run: Would create parent directories: ${newDirPath.parentFile.absolutePath}")
                        } else {
                            println("Creating parent directories: ${newDirPath.parentFile.absolutePath}")
                            newDirPath.parentFile.mkdirs()
                        }
                    }

                    // Create the target directory
                    if (!newDirPath.exists()) {
                        if (dryRun) {
                            println("\n------ Dry run: Would create directory: ${newDirPath.absolutePath}")
                        } else {
                            println("Creating directory: ${newDirPath.absolutePath}")
                            newDirPath.mkdirs()
                        }
                    }

                    // Move the files and subdirectories
                    if (dryRun) {
                        println("\n------ Dry run: Would move contents of: ${oldDir.absolutePath} -> ${newDirPath.absolutePath}")
                        renamedDirectories.add("${oldDir.absolutePath} -> ${newDirPath.absolutePath} (dry run)")
                    } else {
                        println("Moving contents of: ${oldDir.absolutePath} -> ${newDirPath.absolutePath}")
                        oldDir.listFiles()?.forEach { file ->
                            val targetFile = File(newDirPath, file.name)
                            if (file.renameTo(targetFile)) {
                                println("Moved: ${file.absolutePath} -> ${targetFile.absolutePath}")
                            } else {
                                println("Failed to move: ${file.absolutePath}")
                            }
                        }

                        // Clean up old directory if empty
                        if (oldDir.delete()) {
                            println("Deleted old directory: ${oldDir.absolutePath}")
                        }
                    }
                }
        }

        fun deleteEmptyDirectoriesRecursively(directory: File) {
            directory.walkBottomUp().forEach { subDir ->
                if (subDir.isDirectory && subDir.listFiles()?.all { !it.isFile } == true) {
                    if (dryRun) {
                        println("\n------ Dry run: Would delete empty directory: ${subDir.absolutePath}")
                    } else {
                        println("Deleting empty directory: ${subDir.absolutePath}")
                        if (!subDir.delete()) {
                            println("Failed to delete directory: ${subDir.absolutePath}")
                        }
                    }
                }
            }
        }

//        // Step 1: Rename the root "changehere" directory to "mylibrary"
//        projectDir.listFiles { file -> file.isDirectory }?.forEach { dir ->
//            if (dir.name == ACTUAL_PROJECT_NAME) {
//                val newDir = File(dir.parentFile, projectName)
//                if (dryRun) {
//                    println("\n------ Dry run: Root directory renamed: ${dir.absolutePath} -> ${newDir.absolutePath}")
//                    renamedDirectories.add("${dir.absolutePath} -> ${newDir.absolutePath} (dry run)")
//                } else {
//                    if (dir.renameTo(newDir)) {
//                        println("Renamed root directory: ${dir.absolutePath} -> ${newDir.absolutePath}")
//                        renamedDirectories.add("${dir.absolutePath} -> ${newDir.absolutePath}")
//                    } else {
//                        println("Failed to rename root directory: ${dir.absolutePath}. Check if it's in use or locked.")
//                    }
//                }
//            }
//        }

        // Step 2: Rename subdirectories recursively inside "shared" and "androidApp"
        projectDir.listFiles { file -> file.isDirectory }?.forEach { baseDir ->
            when (baseDir.name) {
                SHARED_MODULE_NAME, ANDROID_MODULE_NAME -> {
                    println("Processing subdirectories in '${baseDir.name}': ${baseDir.absolutePath}")
                    processAndRenameDirectory(baseDir, ACTUAL_PROJECT_PATH, packagePath)
                }
            }
        }

        // Step 3: Delete all empty directories recursively in "shared" and "androidApp"
        projectDir.listFiles { file -> file.isDirectory }?.forEach { baseDir ->
            when (baseDir.name) {
                SHARED_MODULE_NAME, ANDROID_MODULE_NAME -> {
                    println("Deleting empty directories inside '${baseDir.name}'")
                    deleteEmptyDirectoriesRecursively(baseDir)
                }
            }
        }
    }
}

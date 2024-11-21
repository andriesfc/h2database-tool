# Kotlin Java Project Template

> **TL;DR**
> 
> A collection of build logic scripts which configures multi java/kotlin project builds.

## How to use it

### Initial Setup

1. Create a new repository on GitHub using this project as a template.
2. Clone the newly created project.
3. Open the [settings.gradle.kts](settings.gradle.kts) file.
   1. Find the statement: `rootProject.name = "kotlin-java-template"`
   2. Replace the value of `kotlin-java-template` with the name of the new project.
    
   **IMPORTANT**: Remember to use double quotes.
   
4. Save the file, and closed it.
5. Reload the project from the Gradle view.
6. Replace this **README.md** with the project-specific version.
7. Run the following to ensure all the new changes build without problems: 
   1. For macOS/Linux run the following:
   
      ```shell
       ./gradlew clean build
      ```
      
   2. For windows run:      
   
      ```batch
      gradlew.bat clean build
      ```

8. Using GIT, commit and push-up changes to the remote repository.

### Upgrading dependencies

1. Open the [lib.versions.toml](gradle/libs.versions.toml) file.
2. Find the `[versions]` section. 
3. Change the corresponding version.
4. Save and reload the Gradle project from the IDE.

## What is in it?

A collection of Gradle build logic scripts to elevate the tedium of configuring 
Kotlin (and Java) for a multi-project build.
This setup also includes two testing frameworks, one for Java and another for Kotlin:

- **Java**: [JUnit5](https://junit.org/junit5/) based on a version configures in the `libs.versions.toml` file.
- **Kotlin**: [Kotest](https://kotest.io/) for Kotlin projects.

In addition, it also includes only two template projects with the absolute minimum dependencies:

- A kotlin command line [`app`](app/build.gradle.kts) which depends on a [`commons`](commons/build.gradle.kts) module
- The `commons` module which has no other dependencies (except the build scripts).

This template project drives your multi-project build via the following build logic plugins:

| Build Logic Script Plugin                                                                 | Purpose                                                                                                          |
|-------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------|
| [`buildlogic.java`](build-logic/src/main/kotlin/buildlogic.java.gradle.kts)               | Sets up maven repositories, java tool chain, JUnit5 based on version specified in the `libs.versions.toml` file. |
| [`buildlogic.kotlin`](build-logic/src/main/kotlin/buildlogic.kotlin.gradle.kts)           | Sets up kotlin compiler, and add the kotest dependencies to the test suit.                                       |
| [`buildlogic.projectroot`](build-logic/src/main/kotlin/buildlogic.projectroot.gradle.kts) | Applied to root project to ensure that all modules have the same version and group info.                         |
| [`buildlogic.kotlin.app`](build-logic/src/main/kotlin/buildlogic.kotlin.app.gradle.kts)   | Applies the `buildlogic.kotlin` application, followed by the application plugin with some JDK 21 defaults.       |
| [`buildlogic.dokkatoo`](build-logic/src/main/kotlin/buildlogic.dokkatoo.gradle.kts)       | Applied Dokatoo to kotlin project. (Note this already applied the buildlogic.kotlin and the root project.        |

These build scripts use versions as specified in the `gradle/lib.versions.toml` file to drive the versions of the Gradle plugins.


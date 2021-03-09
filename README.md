# QualityMinds Gherkin-Test-Automation Framework

## Technology Stack

* Gradle
* Kotlin DSL
* Gherkin
* Serenity BDD
* Cucumber
* Spring boot


## Gradle is the build managment tool here, description of important files of gradle.

 * Build.gradle: Defines the build configuration scripts , simply specify the dependency in your Gradle build file.

* Settings.gradle: This script will be almost first in project, you can add subprojects to your build.

* Gradle.properties: This file is a simple Java Properties file and simple key-value store that only allows string values.

* Gradlew or gradle.bat: A shell script and a Windows batch script for executing the build with the Wrapper.

* Gradle wrapper: This is just a small utility that will ensure that Gradle is installed (or install it if necessary) so you can always build the project, No need to install gradle on your local system.

#### Documenatation of gradle: https://docs.gradle.org/current/userguide/userguide.html


## How to download and use this project 

* Download source code from git repository: https://github.com/QualityMinds/gherkin-test-automation

* Should have JAVA installed on system: https://www.guru99.com/install-java.html

* Download and Install IntelliJ IDE: https://www.jetbrains.com/idea/download/#section=windows

* Open the project in Intellij, It would automatically do the indexing the build the project.

* Something have to perform 2 steps.

1) Select Core folder, right click and build the module then check there should be build folder created. 
2) Select the WebApplication, right click and build the module then check there should be build folder created.

#### Right now we have test only in core module so just select it, right click and run it. It will run cucmber test and junit test.


# TEST REPORT

You can found a target folder under the the core module, There you can see the HTML report.


## Can run through cmd or IntelliJ and so on ***


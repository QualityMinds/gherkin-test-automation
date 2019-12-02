plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":Core"))

    testCompile("junit", "junit", "4.12")
}

tasks.named<Test>("test") {
    systemProperty("file.encoding", "utf-8")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

gradle.startParameter.setContinueOnFailure(true)

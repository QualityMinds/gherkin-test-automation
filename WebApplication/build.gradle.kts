val seleniumVersion = "3.141.59"

plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":Core"))

    //Galen
    api("com.galenframework:galen-java-support:2.4.4")

    //Selenide
    api("com.codeborne:selenide:5.5.1")

    //thewaiter
    api("com.imalittletester:thewaiter:1.0")

    //Webdrivermanager
    api("io.github.bonigarcia:webdrivermanager:3.7.1")

    //Selenium
    api("org.seleniumhq.selenium:selenium-firefox-driver:${seleniumVersion}")
    api("org.seleniumhq.selenium:selenium-chrome-driver:${seleniumVersion}")
    api("org.seleniumhq.selenium:selenium-edge-driver:${seleniumVersion}")
    api("org.seleniumhq.selenium:selenium-safari-driver:${seleniumVersion}")
    api("org.seleniumhq.selenium:selenium-remote-driver:${seleniumVersion}")

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

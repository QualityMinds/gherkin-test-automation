val seleniumVersion = "3.141.59"

plugins {
    `java-library`
    `maven-publish`
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

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("source")
    from(sourceSets.main.get().allSource)
}

publishing {
    repositories {
        maven {
            val snapshotsRepoUrl = uri("https://oss.sonatype.org/content/repositories/snapshots")
            val releasesRepoUrl = uri("https://oss.sonatype.org/content/repositories/releases/")
            credentials(PasswordCredentials::class.java) {
                username = rootProject.extra.get("ossrhUser")?.toString()
                password = rootProject.extra.get("ossrhPassword")?.toString()
            }
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
        }
    }
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifact(sourcesJar.get())
            pom {
                name.set("GTA GTA WebApplication")
                description.set("The webapplication module of GTA")
                url.set("https://github.com/QualityMinds/gherkin-test-automation/tree/master/WebApplication")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("tva")
                        name.set("Tobias Varlemann")
                        email.set("tobias.varlemann@qualityminds.de")
                    }
                    developer {
                        id.set("jfo")
                        name.set("Johann Förster")
                        email.set("johann.foerster@qualityminds.de")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/QualityMinds/gherkin-test-automation.git")
                    developerConnection.set("scm:git:https://github.com/QualityMinds/gherkin-test-automation.git")
                    url.set("https://github.com/QualityMinds/gherkin-test-automation/tree/master/WebApplication")
                }
            }

        }
    }
}


gradle.startParameter.setContinueOnFailure(true)

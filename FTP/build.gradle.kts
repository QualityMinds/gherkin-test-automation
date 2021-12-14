plugins {
    `java-library`
    `maven-publish`
    id("io.freefair.lombok") version "4.1.6"
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":Core"))
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
            val releasesRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
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
                name.set("GTA FTP")
                description.set(
                    "The FTP module of GTA."
                )
                url.set("https://github.com/QualityMinds/gherkin-test-automation/tree/master/FTP")
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
                    url.set("https://github.com/QualityMinds/gherkin-test-automation/tree/master/FTP")
                }
            }

        }
    }
}


gradle.startParameter.setContinueOnFailure(true)

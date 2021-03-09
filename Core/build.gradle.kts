import com.github.jk1.license.render.ReportRenderer
import com.github.jk1.license.render.InventoryHtmlReportRenderer
import com.github.jk1.license.render.SimpleHtmlReportRenderer
import com.github.jk1.license.render.CsvReportRenderer
import com.github.jk1.license.filter.DependencyFilter
import com.github.jk1.license.filter.LicenseBundleNormalizer

val serenityVersion = "2.0.81"
val serenityCucumberVersion = "2.1.2"
val springBootVersion = "2.4.3"

plugins {
    `java-library`
    `maven-publish`
    id("com.github.jk1.dependency-license-report") version "1.16"
    // id("net.serenity-bdd.aggregator") version "2.0.81"
}

licenseReport {
    renderers = arrayOf<ReportRenderer>(InventoryHtmlReportRenderer("report.html","Core"),SimpleHtmlReportRenderer("report-simple.html"),CsvReportRenderer("report.csv"))
    filters = arrayOf<DependencyFilter>(LicenseBundleNormalizer())
}

dependencies {
    //Serenity  
    api("net.serenity-bdd:serenity-core:${serenityVersion}")                  
    api("net.serenity-bdd:serenity-junit:${serenityVersion}")
    api("net.serenity-bdd:serenity-spring:${serenityVersion}")
    
    //Cucumber
    api("net.serenity-bdd:serenity-cucumber4:${serenityCucumberVersion}")
    
    // Spring
    api("org.springframework.boot:spring-boot-autoconfigure:${springBootVersion}")
    api("org.springframework.boot:spring-boot-starter-test:${springBootVersion}")

    //Utils
    api("org.assertj:assertj-core:3.19.0")
    api("org.slf4j:slf4j-simple:2.0.0-alpha1")
    api("org.yaml:snakeyaml:1.28")
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
                name.set("GTA Core")
                description.set("The core module of GTA")
                url.set("https://github.com/QualityMinds/gherkin-test-automation/tree/master/Core")
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
                    url.set("https://github.com/QualityMinds/gherkin-test-automation/tree/master/Core")
                }
            }

        }
    }
}

gradle.startParameter.setContinueOnFailure(true)

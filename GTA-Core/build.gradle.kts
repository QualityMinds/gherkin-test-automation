val serenityVersion = "2.0.81"
val serenityCucumberVersion = "1.0.21"
val springBootVersion = "2.2.0.RELEASE"

plugins {
    id("java-library")
    // id("net.serenity-bdd.aggregator") version "2.0.81"
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
    api("org.assertj:assertj-core:3.12.1")
    api("org.slf4j:slf4j-simple:1.8.0-beta4")
    api("org.yaml:snakeyaml:1.24")
}

tasks.named<Test>("test") {
    // TODO: translate from Groovy to Kotlin
    // systemProperties = System.getProperties()
    systemProperty("file.encoding", "utf-8")
}

gradle.startParameter.setContinueOnFailure(true)                 

buildscript {
    var ossrhUser: String? by extra
    ossrhUser = if(ossrhUser!=null) ossrhUser else System.getenv("ossrhUser")
    var ossrhPassword: String? by extra
    ossrhPassword = if(ossrhPassword!=null) ossrhPassword else System.getenv("ossrhPassword")
}

allprojects {
    group = "de.qualityminds.gta"
    version = "0.0.1-RC1"

    repositories {
        jcenter()
        mavenCentral()
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots")
        }
    }
}


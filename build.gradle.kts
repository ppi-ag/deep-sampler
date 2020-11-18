/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

plugins {
    `java-library`
    jacoco
    id("org.sonarqube") version "3.0"
}

allprojects {
    version = "1.0.0"
    group = "de.ppi.deepsampler"

    repositories {
        jcenter()
    }
}



jacoco {
    toolVersion = "0.8.5"
    reportsDir = file("$buildDir/customJacocoReportDir")
}

sonarqube {
    properties {
        property("sonar.projectKey", "ppi-ag_deep-sampler")
        property("sonar.organization", "ppi-ag")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "jacoco")
    dependencies {
        testImplementation("org.mockito:mockito-core:3.3.3")
        if (project.name != "junit4") {
            testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
            testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
        }
    }

    tasks.named<Test>("test") {
        finalizedBy(tasks.jacocoTestReport, tasks.jacocoTestCoverageVerification)
        useJUnitPlatform()
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withSourcesJar()
}

dependencies {
    testImplementation("org.mockito:mockito-core:3.3.3")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport, tasks.jacocoTestCoverageVerification)
}


tasks.jacocoTestReport {
    dependsOn(tasks.named<Test>("test"))
    reports {
        xml.isEnabled = true
        csv.isEnabled = false
        html.destination = file("${buildDir}/jacocoHtml")
    }
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.named<Test>("test"))
    violationRules {
        rule {
            limit {
                minimum = "0.5".toBigDecimal()
            }
        }

        rule {
            enabled = false
            element = "CLASS"
            includes = listOf("org.gradle.*")

            limit {
                counter = "LINE"
                value = "TOTALCOUNT"
                maximum = "0.3".toBigDecimal()
            }
        }
    }
}


tasks.register<Javadoc>("JavadocAll") {
    source = fileTree(".").matching {include("**/src/main/java/**")}

    val classPathList = subprojects.flatMap { subProject -> subProject.sourceSets.main.get().compileClasspath }
    classpath = files(classPathList)
}


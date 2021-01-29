/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

plugins {
    `java-library`
    jacoco
    id("org.sonarqube") version "3.0"
    id("com.vanniktech.maven.publish") version "0.13.0"
}

allprojects {
    version = "1.0.0"
    group = "de.ppi"

    apply(plugin = "java-library")
    apply(plugin = "jacoco")

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    repositories {
        jcenter()
    }

    jacoco {
        toolVersion = "0.8.6"
    }

}

jacoco {
    toolVersion = "0.8.6"
}

tasks.jar {
    from("LICENSE.md") {
        into("/META-INF")
    }
}

mavenPublish {
    nexus {
        baseUrl = "https://oss.sonatype.org/service/local/"
        stagingProfile = "de.ppi"
    }
}


sonarqube {
    properties {
        property("sonar.projectKey", "ppi-ag_deep-sampler")
        property("sonar.organization", "ppi-ag")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.coverage.jacoco.xmlReportPaths", "$buildDir/reports/jacoco/codeCoverageReport/codeCoverageReport.xml")
    }
}

// task to gather code coverage from multiple subprojects
// NOTE: the `JacocoReport` tasks do *not* depend on the `test` task by default. Meaning you have to ensure
// that `test` (or other tasks generating code coverage) run before generating the report.
// You can achieve this by calling the `test` lifecycle task manually
// $ ./gradlew test codeCoverageReport
tasks.register<JacocoReport>("codeCoverageReport") {
    // If a subproject applies the 'jacoco' plugin, add the result it to the report
    subprojects {
        val subproject = this
        subproject.plugins.withType<JacocoPlugin>().configureEach {
            subproject.tasks.matching({ it.extensions.findByType<JacocoTaskExtension>() != null }).configureEach {
                val testTask = this
                sourceSets(subproject.sourceSets.main.get())
                executionData(testTask)
            }

            // To automatically run `test` every time `./gradlew codeCoverageReport` is called,
            // you may want to set up a task dependency between them as shown below.
            // Note that this requires the `test` tasks to be resolved eagerly (see `forEach`) which
            // may have a negative effect on the configuration time of your build.
            subproject.tasks.matching({ it.extensions.findByType<JacocoTaskExtension>() != null }).forEach {
                rootProject.tasks["codeCoverageReport"].dependsOn(it)
            }
        }
    }

    // enable the different report types (html, xml, csv)
    reports {
        // xml is usually used to integrate code coverage with
        // other tools like SonarQube, Coveralls or Codecov
        xml.isEnabled = true

        // HTML reports can be used to see code coverage
        // without any external tools
        html.isEnabled = true
    }
}

var projectsWithoutTests = arrayOf("deepsampler-provider")

subprojects {

    apply(plugin = "java-library")

    if (!projectsWithoutTests.contains(project.name)) {
        collectTestCoverage()
    }


    tasks.jar {
        from("../LICENSE.md") {
            into("/META-INF")
        }
    }

    dependencies {
        testImplementation("org.mockito:mockito-core:3.3.3")
        if (project.name != "deepsampler-junit4") {
            testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
            testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
        }
    }




}

dependencies {
    testImplementation("org.mockito:mockito-core:3.3.3")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    maxParallelForks = 1
}




tasks.register<Javadoc>("JavadocAll") {
    source = fileTree(".").matching { include("**/src/main/java/**") }

    val classPathList = subprojects.flatMap { subProject -> subProject.sourceSets.main.get().compileClasspath }
    classpath = files(classPathList)
}

fun Project.collectTestCoverage() {
    apply(plugin = "jacoco")

    tasks.named<Test>("test") {
        finalizedBy(tasks.jacocoTestReport, tasks.jacocoTestCoverageVerification)
        useJUnitPlatform()
    }

    tasks.jacocoTestReport {
        dependsOn(tasks.named<Test>("test"))
        reports {
            xml.isEnabled = false // This must be deactivated for individual projects, since otherwise sonar cloud would collect
            // coverage data from each individual project without combining cross-project calls. Instead sonar cloud is supposed to
            // use die merged report, which is gerenerated by the task codeCoverageReport.
            csv.isEnabled = false
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
}
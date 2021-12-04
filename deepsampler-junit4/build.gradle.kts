import org.gradle.kotlin.dsl.provider.inClassPathMode

/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */
plugins {
    id("com.vanniktech.maven.publish")
}

dependencies {
    implementation("junit:junit:4.13")
    implementation(project(":deepsampler-core"))
    implementation(project(":deepsampler-persistence"))
    implementation(project(":deepsampler-persistence-json"))
    implementation(project(":deepsampler-junit"))
    testImplementation(testFixtures(project(":deepsampler-junit")))
    testImplementation(group = "com.google.inject", name = "guice", version = "4.0")
    testImplementation(project(":deepsampler-provider-guice"))

    testImplementation(files("./src/test/tmp"))
}


tasks.test {
    useJUnit()
}

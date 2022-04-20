/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

plugins {
    id("com.vanniktech.maven.publish")
}

dependencies {
    implementation(project(":deepsampler-core"))
    implementation(project(":deepsampler-junit"))
    implementation(project(":deepsampler-persistence-json"))
    implementation(project(":deepsampler-persistence"))

    implementation("com.fasterxml.jackson.core:jackson-databind:2.11.0")

    testImplementation(project(":deepsampler-junit5"))
    testImplementation(testFixtures(project(":deepsampler-junit")))
    testImplementation("org.assertj:assertj-core:3.21.0")
    testImplementation(group = "com.google.inject", name = "guice", version = "4.0")
}
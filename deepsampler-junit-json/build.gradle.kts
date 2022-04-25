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

    implementation(libs.jackson.databind)

    testImplementation(project(":deepsampler-junit5"))
    testImplementation(testFixtures(project(":deepsampler-junit")))
    testImplementation(libs.assertj)
    testImplementation(libs.guice)
}
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
    implementation(libs.junit.jupiter.api)

    testImplementation(testFixtures(project(":deepsampler-junit")))
    testImplementation(project(":deepsampler-provider-guice"))
    testImplementation(project(":deepsampler-junit-json"))
    testImplementation(libs.assertj)
    testImplementation(libs.guice)
    testImplementation(libs.assertj)

    testImplementation(files("./src/test/tmp"))
}
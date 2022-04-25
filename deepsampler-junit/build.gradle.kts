/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

plugins {
    `java-test-fixtures`
    id("com.vanniktech.maven.publish")
}

dependencies {
    implementation(project(":deepsampler-core"))
    implementation(project(":deepsampler-persistence"))

    testFixturesImplementation(project(":deepsampler-core"))
    testFixturesImplementation(project(":deepsampler-persistence"))
    testFixturesImplementation(libs.junit.jupiter.api)
    testFixturesImplementation(libs.guice)
    testFixturesImplementation(project(":deepsampler-provider-guice"))
    testImplementation(libs.assertj)

}
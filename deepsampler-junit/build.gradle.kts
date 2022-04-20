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
    testFixturesImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testFixturesImplementation(group = "com.google.inject", name = "guice", version = "4.0")
    testFixturesImplementation(project(":deepsampler-provider-guice"))
    testImplementation("org.assertj:assertj-core:3.21.0")

}
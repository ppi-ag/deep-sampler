/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */
plugins {
    id("com.vanniktech.maven.publish")
}

dependencies {
    implementation(libs.guice)
    implementation(project(":deepsampler-core"))
    testImplementation(testFixtures(project(":deepsampler-provider")))
}
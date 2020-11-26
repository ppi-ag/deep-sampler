/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */
plugins {
    id("com.vanniktech.maven.publish")
}

dependencies {
    implementation(group = "com.google.inject", name = "guice", version = "4.0")
    implementation(project(":deepsampler-core"))
    testImplementation(testFixtures(project(":deepsampler-provider")))
}
/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */
plugins {
    id("com.vanniktech.maven.publish")
}

dependencies {
    implementation(project(":deepsampler-core"))
    implementation(project(":deepsampler-junit"))
    implementation(project(":deepsampler-persistence"))
    implementation(project(":deepsampler-persistence-json"))
    implementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testImplementation(testFixtures(project(":deepsampler-junit")))
}
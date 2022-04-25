/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */
plugins {
    id("com.vanniktech.maven.publish")
}

dependencies {
    implementation(project(":deepsampler-core"))
    implementation(project(":deepsampler-persistence"))

    implementation(libs.jackson.databind)
    implementation(libs.jackson.module.parameter.names)
    implementation(libs.jackson.datatype.jsr310)
}
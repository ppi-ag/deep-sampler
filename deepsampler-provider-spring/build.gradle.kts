/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */
plugins {
    id("com.vanniktech.maven.publish")
}

dependencies {
    implementation(libs.aspectj.rt)
    implementation(libs.aspectj.weaver)
    implementation(libs.spring.aop)
    implementation(libs.spring.context)
    implementation(libs.spring.test)
    implementation(project(":deepsampler-core"))
    testImplementation(testFixtures(project(":deepsampler-provider")))
}
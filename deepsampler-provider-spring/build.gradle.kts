/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */
plugins {
    id("com.vanniktech.maven.publish")
}

dependencies {
    implementation("org.aspectj:aspectjrt:1.9.6")
    implementation("org.aspectj:aspectjweaver:1.9.6")
    implementation("org.springframework:spring-aop:5.2.8.RELEASE")
    implementation("org.springframework:spring-context:5.2.8.RELEASE")
    implementation("org.springframework:spring-test:5.2.8.RELEASE")
    implementation(project(":deepsampler-core"))
    testImplementation(testFixtures(project(":deepsampler-provider")))
}
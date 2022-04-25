/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

plugins {
    id("com.vanniktech.maven.publish")
}

dependencies {
    implementation(libs.junit.v4)
    implementation(project(":deepsampler-core"))
    implementation(project(":deepsampler-junit"))

    testImplementation(testFixtures(project(":deepsampler-junit")))
    testImplementation(project(":deepsampler-junit-json"))
    testImplementation(project(":deepsampler-provider-guice"))
    testImplementation(libs.guice)

    testImplementation(files("./src/test/tmp"))
}


tasks.test {
    useJUnit()
}

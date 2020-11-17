/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

dependencies {
    implementation("junit:junit:4.13")
    implementation(project(":deepsampler-core"))
    implementation(project(":deepsampler-persistence"))
    implementation(project(":deepsampler-persistence-json"))
    implementation(project(":deepsampler-junit"))
    testImplementation(testFixtures(project(":deepsampler-junit")))
}

tasks.test {
    useJUnit()
}

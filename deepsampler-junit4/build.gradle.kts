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

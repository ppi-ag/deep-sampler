dependencies {
    implementation(project(":deepsampler-core"))
    implementation(project(":deepsampler-junit"))
    implementation(project(":deepsampler-persistence"))
    implementation(project(":deepsampler-persistence-json"))
    implementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testImplementation(testFixtures(project(":deepsampler-junit")))
}
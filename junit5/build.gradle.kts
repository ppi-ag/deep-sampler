dependencies {
    implementation(project(":core"))
    implementation(project(":junit"))
    implementation(project(":persistence"))
    implementation(project(":persistence-json"))
    implementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testImplementation(testFixtures(project(":junit")))
}
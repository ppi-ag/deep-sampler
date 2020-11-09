dependencies {
    implementation("junit:junit:4.13")
    implementation(project(":core"))
    implementation(project(":persistence"))
    implementation(project(":persistence-json"))
    implementation(project(":junit"))
    testImplementation(testFixtures(project(":junit")))
}

tasks.test {
    useJUnit()
}

dependencies {
    implementation(group = "com.google.inject", name = "guice", version = "4.0")
    implementation(project(":core"))
    testImplementation(testFixtures(project(":provider")))
}
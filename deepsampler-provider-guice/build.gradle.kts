dependencies {
    implementation(group = "com.google.inject", name = "guice", version = "4.0")
    implementation(project(":deepsampler-core"))
    testImplementation(testFixtures(project(":deepsampler-provider")))
}
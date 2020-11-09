plugins {
    `java-test-fixtures`
}

dependencies {
    implementation(project(":core"))
    implementation(project(":persistence-json"))
    implementation(project(":persistence"))

    testFixturesImplementation(project(":core"))
    testFixturesImplementation(project(":persistence-json"))
    testFixturesImplementation(project(":persistence"))
}
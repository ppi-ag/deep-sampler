plugins {
    `java-test-fixtures`
}

dependencies {
    testFixturesImplementation(project(":core"))
    testFixturesImplementation(project(":persistence"))
    testFixturesImplementation(project(":persistence-json"))
    testFixturesImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
}



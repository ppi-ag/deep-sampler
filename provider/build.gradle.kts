plugins {
    `java-test-fixtures`
}

dependencies {
    testFixturesImplementation(project(":core"))
    testFixturesImplementation(project(":persistence"))
    testFixturesImplementation(project(":persistence-json"))
    testFixturesImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testFixturesApi("org.apache.commons:commons-lang3:3.9")
    testFixturesImplementation("org.apache.commons:commons-text:1.6")
}



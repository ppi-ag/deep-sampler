plugins {
    `java-test-fixtures`
}

dependencies {
    testFixturesImplementation(project(":deepsampler-core"))
    testFixturesImplementation(project(":deepsampler-persistence"))
    testFixturesImplementation(project(":deepsampler-persistence-json"))
    testFixturesImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
}



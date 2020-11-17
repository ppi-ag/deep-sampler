/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

plugins {
    `java-test-fixtures`
}

dependencies {
    testFixturesImplementation(project(":deepsampler-core"))
    testFixturesImplementation(project(":deepsampler-persistence"))
    testFixturesImplementation(project(":deepsampler-persistence-json"))
    testFixturesImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
}



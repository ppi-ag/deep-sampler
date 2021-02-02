/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

plugins {
    id("com.vanniktech.maven.publish")
}

dependencies {
    implementation("org.javassist:javassist:3.27.0-GA")
    implementation("org.objenesis:objenesis:3.1")
    testImplementation("org.awaitility:awaitility:4.0.3")
}
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
    implementation(group = "org.apache.commons", name = "commons-lang3", version = "3.11")
    testImplementation("org.awaitility:awaitility:4.0.3")
}
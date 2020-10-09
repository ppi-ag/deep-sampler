dependencies {
    implementation("junit:junit:4.13")
    implementation(project(":core"))
    implementation(project(":junit"))
}

tasks.test {
    useJUnit()
}

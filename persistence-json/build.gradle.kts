dependencies {
    implementation(project(":core"))
    implementation(project(":persistence"))

    implementation("com.fasterxml.jackson.core:jackson-databind:2.11.0")
    implementation("com.fasterxml.jackson.module:jackson-module-parameter-names:2.11.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.11.0")
}
dependencies {
    implementation("org.aspectj:aspectjrt:1.9.6")
    implementation("org.aspectj:aspectjweaver:1.9.6")
    implementation("org.springframework:spring-aop:5.2.8.RELEASE")
    implementation("org.springframework:spring-context:5.2.8.RELEASE")
    implementation("org.springframework:spring-test:5.2.8.RELEASE")
    implementation(project(":core"))
    implementation(project(":provider"))
}
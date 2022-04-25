/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */


rootProject.name = "deepsampler"

enableFeaturePreview("VERSION_CATALOGS")

dependencyResolutionManagement {
	versionCatalogs {
		create("libs") {

			alias("javassist").to("org.javassist:javassist:3.27.0-GA")
			alias("objenesis").to("org.objenesis:objenesis:3.2")
			alias("guice").to("com.google.inject:guice:5.1.0")

			alias("jackson-databind").to("com.fasterxml.jackson.core:jackson-databind:2.13.2.2")
			alias("jackson-module-parameter-names").to("com.fasterxml.jackson.module:jackson-module-parameter-names:2.11.0")
			alias("jackson-datatype-jsr310").to("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.11.0")

			alias("aspectj-rt").to("org.aspectj:aspectjrt:1.9.6")
			alias("aspectj-weaver").to("org.aspectj:aspectjweaver:1.9.6")

			alias("spring-aop").to("org.springframework:spring-aop:5.3.19")
			alias("spring-context").to("org.springframework:spring-context:5.2.8.RELEASE")
			alias("spring-test").to("org.springframework:spring-test:5.3.19")

			// Test
			alias("awaitility").to("org.awaitility:awaitility:4.0.3")
			alias("junit-jupiter-api").to("org.junit.jupiter:junit-jupiter-api:5.6.0")
			alias("junit-v4").to("junit:junit:4.13.2")
			alias("assertj").to("org.assertj:assertj-core:3.21.0")
		}
	}
}

include ("deepsampler-provider",
	"deepsampler-provider-spring",
	"deepsampler-provider-guice",
	"deepsampler-core",
	"deepsampler-persistence",
	"deepsampler-persistence-json",
	"deepsampler-junit",
	"deepsampler-junit-json",
	"deepsampler-junit4",
	"deepsampler-junit5")
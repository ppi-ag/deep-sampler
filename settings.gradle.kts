/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

rootProject.name = "deepsampler"

include ("deepsampler-provider",
	"deepsampler-provider-spring",
	"deepsampler-provider-guice",
	"deepsampler-core",
	"deepsampler-persistence",
	"deepsampler-persistence-json",
	"deepsampler-junit",
	"deepsampler-junit4",
	"deepsampler-junit5")
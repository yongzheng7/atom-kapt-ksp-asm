import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:4.2.0")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
    testImplementation("org.assertj:assertj-core:3.22.0")

    implementation("com.google.devtools.ksp:symbol-processing-api:1.6.10-1.0.2")
    implementation("com.squareup:kotlinpoet:1.10.0")
    implementation("com.squareup:kotlinpoet-ksp:1.10.0")
    implementation(project(":module-annotation"))
    testImplementation(project(":compiler-test-ksp"))
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = listOf(
            "-Xuse-experimental=com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview",
            "-Xuse-experimental=com.google.devtools.ksp.KspExperimental"
        )
    }
}
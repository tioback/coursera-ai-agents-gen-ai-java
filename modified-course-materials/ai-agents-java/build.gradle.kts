plugins {
    id("java")
}

// Derived from Coursera "Building AI Agents in Java" course materials.
// Modified to support local adaptations in `modified-course-materials/`.

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
    // Make source level explicit so IDEs (Cursor/VS Code Java) know text blocks are allowed
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

group = "com.renatoback"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.openai:openai-java:1.5.0")
    implementation("org.reflections:reflections:0.10.2")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}


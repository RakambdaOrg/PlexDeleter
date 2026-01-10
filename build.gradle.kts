import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

plugins {
    java
    idea
    alias(libs.plugins.sptringbootDependencyManagement)
    alias(libs.plugins.sptringboot)
    alias(libs.plugins.graalvm)
}

group = "fr.rakambda"
description = "PlexDeleter"

repositories {
    mavenCentral()
    maven {
        url = uri("https://projectlombok.org/edge-releases")
    }
}

ext["lombok.version"] = "edge-SNAPSHOT"

configurations {
    compileOnly {
        extendsFrom(configurations["annotationProcessor"])
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.session:spring-session-jdbc")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-mysql")
    implementation("org.mariadb.jdbc:mariadb-java-client")
    implementation(libs.logbackEcs)
    compileOnly("org.jspecify:jspecify")

    implementation(libs.thymeleafSpringSecurity)
    implementation(libs.webauthn4j)
    implementation(libs.mjml4j)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")
    compileOnly("org.projectlombok:lombok")

    testAnnotationProcessor("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")


    developmentOnly("org.springframework.boot:spring-boot-devtools")
}

tasks {
    test {
        useJUnitPlatform()
    }

    graalvmNative {
        testSupport = false
    }

    bootJar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}

tasks.withType<BootBuildImage> {
    builder = "paketobuildpacks/builder-jammy-full:latest"
    environment = mapOf(
        // 1. Force GraalVM to initialize the image with UTF-8
        "BP_NATIVE_IMAGE_BUILD_ARGUMENTS" to "-H:+AddAllCharsets -march=compatibility -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8",

        // 2. These variables tell the Paketo builder to use UTF-8 during the build process
        "LC_ALL" to "en_US.UTF-8",
        "LANG" to "en_US.UTF-8",
        "LC_CTYPE" to "en_US.UTF-8",

        // 3. These set the environment for the resulting container at runtime
        "BPE_LANG" to "en_US.UTF-8",
        "BPE_LC_ALL" to "en_US.UTF-8",
        "BPE_LC_CTYPE" to "en_US.UTF-8",
        "BPE_APPEND_JAVA_TOOL_OPTIONS" to "-Duser.language=en -Duser.country=US -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8",
        "BPE_DELIM_JAVA_TOOL_OPTIONS" to " "
    )
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf("-Xlint:deprecation"))
}

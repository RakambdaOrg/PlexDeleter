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
}

configurations {
    compileOnly {
        extendsFrom(configurations["annotationProcessor"])
    }
}

ext["hibernate.version"] = "6.4.2.Final"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-mysql")

    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6:3.1.2.RELEASE")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")
    compileOnly("org.projectlombok:lombok")
    compileOnly(libs.jetbrainsAnnotations)

    testAnnotationProcessor("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")

    runtimeOnly("org.mariadb.jdbc:mariadb-java-client")

    developmentOnly("org.springframework.boot:spring-boot-devtools")
}

tasks {
    test {
        useJUnitPlatform()
    }

    bootBuildImage {
        environment = mapOf(
            "BP_NATIVE_IMAGE_BUILD_ARGUMENTS" to "-march=compatibility",
            "BP_DISABLE_SBOM" to "true",
            "BPE_LANG" to "C.UTF-8",
            "BPE_APPEND_JAVA_TOOL_OPTIONS" to "-Duser.language=en -Duser.country=US",
            "BPE_DELIM_JAVA_TOOL_OPTIONS" to " "
        )
    }

    graalvmNative {
        testSupport = false
    }

    bootJar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf("-Xlint:deprecation"))
}

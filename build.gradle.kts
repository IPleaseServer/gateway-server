import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.7.0"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21"
}

group = "site.iplease"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

dependencies {
    implementation ("org.springframework.boot:spring-boot-starter-actuator")
    testImplementation ("org.springframework.boot:spring-boot-starter-test")
    implementation ("org.springframework.boot:spring-boot-starter-webflux")
    implementation ("org.springframework.cloud:spring-cloud-starter-gateway")
    implementation ("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    implementation ("org.springframework.cloud:spring-cloud-starter-config")
    implementation ("org.springframework.cloud:spring-cloud-starter-bus-amqp")
    testImplementation ("io.projectreactor:reactor-test")
    implementation ("org.springdoc:springdoc-openapi-webflux-ui:1.5.10")
}

dependencyManagement {
    imports {
        val springCloudVersion = "2021.0.0"
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${springCloudVersion}")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.6.7"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21"
}

group = "site.iplease"
version = "1.1.0-RELEASE2"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

dependencies {
    //proto-libs
//    implementation(files("libs/proto-lib-1.0.1-RELEASE.jar"))
    //armeria
//    implementation(platform("com.linecorp.armeria:armeria-bom:1.16.0"))
//    implementation("com.linecorp.armeria:armeria")
//    implementation("com.linecorp.armeria:armeria-grpc")
//    implementation("com.linecorp.armeria:armeria-spring-boot2-webflux-starter")
    //grpc
//    implementation("io.grpc:grpc-protobuf:1.45.1")
//    implementation("io.grpc:grpc-stub:1.45.1")
//    implementation("com.salesforce.servicelibs:reactor-grpc-stub:1.2.3")
//    implementation("io.grpc:grpc-okhttp:1.0.1")
    //annotation
    compileOnly("javax.annotation:javax.annotation-api:1.3.2")
    compileOnly("jakarta.annotation:jakarta.annotation-api:2.0.0")
    //kotlin
    implementation ("io.projectreactor.kotlin:reactor-kotlin-extensions:1.1.6")
    //spring boot
    implementation ("org.springframework.boot:spring-boot-starter-actuator")
    implementation ("org.springframework.boot:spring-boot-starter-amqp")
    implementation ("org.springframework.boot:spring-boot-starter-webflux")
    //spring doc
    implementation ("org.springdoc:springdoc-openapi-webflux-ui:1.6.8")
    implementation("net.logstash.logback:logstash-logback-encoder:7.1.1")
    //spring cloud
    implementation ("org.springframework.cloud:spring-cloud-starter-gateway")
    implementation ("org.springframework.cloud:spring-cloud-starter-config")
    implementation ("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    //test
    testImplementation ("org.springframework.boot:spring-boot-starter-test")
    testImplementation ("io.projectreactor:reactor-test")
    //object mapper
    implementation ("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.3")
    implementation ("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.3")
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

tasks.getByName<Jar>("jar") {
    enabled = false
}
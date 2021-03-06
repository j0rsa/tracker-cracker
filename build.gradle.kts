import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import de.undercouch.gradle.tasks.download.Download
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion = "1.4.10"
val config4kVersion = "0.4.2"
val arrowVersion = "0.11.0"
val vertxVersion = "4.0.0-milestone5"
val exposedVersion = "0.25.1"
val junitJupiterEngineVersion = "5.4.2"

val mainVerticleName = "com.j0rsa.cracker.tracker.Main"
val watchForChange = "src/**/*"
val doOnChange = "./gradlew classes"

plugins {
    kotlin("jvm") version "1.4.10"
    kotlin("kapt") version "1.4.10"
    id("com.github.ben-manes.versions") version "0.31.0"
    id("com.adarshr.test-logger") version "2.1.0"
    id("com.gorylenko.gradle-git-properties") version "2.2.3"
    id("com.avast.gradle.docker-compose") version "0.13.2"
    id("com.palantir.docker") version "0.25.0"
    id("com.github.johnrengelman.shadow") version "6.0.0"
    id("org.flywaydb.flyway") version "6.5.5"
    id("de.undercouch.download") version "4.1.1"
}

group = "com.j0rsa.tracker-cracker"
version = "0.0.1"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation("org.flywaydb:flyway-core:6.5.5")
    implementation("org.postgresql:postgresql:42.2.9")
    implementation("com.zaxxer:HikariCP:3.4.2")
    implementation("io.arrow-kt:arrow-fx:$arrowVersion")
    implementation("io.arrow-kt:arrow-syntax:$arrowVersion")
    kapt("io.arrow-kt:arrow-meta:$arrowVersion")
    implementation("io.github.config4k:config4k:$config4kVersion")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")

    implementation("io.vertx:vertx-health-check:$vertxVersion")
    implementation("io.vertx:vertx-web:$vertxVersion")
    implementation("io.vertx:vertx-lang-kotlin-coroutines:$vertxVersion")
    implementation("io.vertx:vertx-lang-kotlin:$vertxVersion")
    implementation("io.vertx:vertx-web-api-contract:$vertxVersion")
    implementation("com.google.code.gson:gson:2.8.6")

    implementation("io.lettuce:lettuce-core:5.3.3.RELEASE")

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jodatime:$exposedVersion")
    implementation("io.jsonwebtoken:jjwt:0.9.1")
    implementation("org.slf4j:slf4j-api:1.7.25")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("ch.qos.logback:logback-core:1.2.3")

    implementation("org.apache.kafka:kafka-clients:2.6.0")

    testImplementation("io.mockk:mockk:1.9.3")
    testImplementation("io.vertx:vertx-junit5:$vertxVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitJupiterEngineVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterEngineVersion")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.20")
    testImplementation("org.jetbrains.spek:spek-api:1.1.5")
    testImplementation("org.jetbrains.spek:spek-junit-platform-engine:1.1.5")
    testImplementation("org.postgresql:postgresql:42.2.9")

    testImplementation("io.kotest:kotest-runner-junit5:4.2.5") // for kotest framework
    testImplementation("io.kotest:kotest-assertions-core:4.2.5") // for kotest core jvm assertions
    testImplementation("io.kotest:kotest-property:4.2.5") // for kotest property test
}

tasks {
    named<ShadowJar>("shadowJar") {
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Verticle" to mainVerticleName))
        }
        mergeServiceFiles {
            include("META-INF/services/io.vertx.core.spi.VerticleFactory")
        }
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    register<JavaExec>("run") {
        args = listOf(
			"run",
			mainVerticleName,
			"--redeploy=${watchForChange}",
			"--launcher-class=io.vertx.core.Launcher",
			"--on-redeploy=${doOnChange}"
		)
    }

    docker {
        dependsOn(this@tasks.test.get())
    }

    val composeUp = getByName("composeUp")
    val flywayMigrate = getByName("flywayMigrate")
    val flywayValidate = getByName("flywayValidate")
    val test by getting(Test::class) {
        dependsOn(composeUp)
        dependsOn(flywayMigrate)
        dependsOn(flywayValidate)
        useJUnitPlatform { }
        jvmArgs = listOf("-Duser.timezone=UTC")
    }

    val downloadSwagger = register<Download>("downloadSwaggerUi") {
        src(listOf("https://github.com/swagger-api/swagger-ui/archive/v3.25.0.zip"))
        dest(File(buildDir, "swaggerui.zip"))
        onlyIfModified(true)
    }

    val unpackSwagger = register<Copy>("unpackSwaggerUi") {
        dependsOn(downloadSwagger)
        from(zipTree(downloadSwagger.get().dest))
        into(File(buildDir, "swaggerui"))
    }

    val copySwagger = register<Copy>("copySwaggerUi") {
        dependsOn(unpackSwagger)
        from(File(unpackSwagger.get().destinationDir, "swagger-ui-3.25.0/dist"))
        into(File(buildDir, "resources/main/webroot"))

    }

    val replaceSpecToken = register<Copy>("replaceSpecToken") {
        dependsOn(copySwagger)
        from(File(unpackSwagger.get().destinationDir, "swagger-ui-3.25.0/dist/index.html"))
        into(File(buildDir, "resources/main/webroot/"))
        filter { line -> line.replace("https://petstore.swagger.io/v2/swagger.json", "spec.yaml") }
    }

    // For Intellij IDEA
    val copySwaggerToSrc = register<Copy>("copySwaggerToSrc") {
        dependsOn(replaceSpecToken)
        from(File(buildDir, "resources/main/webroot/"))
        into(File(projectDir, "src/main/resources/webroot/"))
    }
    // For Intellij IDEA
    val copySwaggerToOut = register<Copy>("copySwaggerToOut") {
        dependsOn(copySwaggerToSrc)
        from(File(buildDir, "resources/main/webroot/"))
        into(File(projectDir, "out/production/resources/webroot/"))
    }

    build {
        dependsOn(replaceSpecToken)
    }
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
    freeCompilerArgs = listOf(
        "-Xinline-classes",
        "-Xopt-in=kotlin.time.ExperimentalTime"
    )
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions.jvmTarget = "1.8"

val hash = Runtime.getRuntime().exec("git rev-parse --short=6 HEAD").inputStream.reader().use { it.readText() }.trim()
val projectTag = hash
val baseDockerName = "j0rsa/${project.name}"
val taggedDockerName = "$baseDockerName:$projectTag"

val baseDockerFile = file("$projectDir/src/main/docker/Dockerfile")
docker {
    val shadowJar: ShadowJar by tasks
    name = taggedDockerName
    setDockerfile(baseDockerFile)
    tag("DockerTag", "$baseDockerName:$projectTag")
    buildArgs(mapOf("JAR_FILE" to shadowJar.archiveFileName.get()))
    files(shadowJar.outputs, "$buildDir/resources/main/webroot/spec.yaml")
}

dockerCompose {
    useComposeFiles = listOf("docker-compose.yaml")
    captureContainersOutput = true
    forceRecreate = true
    dockerComposeWorkingDirectory = "src/main/docker"
    projectName = project.name
}

flyway {
    url = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost/postgres"
    user = System.getenv("DB_USER") ?: "postgres"
    password = System.getenv("DB_PASSWORD") ?: "postgres"
    locations = arrayOf("filesystem:src/main/resources/db/migration")
    baselineOnMigrate = true
}

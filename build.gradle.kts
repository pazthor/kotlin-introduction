import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestLogEvent.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin ("jvm") version "1.4.21"
  application
  id("com.github.johnrengelman.shadow") version "6.1.0"
}

group = "com.example"
version = "1.0.0-SNAPSHOT"

repositories {
  mavenCentral()
  jcenter()
}

val vertxVersion = "3.9.5"
val junitJupiterVersion = "5.7.0"

val mainVerticleName = "com.example.starter.MainVerticle"
val launcherClassName = "io.vertx.core.Launcher"

val watchForChange = "src/**/*"
val doOnChange = "${projectDir}/gradlew classes"

application {
  mainClassName = launcherClassName
}

dependencies {
  implementation(platform("io.vertx:vertx-stack-depchain:$vertxVersion"))
  implementation("io.vertx:vertx-config")
  implementation("io.vertx:vertx-web")
  implementation("io.vertx:vertx-service-discovery")
  implementation("io.vertx:vertx-circuit-breaker")
  implementation("io.vertx:vertx-lang-kotlin-coroutines")
  implementation("io.vertx:vertx-lang-kotlin")
  implementation(kotlin("stdlib-jdk8"))
  testImplementation("io.vertx:vertx-junit5")
  testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
  testImplementation("io.vertx:vertx-rx-java") // to be removed when uprading to 3.9.6 or 4.0.0, see https://github.com/vert-x3/vertx-junit5/issues/93
  testImplementation("io.vertx:vertx-rx-java2") // to be removed when uprading to 3.9.6 or 4.0.0, see https://github.com/vert-x3/vertx-junit5/issues/93
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = "11"

tasks.withType<ShadowJar> {
  archiveClassifier.set("fat")
  manifest {
    attributes(mapOf("Main-Verticle" to mainVerticleName))
  }
  mergeServiceFiles()
}

tasks.withType<Test> {
  useJUnitPlatform()
  testLogging {
    events = setOf(PASSED, SKIPPED, FAILED)
  }
}

tasks.withType<JavaExec> {
  args = listOf("run", mainVerticleName, "--redeploy=$watchForChange", "--launcher-class=$launcherClassName", "--on-redeploy=$doOnChange")
}

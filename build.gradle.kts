plugins {
    id("java")
}

group = "dev.kataray.brewery"
version = "1.1"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.jsinco.dev/releases")

}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.3-R0.1-SNAPSHOT")
    compileOnly("com.dre.brewery:BreweryX:3.4.5-SNAPSHOT#3")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

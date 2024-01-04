# repository-mirrors-gradle-plugin
A Gradle [init script plugin](https://docs.gradle.org/current/userguide/init_scripts.html#sec:init_script_plugins) that dynamically updates buildscript and project repositories to use the configured mirror URLs.

## Usage
The repository-mirrors-gradle-plugin is available in the [Gradle Plugin Portal](https://plugins.gradle.org/plugin/co.insecurity.repository-mirrors).

To apply the plugin to all projects on your workstation or build server automatically, update the Gradle init script below with your own Artifactory URL, and save it to `$HOME/.gradle/init.d/repositoryMirrors.gradle`.

```groovy
initscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        classpath "co.insecurity:repository-mirrors-gradle-plugin:0.4.0"
    }
}

apply plugin: co.insecurity.gradle.repository_mirrors.RepositoryMirrorsPlugin

allprojects {
    repositoryMirrors {
        artifactoryURL = 'https://repo.jfrog.org/artifactory'
        // read timeout in milliseconds; when readTimeout > 0, a warning will be emitted if exceeded while
        // reading response from Artifactory server and configured repos will be left unmodified;
        // otherwise, if readTimeout <= 0, wait for the server to respond indefinitely
        // (default: 5000ms)
        readTimeout = 10000
        // connection timeout in milliseconds; when connectTimeout > 0, a warning will be emitted if exceeded while
        // connecting to Artifactory server and configured repos will be left unmodified;
        // otherwise, if connectTimeout <= 0, wait for the connection to complete indefinitely
        // (default: 5000ms)
        connectTimeout = 5000
        failOnTimeout = false  // fail build on connection timeout to Artifactory server (default: false)
        removeDuplicates = true  // remove duplicate (i.e. same URL) Ivy and Maven repositories (default: false)
        removeMissing = false  // remove Ivy and Maven repositories which do not have a mirror (default: false)
        enabled = true  // when false, plugin execution will be skipped (default: false)
    }
}
```

Once this script is added, the plugin will automatically be applied to all Gradle projects during the configuration phase, and will update each buildscript and project repository URL with its mirror in Artifactory, if one exists.

## To Build
Without running tests:
```bash
./gradlew clean assemble
```

To run tests:
```bash
./gradlew clean test check
```


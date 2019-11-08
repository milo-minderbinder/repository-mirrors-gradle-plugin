# repository-mirrors-gradle-plugin
A Gradle [init script plugin](https://docs.gradle.org/current/userguide/init_scripts.html#sec:init_script_plugins) that automatically updates buildscript and project repositories to use the configured mirror URLs.

## Basic Usage
To apply the plugin to all projects on your workstation or build server automatically, update the Gradle init script below with your own Artifactory URL, and save it to `$HOME/init.d/repositoryMirrors.gradle`. The plugin will search the configured Artifactory server for all remote Ivy and Maven repositories it mirrors, and will automatically update all buildscript and project repository URLs with the URL of its mirror in Artifactory (where one exists).

```groovy
initscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        classpath "co.insecurity:repository-mirrors-gradle-plugin:0.1.0"
    }
}

apply plugin: co.insecurity.gradle.repository_mirrors.RepositoryMirrorsPlugin

allprojects {
    repositoryMirrors {
        artifactoryURL = 'https://artifactory.example.com/artifactory'
    }
    removeDuplicates = true  // remove duplicate (i.e. same URL) Ivy and Maven repositories (default: false)
}
```

## To Build
Without running tests:
```bash
./gradlew clean assemble
```

To run tests:
```bash
./gradlew clean test check
```


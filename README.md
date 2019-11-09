# repository-mirrors-gradle-plugin
A Gradle [init script plugin](https://docs.gradle.org/current/userguide/init_scripts.html#sec:init_script_plugins) that automatically updates buildscript and project repositories to use the configured mirror URLs.

## Usage
To apply the plugin to all projects on your workstation or build server automatically, update the Gradle init script below with your own Artifactory URL, and save it to `$HOME/.gradle/init.d/repositoryMirrors.gradle`.

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


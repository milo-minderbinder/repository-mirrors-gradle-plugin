# repository-mirrors-gradle-plugin
A Gradle Plugin that automatically updates buildscript and project repositories to use the configured mirror URLs.

## Basic Usage
Add the plugin to your `build.gradle`:
```groovy
plugins {
  id "co.insecurity.repository-mirrors" version "0.1.0"
}
```

Next, configure the plugin with the URL of an Artifactory server to use:
```groovy
repositoryMirrors {
    artifactoryURL = 'https://artifactory.example.com/artifactory'
}
```

The plugin will automatically search the configured Artifactory server for all remote Ivy and Maven repositories it mirrors, and will automatically update all buildscript and project repository URLs with the URL of its mirror in Artifactory (where one exists).

### Automatically Applying to All Projects
To apply the plugin to all projects on your workstation or build server automatically, update the Gradle init script below with your own Artifactory URL, and save it to `$HOME/init.d/repositoryMirrors.gradle`:
```groovy
initscript {
    dependencies {
        classpath "co.insecurity:repository-mirrors-gradle-plugin:0.1.0"
    }
}

apply plugin: co.insecurity.gradle.repository_mirrors.RepositoryMirrorsPlugin

allprojects {
    repositoryMirrors {
        artifactoryURL = 'https://artifactory.example.com/artifactory'
    }
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


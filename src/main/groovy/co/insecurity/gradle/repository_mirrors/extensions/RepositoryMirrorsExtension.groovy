package co.insecurity.gradle.repository_mirrors.extensions

import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

class RepositoryMirrorsExtension {
    Property<String> artifactoryURL
    ListProperty<String> artifactoryURLs
    Property<Boolean> removeDuplicates

    RepositoryMirrorsExtension(Project project) {
        artifactoryURL = project.objects.property(String)
        artifactoryURLs = project.objects.listProperty(String)
        removeDuplicates = project.objects.property(Boolean)
    }
}
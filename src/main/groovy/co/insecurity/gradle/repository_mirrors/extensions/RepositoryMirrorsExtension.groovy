package co.insecurity.gradle.repository_mirrors.extensions

import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

class RepositoryMirrorsExtension {
    public static final int DEFAULT_CONNECT_TIMEOUT = 5 * 1000;
    public static final int DEFAULT_READ_TIMEOUT = 5 * 1000;

    Property<String> artifactoryURL
    ListProperty<String> artifactoryURLs
    Property<Integer> connectTimeout
    Property<Integer> readTimeout
    Property<Boolean> failOnTimeout
    Property<Boolean> removeDuplicates
    Property<Boolean> removeMissing
    Property<Boolean> enabled

    RepositoryMirrorsExtension(Project project) {
        artifactoryURL = project.objects.property(String)
        artifactoryURLs = project.objects.listProperty(String)
        connectTimeout = project.objects.property(Integer)
        readTimeout = project.objects.property(Integer)
        failOnTimeout = project.objects.property(Boolean)
        removeDuplicates = project.objects.property(Boolean)
        removeMissing = project.objects.property(Boolean)
        enabled = project.objects.property(Boolean)
    }
}

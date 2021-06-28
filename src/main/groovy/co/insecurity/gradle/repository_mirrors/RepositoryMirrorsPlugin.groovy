package co.insecurity.gradle.repository_mirrors

import co.insecurity.gradle.repository_mirrors.extensions.RepositoryMirrorsExtension
import co.insecurity.gradle.repository_mirrors.tasks.RepositoryMirrorsReport
import groovy.json.JsonSlurper
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.ArtifactRepository
import org.gradle.api.artifacts.repositories.IvyArtifactRepository
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.Logger

import java.nio.charset.StandardCharsets

class RepositoryMirrorsPlugin implements Plugin<Gradle> {
    enum PackageType {
        MAVEN,
        IVY;

        public String toString() {
            return this.name().toLowerCase()
        }
    }

    private static final Logger log = ColorizedLogger.getLogger(RepositoryMirrorsPlugin.class)

    static final String PLUGIN_ID = 'co.insecurity.repository-mirrors'
    static final String EXTENSION_NAME = 'repositoryMirrors'

    static final String REMOTE_REPOS_ENDPOINT = 'api/repositories'

    private Map<PackageType, Map<String, String>> mirrors = new HashMap<>();

    @Override
    void apply(Gradle gradle) {
        log.lifecycle "applying ${PLUGIN_ID} plugin"
        RepositoryMirrorsExtension extension
        gradle.allprojects {Project project ->
            log.debug "creating ${project} ${EXTENSION_NAME} extension"
            extension = project.extensions.create(EXTENSION_NAME, RepositoryMirrorsExtension, project)
            project.tasks.create(RepositoryMirrorsReport.TASK_NAME, RepositoryMirrorsReport)
            configureRepositoryMirrors(project, extension)
        }
    }

    static Map<String, String> getMirrorsForType(String artifactoryURL, PackageType packageType) {
        URL reposAPIURL = new URL(String.format('%s/%s?type=remote&packageType=%s',
                                                artifactoryURL, REMOTE_REPOS_ENDPOINT, packageType.toString()))
        log.debug "fetching mirrors from ${reposAPIURL}"
        List repoList
        try {
            repoList = new JsonSlurper().parse(reposAPIURL, StandardCharsets.UTF_8.name()) as List
        } catch (Exception e) {
            log.warn(
                    "ignoring ${packageType} mirrors in ${artifactoryURL}; exception while fetching & parsing ${reposAPIURL}",
                    e)
            return Collections.emptyMap()
        }
        Map<String, String> mirrors = repoList.collectEntries {
            String remoteURL = it.url.replaceFirst('/$', '')
            String key = it.key
            String mirrorURL = "${artifactoryURL}/${key}"
            return [(remoteURL): mirrorURL]
        }
        log.debug "fetched ${mirrors.size()} mirrored repository URLs"
        return mirrors
    }

    Map<String, String> getMirrors(RepositoryMirrorsExtension extension, PackageType packageType) {
        if (this.mirrors.containsKey(packageType)) {
            return this.mirrors.get(packageType)
        }
        log.debug "fetching ${packageType} mirrors from configured Artifactory URL(s)"
        //extension.artifactoryURL.finalizeValue()
        //extension.artifactoryURLs.finalizeValue()
        final List<String> artifactoryURLs = (Collections.singleton(extension.artifactoryURL.get()) +
                extension.artifactoryURLs.getOrElse(new ArrayList<String>())
        ).collect {String artifactoryURL ->
            artifactoryURL.replaceFirst('/+$', '')
        }.unique()

        Map<String, String> packageMirrors = artifactoryURLs.collect {String artifactoryURL ->
            getMirrorsForType(artifactoryURL, packageType)
        }.collectEntries()
        this.mirrors.put(packageType, packageMirrors)
        return packageMirrors
    }

    void configureRepositoryMirrors(Project project, RepositoryMirrorsExtension extension) {
        Closure repoMirrorClosure = {Map<String, String> packageMirrors, ArtifactRepository repo ->
            if (!extension.enabled.getOrElse(true)) {
                return
            }
            log.debug "checking for mirror: ${repo.name} - ${repo.url}"
            String repoURL = repo.url.toString().replaceFirst('/$', '')
            if (packageMirrors.containsKey(repoURL)) {
                String mirrorURL = packageMirrors.get(repoURL)
                log.warn String.format(
                        "%sChanging ${repo.name} URL to artifactory mirror URL%s: ${repo.url} -> ${mirrorURL}",
                        ColorizedLogger.ANSIColor.YELLOW, ColorizedLogger.ANSIColor.RESET)
                repo.url = mirrorURL
            }
        }

        Closure duplicateRepoCheckClosure = {Set<String> repoURLs, ArtifactRepository repo ->
            if (!extension.enabled.getOrElse(true)) {
                return
            }
            if (extension.removeDuplicates.getOrElse(false)) {
                if (repoURLs.contains(repo.url.toString())) {
                    log.warn String.format(
                            "%sRemoving duplicate repository%s: ${repo.name} (${repo.url})",
                            ColorizedLogger.ANSIColor.BRIGHT_RED, ColorizedLogger.ANSIColor.RESET)
                    remove repo
                } else {
                    repoURLs.add(repo.url.toString())
                }
            }
        }

        Closure configClosure = {
            Map<PackageType, Set<String>> repoURLs = PackageType.values().collectEntries {PackageType packageType ->
                [(packageType): new HashSet<String>()]
            }
            withType(MavenArtifactRepository) {MavenArtifactRepository repo ->
                repoMirrorClosure(getMirrors(extension, PackageType.MAVEN), repo)
                duplicateRepoCheckClosure.rehydrate(
                        getDelegate(), getOwner(), getThisObject()
                )(repoURLs.get(PackageType.MAVEN), repo)
            }
            withType(IvyArtifactRepository) {IvyArtifactRepository repo ->
                repoMirrorClosure(getMirrors(extension, PackageType.IVY), repo)
                duplicateRepoCheckClosure.rehydrate(
                        getDelegate(), getOwner(), getThisObject()
                )(repoURLs.get(PackageType.IVY), repo)
            }
        }
        log.lifecycle "configuring ${project} buildscript repositories to use mirrors (where possible)"
        project.buildscript.repositories(configClosure)
        log.lifecycle "configuring ${project} repositories to use mirrors (where possible)"
        project.repositories(configClosure)
    }
}

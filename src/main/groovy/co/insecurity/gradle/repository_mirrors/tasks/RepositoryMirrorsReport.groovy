package co.insecurity.gradle.repository_mirrors.tasks

import co.insecurity.gradle.repository_mirrors.RepositoryMirrorsPlugin
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.ArtifactRepository
import org.gradle.api.artifacts.repositories.IvyArtifactRepository
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.tasks.TaskAction

class RepositoryMirrorsReport extends DefaultTask {
    static final String TASK_NAME = 'repositoryMirrorsReport'

    RepositoryMirrorsReport() {
        this.group = RepositoryMirrorsPlugin.PLUGIN_ID
    }

    @TaskAction
    void executeTask() {
        String reportContents = [
                'buildscript repositories': project.buildscript.repositories,
                'repositories'            : project.repositories
        ].collect {String repoHandlerName, RepositoryHandler repositories ->
            "${project} ${repoHandlerName}:\n\t" + repositories.
                    withType(ArtifactRepository).
                    findAll {ArtifactRepository r ->
                        if ((r instanceof MavenArtifactRepository) || (r instanceof IvyArtifactRepository)) {
                            return true
                        }
                        return false
                    }.
                    collect {ArtifactRepository r ->
                        "${r.name} - ${r.url}".toString()
                    }.
                    join('\n\t')
        }.join('\n\n')

        String output = """\
        ${'-' * 80}
        ${this.group} ${project} repository report
        ${'-' * 80}
        """.stripIndent() + reportContents
        println output
    }
}

package co.insecurity.gradle.repository_mirrors

import co.insecurity.gradle.repository_mirrors.tasks.RepositoryMirrorsReport
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome

import static co.insecurity.gradle.repository_mirrors.fixtures.RepositoryMirrorsPluginFixture.*


class RepositoryMirrorsPluginFunctionalTest extends AbstractFunctionalTest {
    protected static final List<String> DEFAULT_TASKS = [
            ":${RepositoryMirrorsReport.TASK_NAME}".toString()
    ].asUnmodifiable()

    def "plugin can be added with no repositories and no configuration"() {
        when:
        BuildResult result = buildWithDefaultTasks()

        then:
        result.output
        DEFAULT_TASKS.every {String taskPath ->
            result.task(taskPath).outcome == TaskOutcome.SUCCESS
        }
    }

    def "plugin can be added when buildscript repositories defined if configured in init script"() {
        given:
        initscriptFile << """\
            allprojects {
                project.${extensionSetArtifactoryURL()}
            }
        """.stripIndent()
        buildFile << buildscriptRepositories()

        when:
        BuildResult result = buildWithDefaultTasks()

        then:
        result.output
        DEFAULT_TASKS.every {String taskPath ->
            result.task(taskPath).outcome == TaskOutcome.SUCCESS
        }
    }

    def "plugin can be added when project repositories defined if configured in build file"() {
        given:
        buildFile << extensionSetArtifactoryURL()
        buildFile << projectRepositories()

        when:
        BuildResult result = buildWithDefaultTasks()

        then:
        result.output
        DEFAULT_TASKS.every {String taskPath ->
            result.task(taskPath).outcome == TaskOutcome.SUCCESS
        }
    }

    def "plugin can be added when buildscript and project repositories defined if configured in init script"() {
        given:
        initscriptFile << """\
            allprojects {
                project.${extensionSetArtifactoryURL()}
            }
        """.stripIndent()
        buildFile << buildscriptRepositories()
        buildFile << projectRepositories()

        when:
        BuildResult result = buildWithDefaultTasks()

        then:
        result.output
        DEFAULT_TASKS.every {String taskPath ->
            result.task(taskPath).outcome == TaskOutcome.SUCCESS
        }
    }

    private BuildResult buildWithDefaultTasks() {
        //println "initscript file:"
        //println initscriptFile.text
        //println "build file:"
        //println buildFile.text
        gradleRunner
                .withArguments(gradleRunner.getArguments() + DEFAULT_TASKS)
                .build()
    }
}

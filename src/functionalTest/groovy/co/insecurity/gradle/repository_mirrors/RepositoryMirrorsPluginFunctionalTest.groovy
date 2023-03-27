package co.insecurity.gradle.repository_mirrors

import co.insecurity.gradle.repository_mirrors.extensions.RepositoryMirrorsExtension
import co.insecurity.gradle.repository_mirrors.tasks.RepositoryMirrorsReport
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome
import org.mockserver.mock.Expectation
import org.mockserver.model.Delay
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse

import static co.insecurity.gradle.repository_mirrors.RepositoryMirrorsPlugin.EXTENSION_NAME
import static co.insecurity.gradle.repository_mirrors.fixtures.RepositoryMirrorsPluginFixture.*


class RepositoryMirrorsPluginFunctionalTest extends AbstractFunctionalTest {
    protected static final List<String> GRADLE_VERSIONS = [
            '8.0.2',
            '7.6.1',
            '6.9.4',
            '5.6.4',
    ].asUnmodifiable()
    protected static final List<String> DEFAULT_TASKS = [
            ":${RepositoryMirrorsReport.TASK_NAME}".toString()
    ].asUnmodifiable()

    def "plugin can be added with no repositories and no configuration"() {
        when:
        BuildResult result = buildWithDefaultTasks(v)

        then:
        assert result.output
        assert DEFAULT_TASKS.every {String taskPath ->
            result.task(taskPath).outcome == TaskOutcome.SUCCESS
        }

        where:
        v << GRADLE_VERSIONS
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
        BuildResult result = buildWithDefaultTasks(v)

        then:
        assert result.output
        assert DEFAULT_TASKS.every {String taskPath ->
            result.task(taskPath).outcome == TaskOutcome.SUCCESS
        }
        assert result.output.contains('bsm2 - http://localhost:8080/artifactory/repo1')

        when: 'build result is still successful if plugin fails to fetch mirrors'
        artifactoryServer.reset()
        artifactoryServer
                .when(HttpRequest.request())
                .respond(HttpResponse.notFoundResponse())
        result = buildWithDefaultTasks(v)

        then:
        assert result.output
        assert DEFAULT_TASKS.every {String taskPath ->
            result.task(taskPath).outcome == TaskOutcome.SUCCESS
        }
        assert result.output.contains('bsm2 - https://repo1.maven.org/maven2')

        where:
        v << GRADLE_VERSIONS
    }

    def "plugin can be added when project repositories defined if configured in build file"() {
        given:
        buildFile << extensionSetArtifactoryURL()
        buildFile << projectRepositories()

        when:
        BuildResult result = buildWithDefaultTasks(v)

        then:
        assert result.output
        assert DEFAULT_TASKS.every {String taskPath ->
            result.task(taskPath).outcome == TaskOutcome.SUCCESS
        }
        assert result.output.contains('pi1 - http://localhost:8080/artifactory/jcenter-ivy')

        when: 'build result is still successful if plugin fails to fetch mirrors'
        artifactoryServer.reset()
        artifactoryServer
                .when(HttpRequest.request())
                .respond(HttpResponse.notFoundResponse())
        result = buildWithDefaultTasks(v)

        then:
        assert result.output
        assert DEFAULT_TASKS.every {String taskPath ->
            result.task(taskPath).outcome == TaskOutcome.SUCCESS
        }
        assert result.output.contains('pi1 - https://jcenter.bintray.com')

        where:
        v << GRADLE_VERSIONS
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
        BuildResult result = buildWithDefaultTasks(v)

        then:
        assert result.output
        assert DEFAULT_TASKS.every {String taskPath ->
            result.task(taskPath).outcome == TaskOutcome.SUCCESS
        }
        assert result.output.contains('bsm2 - http://localhost:8080/artifactory/repo1')
        assert result.output.contains('pi1 - http://localhost:8080/artifactory/jcenter-ivy')

        when: 'build result is still successful if plugin fails to fetch mirrors'
        artifactoryServer.reset()
        artifactoryServer
                .when(HttpRequest.request())
                .respond(HttpResponse.notFoundResponse())
        result = buildWithDefaultTasks(v)

        then:
        assert result.output
        assert DEFAULT_TASKS.every {String taskPath ->
            result.task(taskPath).outcome == TaskOutcome.SUCCESS
        }
        assert result.output.contains('bsm2 - https://repo1.maven.org/maven2')
        assert result.output.contains('pi1 - https://jcenter.bintray.com')

        where:
        v << GRADLE_VERSIONS
    }

    def "plugin will warn and continue without changing repo URLs when read timeout exceeded"() {
        given:
        initscriptFile << """\
            allprojects {
                project.${extensionSetArtifactoryURL()}
                project.${EXTENSION_NAME} {
                    readTimeout = 1000
                    connectTimeout = -1
                }
            }
        """.stripIndent()
        buildFile << buildscriptRepositories()
        buildFile << projectRepositories()

        when:
        Expectation[] expectations = artifactoryServer.retrieveActiveExpectations(null).each {Expectation expectation ->
            expectation.getHttpResponse()
                    .withDelay(Delay.milliseconds(RepositoryMirrorsExtension.DEFAULT_READ_TIMEOUT * 2))
        }
        artifactoryServer.upsert(expectations)
        BuildResult result = buildWithDefaultTasks(v)

        then:
        assert result.output
        assert DEFAULT_TASKS.every {String taskPath ->
            result.task(taskPath).outcome == TaskOutcome.SUCCESS
        }
        assert result.output.contains('bsm2 - https://repo1.maven.org/maven2')
        assert result.output.contains('pi1 - https://jcenter.bintray.com')

        where:
        v << GRADLE_VERSIONS
    }

    private BuildResult buildWithDefaultTasks(String version) {
        //println "initscript file:"
        //println initscriptFile.text
        //println "build file:"
        //println buildFile.text
        gradleRunner
                .withGradleVersion(version)
                .withArguments(gradleRunner.getArguments() + DEFAULT_TASKS)
                .build()
    }
}

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
            '8.5',
            '8.0.2',
            '7.6.3',
            '6.9.4',
    ].asUnmodifiable()
    protected static final List<String> DEFAULT_TASKS = [
            ":${RepositoryMirrorsReport.TASK_NAME}".toString()
    ].asUnmodifiable()

    def setup() {
        gradleRunner
                .withArguments(gradleRunner.getArguments() + DEFAULT_TASKS)
    }

    def "plugin can be added with no repositories and no configuration"() {
        given:
        gradleRunner.withGradleVersion(v)

        when:
        BuildResult result = gradleRunner.build()

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
        gradleRunner.withGradleVersion(v)
        initscriptFile << """\
            allprojects {
                project.${extensionSetArtifactoryURL()}
            }
        """.stripIndent()
        buildFile << buildscriptRepositories()

        when:
        BuildResult result = gradleRunner.build()

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
        result = gradleRunner.build()

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
        gradleRunner.withGradleVersion(v)
        buildFile << extensionSetArtifactoryURL()
        buildFile << projectRepositories()

        when:
        BuildResult result = gradleRunner.build()

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
        result = gradleRunner.build()

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
        gradleRunner.withGradleVersion(v)
        initscriptFile << """\
            allprojects {
                project.${extensionSetArtifactoryURL()}
            }
        """.stripIndent()
        buildFile << buildscriptRepositories()
        buildFile << projectRepositories()

        when:
        BuildResult result = gradleRunner.build()

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
        result = gradleRunner.build()

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
        gradleRunner.withGradleVersion(v)
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
        BuildResult result = gradleRunner.build()

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

    def "plugin will throw exception when failOnTimeout property is set to true and read timeout exceeded"() {
        given:
        gradleRunner.withGradleVersion(v)
        initscriptFile << """\
            allprojects {
                project.${extensionSetArtifactoryURL()}
                project.${EXTENSION_NAME} {
                    readTimeout = 1000
                    connectTimeout = -1
                    failOnTimeout = true
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
        BuildResult result = gradleRunner.buildAndFail()

        then:
        assert result.output
        assert result.output.contains('java.net.SocketTimeoutException')

        where:
        v << GRADLE_VERSIONS
    }

    def "plugin will remove buildscript and project repositories without mirrors when removeMissing property is true"() {
        given:
        gradleRunner.withGradleVersion(v)
        initscriptFile << """\
            allprojects {
                project.${extensionSetArtifactoryURL()}
                project.${EXTENSION_NAME} {
                    removeMissing = true
                }
            }
        """.stripIndent()
        buildFile << buildscriptRepositories()
        buildFile << projectRepositories()


        when:
        BuildResult result = gradleRunner.build()

        then:
        assert result.output
        assert DEFAULT_TASKS.every {String taskPath ->
            result.task(taskPath).outcome == TaskOutcome.SUCCESS
        }
        assert [
                'bsm1 - file:/Users/cpassarello16/.m2/repository/',
                'bsm3 - https://repo.maven.apache.org/maven2/',
                'bsm5 - https://repo.maven.apache.org/maven2/',
                'pm2 - https://repo.maven.apache.org/maven2/',
        ].every {String s ->
            !result.output.contains(s)
        }
        assert [
                'bsm2 - http://localhost:8080/artifactory/repo1',
                'bsi1 - http://localhost:8080/artifactory/jcenter-ivy',
                'bsm4 - http://localhost:8080/artifactory/jcenter',
                'bsi2 - http://localhost:8080/artifactory/jcenter-ivy',
                'bsm6 - http://localhost:8080/artifactory/repo1',
                'bsm7 - http://localhost:8080/artifactory/internal-maven',
                'pm1 - http://localhost:8080/artifactory/repo1',
                'pi1 - http://localhost:8080/artifactory/jcenter-ivy',
                'pm3 - http://localhost:8080/artifactory/repo1',
                'pi2 - http://localhost:8080/artifactory/jcenter-ivy',
                'pm4 - http://localhost:8080/artifactory/jcenter',
                'pi3 - http://localhost:8080/artifactory/internal-ivy',
        ].every {String s ->
            result.output.contains(s)
        }

        where:
        v << GRADLE_VERSIONS
    }
}

package co.insecurity.gradle.repository_mirrors

import co.insecurity.gradle.repository_mirrors.RepositoryMirrorsPlugin.PackageType
import co.insecurity.gradle.repository_mirrors.tasks.RepositoryMirrorsReport
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import spock.lang.Specification

import static org.mockserver.integration.ClientAndServer.startClientAndServer


class RepositoryMirrorsPluginFunctionalTest extends Specification {
    private static final int MOCK_SERVER_PORT = 8080
    private static final String ARTIFACTORY_URL = "http://localhost:${MOCK_SERVER_PORT}/artifactory"

    static final String BUILDSCRIPT_REPOSITORIES = """\
        buildscript {
            println 'in buildscript closure'
            repositories {
                mavenLocal {name 'bsm1'}
                maven {
                    name 'bsm2'
                    url 'https://repo1.maven.org/maven2'
                }
                ivy {
                    name 'bsi1'
                    url 'https://jcenter.bintray.com'
                }
                mavenCentral {name 'bsm3'}
                jcenter {name 'bsm4'}
                ivy {
                    name 'bsi2'
                    url 'https://jcenter.bintray.com'
                }
                mavenCentral {name 'bsm5'}
                maven {
                    name 'bsm6'
                    url '${ARTIFACTORY_URL}/repo1'
                }
            }
        }
        """.stripIndent()
    static final String REPOSITORIES = """\
        repositories {
            maven {
                name 'm1'
                url '${ARTIFACTORY_URL}/repo1'
            }
            ivy {
                name 'i1'
                url 'https://jcenter.bintray.com'
            }
            mavenCentral {name 'm2'}
            maven {
                name 'm3'
                url 'https://repo1.maven.org/maven2'
            }
            ivy {
                name 'i2'
                url 'https://jcenter.bintray.com/'
            }
            jcenter {name 'm4'}
        }
        """.stripIndent()
    @Rule
    final TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile
    File initscriptFile
    File propertiesFile
    GradleRunner gradleRunner
    private ClientAndServer mockServer

    private void copyResourceFileIntoProjectDir(String resourceFileName, String targetFileName) {
        def resourceFileContent = new File(getClass().getClassLoader().getResource(resourceFileName).toURI()).text
        def targetDirectory = new File(testProjectDir.root, targetFileName).parentFile
        targetDirectory.mkdirs()
        def targetFile = testProjectDir.newFile(targetFileName)
        targetFile << resourceFileContent
    }

    private String getResourceFileContent(String resourceFileName) {
        File resourceFile = new File(getClass().getClassLoader().getResource(resourceFileName).toURI())
        return resourceFile.getText()
    }

    private String getPluginClasspath() {
        Properties properties = new Properties()
        def pluginClasspathResource = getClass().classLoader.getResource('plugin-under-test-metadata.properties')
        if (pluginClasspathResource == null) {
            throw new IllegalStateException("Did not find plugin classpath resource, run `pluginUnderTestMetadata` build task.")
        }
        pluginClasspathResource.withInputStream {properties.load(it)}
        List<File> pluginClasspath = properties.getProperty('implementation-classpath').split(':').collect {
            new File(it)
        }
        String pluginClasspathString = pluginClasspath.collect {"'${it.canonicalPath}'"}.join(', ')
        return pluginClasspathString
    }

    private void setupMockServer() {
        mockServer = startClientAndServer(MOCK_SERVER_PORT)
        PackageType.values().each {PackageType packageType ->
            String response = getResourceFileContent("mock-remote-${packageType}-response.json")
            mockServer
                    .when(
                            HttpRequest.request()
                                    .withMethod('GET')
                                    .withPath("/artifactory/${RepositoryMirrorsPlugin.REMOTE_REPOS_ENDPOINT}")
                                    .withQueryStringParameter('type', 'remote')
                                    .withQueryStringParameter('packageType', packageType.toString())
                    )
                    .respond(
                            HttpResponse.response()
                                    .withStatusCode(200)
                                    .withBody(response)
                    )
        }
    }

    def setup() {
        String pluginClasspathString = getPluginClasspath()

        initscriptFile = testProjectDir.newFile('init.gradle')
        initscriptFile.append """\
            println 'in init.gradle'
            initscript {
                dependencies {
                    classpath files(${pluginClasspathString})
                }
            }

            apply plugin: co.insecurity.gradle.repository_mirrors.RepositoryMirrorsPlugin
            allprojects {
                ${RepositoryMirrorsPlugin.EXTENSION_NAME} {
                    artifactoryURL = '${ARTIFACTORY_URL}'
                    removeDuplicates = true
                }
            }
        """.stripIndent()

        buildFile = testProjectDir.newFile('build.gradle')
        buildFile.append """\
            println 'in build.gradle'
            apply plugin: 'java'
        """.stripIndent()

        propertiesFile = testProjectDir.newFile('gradle.properties')
        propertiesFile.append """\
            #${RepositoryMirrorsPlugin.EXTENSION_NAME}.artifactoryURL = ${ARTIFACTORY_URL}
        """

        gradleRunner = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments([
                        '-i',
                        '-S',
                        '-I', new File(getClass().classLoader.getResource('testinit.gradle').toURI()).canonicalPath,
                        '-I', initscriptFile.getCanonicalPath(),
                        'dependencies', '--configuration=compile',
                ])
                .withPluginClasspath()
                .withDebug(true)
                .forwardOutput()
        //.withGradleVersion('2.14.1')
        setupMockServer()
    }

    def cleanup() {
        mockServer.stop()
    }

    def "plugin can be added with no repositories"() {
        when:
        def result = gradleRunner
                .withArguments(gradleRunner.getArguments() + ['tasks', '--all'])
                .build()

        then:
        result.output
    }

    def "plugin can be added when buildscript repositories defined"() {
        when:
        buildFile.append """\
            ${BUILDSCRIPT_REPOSITORIES}""".stripIndent()
        def result = gradleRunner
                .withArguments(gradleRunner.getArguments() + [RepositoryMirrorsReport.TASK_NAME])
                .build()

        then:
        result.output
    }

    def "plugin can be added when project repositories defined"() {
        when:
        buildFile.append """\
            ${REPOSITORIES}""".stripIndent()
        def result = gradleRunner
                .withArguments(gradleRunner.getArguments() + [RepositoryMirrorsReport.TASK_NAME])
                .build()

        then:
        result.output
    }

    def "plugin can be added when buildscript and project repositories defined"() {
        when:
        buildFile.append """\
            ${BUILDSCRIPT_REPOSITORIES}
            ${REPOSITORIES}""".stripIndent()
        def result = gradleRunner
                .withArguments(gradleRunner.getArguments() + [RepositoryMirrorsReport.TASK_NAME])
                .build()

        then:
        result.output
    }
}

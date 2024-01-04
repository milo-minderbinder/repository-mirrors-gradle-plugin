package co.insecurity.gradle.repository_mirrors

import org.gradle.testkit.runner.GradleRunner
import org.mockserver.configuration.ConfigurationProperties
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import spock.lang.Specification
import spock.lang.TempDir

import static org.mockserver.integration.ClientAndServer.startClientAndServer


abstract class AbstractFunctionalTest extends Specification {
    public static final int MOCK_SERVER_PORT = 8080
    public static final String ARTIFACTORY_URL = "http://localhost:${MOCK_SERVER_PORT}/artifactory"

    @TempDir
    File projectDir
    File buildFile
    File initscriptFile
    File propertiesFile
    GradleRunner gradleRunner
    ClientAndServer artifactoryServer

    def setup() {
        buildFile = new File(projectDir, 'build.gradle')
        initscriptFile = new File(projectDir, 'init.gradle')
        propertiesFile = new File(projectDir, 'gradle.properties')
        setupInitscriptFile()
        setupBuildFile()
        setupPropertiesFile()
        setupMockServer()
        gradleRunner = GradleRunner.create()
                .withProjectDir(projectDir)
                .withArguments([
                        '-i',
                        '-S',
                        '-I', getResourceFile('testinit.gradle').canonicalPath,
                        '-I', initscriptFile.getCanonicalPath(),
                ])
                .withPluginClasspath()
                .withDebug(true)
                .forwardOutput()
    }

    def cleanup() {
        artifactoryServer.stop()
    }

    protected void setupInitscriptFile() {
        final String pluginClasspath = getPluginClasspath()
        initscriptFile.append """\
            println "in init.gradle with gradle version: \${gradle.gradleVersion}"
            initscript {
                dependencies {
                    classpath files(${pluginClasspath})
                }
            }

            apply plugin: ${RepositoryMirrorsPlugin.class.getCanonicalName()}
        """.stripIndent()
    }

    protected void setupBuildFile() {
        buildFile.append """\
            println "in build.gradle with gradle version: \${gradle.gradleVersion}"
            apply plugin: 'java'
        """.stripIndent()
    }

    protected void setupPropertiesFile() {
        propertiesFile.append """\
            #${RepositoryMirrorsPlugin.EXTENSION_NAME}.artifactoryURL = ${ARTIFACTORY_URL}
        """
    }

    protected void setupMockServer() {
        ConfigurationProperties.enableCORSForAllResponses(false)
        ConfigurationProperties.enableCORSForAPI(false)
        artifactoryServer = startClientAndServer(MOCK_SERVER_PORT)
        RepositoryMirrorsPlugin.PackageType.values().each {RepositoryMirrorsPlugin.PackageType packageType ->
            String response = getResourceFile("mock-remote-${packageType}-response.json").text
            artifactoryServer
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


    protected void copyResourceFileIntoProjectDir(String resourceFileName, String targetFileName) {
        def resourceFileContent = new File(getClass().getClassLoader().getResource(resourceFileName).toURI()).text
        File targetFile = new File(projectDir, targetFileName)
        targetFile.text = resourceFileContent
    }

    protected File getResourceFile(String resourceFileName) {
        File resourceFile = new File(getClass().getClassLoader().getResource(resourceFileName).toURI())
        return resourceFile
    }

    protected String getPluginClasspath() {
        Properties properties = new Properties()
        def pluginClasspathResource = getClass().classLoader.getResource('plugin-under-test-metadata.properties')
        if (pluginClasspathResource == null) {
            throw new IllegalStateException(
                    "Did not find plugin classpath resource, run `pluginUnderTestMetadata` build task.")
        }
        pluginClasspathResource.withInputStream {
            properties.load(it)
        }
        List<File> pluginClasspath = properties.getProperty('implementation-classpath').split(':').collect {
            new File(it)
        }
        String pluginClasspathString = pluginClasspath.collect {
            "'${it.canonicalPath}'"
        }.join(', ')
        return pluginClasspathString
    }
}

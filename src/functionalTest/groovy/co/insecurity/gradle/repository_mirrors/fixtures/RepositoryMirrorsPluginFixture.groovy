package co.insecurity.gradle.repository_mirrors.fixtures

import static co.insecurity.gradle.repository_mirrors.AbstractFunctionalTest.ARTIFACTORY_URL
import static co.insecurity.gradle.repository_mirrors.RepositoryMirrorsPlugin.EXTENSION_NAME

final class RepositoryMirrorsPluginFixture {
    private RepositoryMirrorsPluginFixture() {
    }

    static String extensionSetArtifactoryURL() {
        "${EXTENSION_NAME}.artifactoryURL = '${ARTIFACTORY_URL}'\n"
    }

    static String buildscriptRepositories() {
        """\
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
    }

    static String projectRepositories() {
        """\
            repositories {
                maven {
                    name 'pm1'
                    url '${ARTIFACTORY_URL}/repo1'
                }
                ivy {
                    name 'pi1'
                    url 'https://jcenter.bintray.com'
                }
                mavenCentral {name 'pm2'}
                maven {
                    name 'pm3'
                    url 'https://repo1.maven.org/maven2'
                }
                ivy {
                    name 'pi2'
                    url 'https://jcenter.bintray.com/'
                }
                jcenter {name 'pm4'}
            }
        """.stripIndent()
    }
}

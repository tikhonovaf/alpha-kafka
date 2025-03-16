import groovy.transform.Field

@Field private Registry = null

def init(registry) {
    Registry = registry
    Registry.add('ssh://git@git.moscow.alfaintra.net/ufrsapr/ufr-sapr-proposals-api.git') {
        javaVersion = '17'
    }
    Registry.add('ssh://git@git.moscow.alfaintra.net/ufrsapr/ufr-sapr-settings.git') {
        javaVersion = '17'
    }
    Registry.add('ssh://git@git.moscow.alfaintra.net/ufiprofile/iprofile-gateway-api.git') {
        javaVersion = '17'
    }
    Registry.add('ssh://git@git.moscow.alfaintra.net/ufr-odstr/ufr-odstr-komod-api.git') {
        javaVersion = '17'
    }
    Registry.add('ssh://git@git.moscow.alfaintra.net/ufr/ufr-vertx-oauth-gateway.git') {
        javaVersion = '17'
    }
    Registry.add('ssh://git@git.moscow.alfaintra.net/ufrsandbox/ufr-jenkins-pipeline.git') {
        javaVersion = '8'
    }
}

return this

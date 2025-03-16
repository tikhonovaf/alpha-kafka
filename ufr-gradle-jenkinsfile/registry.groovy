import groovy.transform.Field

enum JavaVersion {
    V8('8', 'mesos-platform'),
    V151('jdk151', 'mesos-platform-jdk151'),
    V171('jdk171', 'mesos-platform-jdk171'),
    V11('11', 'mesos-platform-java11'),
    V15('15', 'mesos-platform-java15'),
    V17('17', 'mesos-platform-jdk17')
    String version
    String label

    JavaVersion(String version, String label) {
        this.version = version
        this.label = label
    }

    static JavaVersion ofVersion(String version) {
        def res = values().find { it.version == version}
        if (res == null) {
            throw new Exception("Unknown java version: $version")
        }
        return res
    }
}

class Repository {
    String id
    String sshURL
    String project
    String name

    Repository(String sshURL) {
        def sshParts = sshURL.split('/')
        this.sshURL = sshURL
        project = sshParts[3].toLowerCase()
        name = sshParts[4].toLowerCase().substring(0, sshParts[4].length() - 4)
        id = "$project/$name"
    }

    static void validateURL(String sshURL) {
        if (!sshURL.startsWith('ssh://') || !sshURL.endsWith('.git')) {
            throw new Exception("Not ssh url: $sshURL")
        }
        def sshParts = sshURL.split('/')
        if (sshParts.length != 5) {
            throw new Exception("Malformed ssh url: $sshURL")
        }
    }

    String toString() {
        return "Repository{sshURL='$sshURL', project='$project', name='$name'}"
    }
}

class RepositorySettings {
    Repository repository
    JavaVersion javaVersion = JavaVersion.V11

    String toString() {
        return "RepositorySettings{repository='${repository.toString()}', javaVersion='${javaVersion.toString()}'}"
    }
}

class RepositoryConfigs {
    String javaVersion = '11'

    void configure(RepositorySettings settings) {
        settings.javaVersion = JavaVersion.ofVersion(javaVersion)
    }

    String toString() {
        return "RepositoryConfigs{javaVersion='$javaVersion'}"
    }
}

@Field
Map<String, RepositorySettings> settingsRegistry = [:]

void add(String sshURL, Closure configure) {
    try {
        Repository.validateURL(sshURL)
        def repository = new Repository(sshURL)
        def settings = new RepositorySettings(repository: repository)

        def configs = new RepositoryConfigs()
        configure.resolveStrategy = Closure.DELEGATE_ONLY
        configure.delegate = configs
        configure.call()

        configs.configure(settings)
        settingsRegistry.put(repository.id, settings)
    } catch (Exception e) {
        println("Error while configuring ${sshURL}: ${e.toString()}")
    }
}

RepositorySettings ofRepository(String sshURL) {
    def repository = new Repository(sshURL)
    return settingsRegistry[repository.id] ?: new RepositorySettings(repository: repository)
}

return this
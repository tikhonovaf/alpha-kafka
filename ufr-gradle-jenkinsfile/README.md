# Общий Jenkinsfile Единого Фронта для сборок с помощью Gradle

## Как выбрать лейбл для сборки

Для вашего проекта можно изменить образ в котором будет происходить сборка в Jenkins. **По умолчанию лейбл mesos-platform-java11**.

Поддерживаемые лейблы: mesos-platform, mesos-platform-jdk151, mesos-platform-jdk171, mesos-platform-java11, mesos-platform-java15, mesos-platform-jdk17

Чтобы изменить лейбл для конкретного репозитория, необходимо в файле settings.groovy добавить ssh ссылку на свой репозиторий и желаемую версию java:

```
'8'      = mesos-platform
'jdk151' = mesos-platform-jdk151
'jdk171' = mesos-platform-jdk171
'11'     = mesos-platform-java11
'15'     = mesos-platform-java15
'17'     = mesos-platform-jdk17
```
Есть несколько способов определения лейбла:
1. Добавление своего репозитория в settings.groovy находящегося в репозитории ufr-gradle-jenkinsfile;
2. Создание settings.groovy в корне своего репозитория
По умолчанию скрипт проверяет наличие данного файла в репозитории собираемого сервиса, и если не находит его - берет данные из ufr-gradle-jenkinsfile.

Пример добавления репозитория в settings.groovy находящийся в репозитории ufr-gradle-jenkinsfile:

```
Registry.add('ssh://git@git-platform.moscow.alfaintra.net/sandboxap/gradle-sonar-test.git') {
        javaVersion = '17'
    }
```

Шаблон для создания собственного settings.groovy в корне репозитория:
```
import groovy.transform.Field

@Field private Registry = null

def init(registry) {
    Registry = registry
    Registry.add('<ссылка на репозиторий вида ssh://>') {
        javaVersion = '<требуемая версия java>'
    }
}

return this
```

## Как добавить отображение документации в Альфа-платформе
Для отображения ссылки на документацию в платформе неообходимо наличие самой документации в проекте.

По умолчанию, без дополнительных настроек, пайплан ищет asciidoc документацию в проекте. Если в проекте нет документации и нет ссылок на документацию, то в платформе появится ссылка на данную инструкцию.

Для этого должно быть свойство у gradle `docsDir` - приходит к нам с новыми версиями `microservice-configurer`. Проверить его наличие у проекта можно командой `./gradlew -s --quiet properties | grep "^docsDir: "` в корне проекта.

Второе свойство, необходимое для формирования ссылки `group` - проставляется руками. Команда для проверки `./gradlew -s --quiet properties | grep "^group: "` в корне проекта.

Когда все свойства у проекта присутсвуют и в ходе сборки проекта формируется директория `build/docs`, то данная директория будет заархивирована и размещена в binary по пути:
`${docsRepo}/${docsGroup}/${packageName}/docs/${version}/*docs.zip`, где:
* `docsRepo`    - принимает значения eco-mvn-releases или eco-mvn-snapshots в зависимости от типа вашего артефакта
* `docsGroup`   - принимает значение свойства `group`. Например `ru.alfabank.ufr.aglist`
* `packageName` - принимает значение имени проекта. Например `ufr-aglist-geo-api`
* `version`     - принимает значение версии собираемого артефакта.

Далее этому артефакту документации в числе прочих, будут проставлены property в binary:
* `platform.docs-link` - Итоговая ссылка, которая будет отображаться в Альфа-платформе. Она же указывается в артефакт самого билда.
* `platform.label=DOC` - Лейбл для платформы, указывающий что артефакт содержит документацию.

Итоговая ссылка на документацию ожидает найти в архиве `asciidoc/index.html` 

P.S. Общая инструкция от платформы размещена [здесь](http://confluence.moscow.alfaintra.net/pages/viewpage.action?pageId=1166209728)
### Добавление произвольной ссылки на документацию
Имеется возможность добавлять произвольные ссылки на документацию. Например, на confluence

Ссылка размещается в файле `platform-config.yml`, размещенном в корне проекта, в следующем виде:
```
docs_type: confluence
properties:
  - platform.docs-link: http://confluence.moscow.alfaintra.net/display/ALFAGO/ufr-aglist-geo-api
```
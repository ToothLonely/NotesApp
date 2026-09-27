# Приложение NotesApp

NotesApp — Android-приложение для работы с заметками и задачами. Основные данные
хранятся локально в приложении используется offline-first подход.
Сеть используется только для функций GigaChat: преобразования голосового ввода в
краткое название задачи и получения баланса токенов.

В приложении реализована следующая функциональность:

- создание, редактирование и удаление заметок и задач;
- добавление изображения из файлов устройства или с камеры;
- голосовой ввод текста заметок или задач, а также использование API GigaChat для последующей обработки;
- пагинация для отображения заметок при прокрутке;
- отправка заметки в другие приложения с использованием Intent;
- светлая, тёмная и системная темы и несколько вариантов цветовых схем;
- анимации при переходе между экранами и при открытии заметки, анимированный splash screen;

## Архитектура

Проект построен по принципам Now in Android и разделён на независимые Gradle-модули.
В presentation-слое используются MVVM, однонаправленный поток данных и неизменяемое
состояние экрана через `StateFlow`. UI собирает состояние с учётом жизненного цикла.

Room выступает единым источником истины для заметок и задач. Изменения данных
передаются в интерфейс реактивно через Kotlin Flow. Репозитории скрывают детали
локального хранения, сети, голосового ввода и работы с изображениями, а зависимости
между слоями связываются через Koin.

## Стек

| Категория | Технология |
|---|---|
| Architecture | ![MVVM](https://img.shields.io/badge/MVVM-6A1B9A) ![Multi-module](https://img.shields.io/badge/Multi--module-455A64) |
| Language | ![Kotlin](https://img.shields.io/badge/Kotlin-2.3.21-7F52FF?logo=kotlin&logoColor=white) |
| UI | ![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white) |
| Navigation | ![Navigation 3](https://img.shields.io/badge/Navigation-3-008577) |
| Database | ![Room](https://img.shields.io/badge/Room-2.8.4-C62828) |
| Local data | ![DataStore](https://img.shields.io/badge/DataStore-1.2.1-5E35B1) |
| DI | ![Koin](https://img.shields.io/badge/Koin-4.2.2-F9A825) |
| Network | ![Retrofit](https://img.shields.io/badge/Retrofit-3.0.0-48B983) ![OkHttp](https://img.shields.io/badge/OkHttp-5.3.0-3E4348) |
| Concurrency | ![Coroutines](https://img.shields.io/badge/Coroutines-1.11.0-7F52FF) ![Flow](https://img.shields.io/badge/Flow-reactive-1976D2) |
| Serialization | ![Kotlinx Serialization](https://img.shields.io/badge/Kotlinx%20Serialization-1.11.0-7F52FF) |
| AI | ![GigaChat](https://img.shields.io/badge/GigaChat-API-21A038) |
| Testing | ![JUnit](https://img.shields.io/badge/JUnit-4.13.2-25A162?logo=junit5&logoColor=white) ![Koin Verify](https://img.shields.io/badge/Koin-verify-F9A825) |

## Структура проекта

| Модуль | Назначение |
|---|---|
| `:app` | Корень композиции, Application, Activity, Navigation 3, splash screen и нижняя навигация |
| `:core:domain` | Общие доменные модели и контракты репозиториев |
| `:core:data` | Room, DAO, DataStore, Retrofit, OkHttp, GigaChat, голосовой ввод и реализации общих репозиториев |
| `:core:designsystem` | Тема, дизайн-токены, иконки и переиспользуемые Compose-компоненты |
| `:feature:notes:api` | Публичные маршруты раздела заметок |
| `:feature:notes:impl` | Список и редактор заметок, поиск, изображения, голосовой ввод и локальные репозитории |
| `:feature:tasks:api` | Публичные маршруты раздела задач |
| `:feature:tasks:impl` | Список задач, фильтры, поиск, голосовое создание и локальный data-слой |
| `:feature:settings:api` | Публичные маршруты раздела настроек |
| `:feature:settings:impl` | Настройки темы, цветовой схемы и баланс GigaChat |
| `build-logic` | Convention Plugins с общей конфигурацией Android-, Kotlin- и Compose-модулей |

Feature-модули разделены на `api` и `impl`: приложение зависит от публичных
навигационных контрактов, а экраны, ViewModel, DI и детали реализации остаются
внутри соответствующих `impl`-модулей.

## Настройка GigaChat

Для сборки приложения ключ GigaChat не требуется, однако без него функции,
использующие API, будут недоступны. Секреты не хранятся в репозитории: добавьте
их в локальный файл `local.properties`.

Рекомендуемый вариант — OAuth-ключ:

```properties
gigachat.authorization.key=YOUR_BASE64_AUTHORIZATION_KEY
gigachat.scope=GIGACHAT_API_PERS
```

Access token будет получен автоматически, сохранён в памяти и обновлён перед
истечением срока действия. Для локальной отладки также поддерживается готовый
токен:

```properties
access.token=YOUR_ACCESS_TOKEN
```

## Автор

**ToothLonely**

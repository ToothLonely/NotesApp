# Навигация

> Статус: утверждено.

## Верхнеуровневые направления

Bottom Navigation содержит ровно три равнозначных направления:

| Раздел | Label | Иконка | Стартовый экран |
| --- | --- | --- | --- |
| Notes | «Заметки» | outline/filled `Description` | список заметок |
| Tasks | «Задачи» | outline/filled `CheckBox` | список задач |
| Settings | «Настройки» | outline/filled `Settings` | настройки |

Стартовое направление приложения — Notes.

## Внешний вид

- Navigation Bar является отдельной плавающей capsule, а не полосой от края до края.
- Внешний контейнер: высота 72dp, радиус 36dp, horizontal margin 16dp, цвет `surfaceContainerHigh`, outline 1dp `outlineVariant`, elevation 3.
- Под контейнером остаётся 12dp до верхней границы системного navigation-bar inset. Фон экрана виден слева, справа и снизу.
- Контент экрана получает bottom padding, равный высоте capsule, нижнему зазору, safe inset и ещё 16dp, поэтому список и FAB не перекрываются навигацией.
- Label показывается всегда; одна иконка без текста не используется.
- Иконка 24dp, touch target занимает всю область item.
- Каждый item занимает не меньше 48dp по высоте. Выбранный item получает внутреннюю capsule примерно 96×56dp: `primaryContainer`, иконка `onPrimaryContainer`, label `primary` с `labelMedium` 500.
- Невыбранный item: `onSurfaceVariant`; background панели — `surfaceContainer`.
- Для тёмной темы меняются только токены, геометрия и иерархия сохраняются.
- FAB располагается на 16dp выше верхней границы floating bar, а не над системным inset.

## Видимость и поведение

| Контекст | Bottom Navigation |
| --- | --- |
| Notes: loading / empty / content / error / delete mode | виден и доступен |
| Tasks: loading / empty / content / inline create / voice states / error | виден и доступен |
| Settings: loading / content / balance error | виден и доступен |
| Note Editor и выбор источника изображения | скрыт |

- Переход между вкладками сохраняет пользовательский контекст раздела: scroll position, применённый поиск, сортировку и фильтр в рамках текущего процесса.
- Выбор другой вкладки во время delete mode завершает режим удаления.
- Вложенный редактор открывается из Notes. Back возвращает к прежней позиции списка.
- Успешное сохранение возвращает в Notes, где обновлённая заметка появляется автоматически.
- Повторный выбор уже активного пункта не создаёт новый экземпляр destination.

## Компоновка Compose

Будущая структура без реализации на этом этапе:

```text
app/
|-- navigation/
|   |-- AppDestination.kt
|   `-- AppNavigationState.kt
`-- ui/
    |-- NotesApp.kt
    `-- components/
        `-- NotesBottomNavigationBar.kt
```

`NotesApp` владеет корневым back stack и решает, когда показывать `NotesBottomNavigationBar`. Внешний `Box` учитывает safe insets и позиционирует capsule поверх фона, а content получает соответствующий bottom padding. Каждая production composable остаётся в отдельном файле согласно правилам проекта.

## Accessibility

- Item сообщает role tab, label и selected state.
- Порядок фокуса соответствует визуальному: Notes → Tasks → Settings.
- У иконки внутри item нет отдельного content description: label описывает объединённую цель и предотвращает двойное чтение TalkBack.
- Смена вкладки переводит фокус на заголовок нового экрана.

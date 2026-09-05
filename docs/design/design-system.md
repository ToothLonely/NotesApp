# Дизайн-система

> Статус: проект для согласования.

## Визуальная концепция

Рабочее название концепции — «Мягкое рабочее пространство». Приложение должно ощущаться как аккуратный стол с карточками заметок: нейтральные поверхности, выраженно круглые крупные формы, ясная иерархия и один выбранный пользователем акцент. Цвет помогает найти действие, но не конкурирует с содержимым.

Основные принципы:

- содержание важнее декора: фотографии, заголовки и статусы остаются главным визуальным уровнем;
- фон и карточки различаются тональными поверхностями, а не тяжёлыми тенями;
- геометрия дружелюбная и округлая: карточки используют радиусы 24–28dp, одиночные кнопки и поиск — форму capsule, FAB — круг;
- акцент используется для FAB, primary-кнопок, выбранных фильтров, checkbox, прогресса и активного пункта навигации;
- ошибка, успех и выбранность всегда имеют текст или иконку и не кодируются только цветом;
- светлая и тёмная темы сохраняют одинаковую иерархию и размеры;
- анимация короткая и функциональная, без декоративных переходов.

## Цвет

### Настройки по умолчанию

- Режим темы: `System`.
- Акцентная палитра: `Indigo` («Индиго»).
- Dynamic color: выключен во всех версиях Android.
- Пользовательская палитра меняет accent-роли, но не семантические цвета ошибок и нейтральные поверхности.

### Нейтральные роли

| Роль Material 3 | Светлая тема | Тёмная тема |
| --- | --- | --- |
| `background`, `surface` | `#FAF9FD` | `#121318` |
| `onBackground`, `onSurface` | `#1B1B1F` | `#E4E1E9` |
| `surfaceDim` | `#DBD9DE` | `#121318` |
| `surfaceBright` | `#FAF9FD` | `#38393F` |
| `surfaceContainerLowest` | `#FFFFFF` | `#0D0E13` |
| `surfaceContainerLow` | `#F5F3F8` | `#1A1B20` |
| `surfaceContainer` | `#EFEDF2` | `#1E1F24` |
| `surfaceContainerHigh` | `#E9E7EC` | `#292A30` |
| `surfaceContainerHighest`, `surfaceVariant` | `#E3E1E6` | `#34353B` |
| `onSurfaceVariant` | `#46464F` | `#C7C5D0` |
| `outline` | `#777680` | `#918F99` |
| `outlineVariant` | `#C7C5D0` | `#46464F` |
| `inverseSurface` | `#303034` | `#E4E1E9` |
| `inverseOnSurface` | `#F2F0F5` | `#303034` |
| `scrim` | `#000000` | `#000000` |

Поверхности не подкрашиваются выбранным акцентом. Это удерживает фотографии заметок и цветовые образцы в настройках визуально стабильными при смене схемы.

### Акцентные палитры

Каждый preset задаёт четыре primary-роли для обеих тем. Это достаточно для всех пользовательски окрашиваемых элементов концепции.

| Preset | Light: `primary` / `onPrimary` | Light: `primaryContainer` / `onPrimaryContainer` | Dark: `primary` / `onPrimary` | Dark: `primaryContainer` / `onPrimaryContainer` |
| --- | --- | --- | --- | --- |
| Индиго, default | `#4F56A6` / `#FFFFFF` | `#E0E0FF` / `#090E5D` | `#BEC2FF` / `#1E276E` | `#373E86` / `#E0E0FF` |
| Бирюза | `#006A66` / `#FFFFFF` | `#9CF2EB` / `#00201E` | `#80D5CF` / `#003734` | `#00504C` / `#9CF2EB` |
| Малина | `#984061` / `#FFFFFF` | `#FFD9E3` / `#3E001D` | `#FFB0C8` / `#5E1133` | `#7A2949` / `#FFD9E3` |
| Янтарь | `#765A00` / `#FFFFFF` | `#FFE080` / `#241A00` | `#EAC300` / `#3D2F00` | `#594400` / `#FFE080` |

Контраст всех перечисленных пар находится в диапазоне от 6.45:1 до 13.32:1, то есть превышает WCAG AA 4.5:1 для обычного текста. Перед реализацией это не отменяет проверки готовых компонентов, disabled-прозрачностей и текста поверх изображений.

Применение:

- `primary` / `onPrimary`: FAB, filled button, выбранный checkbox, активный progress;
- `primaryContainer` / `onPrimaryContainer`: индикатор выбранной вкладки, выбранный filter chip, мягкий информационный баннер;
- swatch в Settings показывает `primary`; выбор дополнительно отмечается контуром, check-иконкой и selected semantics;
- tertiary-роли не используются как пользовательский акцент, чтобы не создавать второй конкурирующий цвет.

### Общие secondary, tertiary и error-роли

| Роль | Светлая тема | Тёмная тема |
| --- | --- | --- |
| `secondary` / `onSecondary` | `#5D5E6E` / `#FFFFFF` | `#C6C5D8` / `#2F3040` |
| `secondaryContainer` / `onSecondaryContainer` | `#E2E1F3` / `#1A1B2C` | `#464757` / `#E2E1F3` |
| `tertiary` / `onTertiary` | `#75556F` / `#FFFFFF` | `#E4BADB` / `#43283F` |
| `tertiaryContainer` / `onTertiaryContainer` | `#FFD7F5` / `#2C122A` | `#5B3E56` / `#FFD7F5` |
| `error` / `onError` | `#BA1A1A` / `#FFFFFF` | `#FFB4AB` / `#690005` |
| `errorContainer` / `onErrorContainer` | `#FFDAD6` / `#410002` | `#93000A` / `#FFDAD6` |

Красный применяется к фактической ошибке, удалению и явному сбросу сохранённых предпочтений. Режим удаления в целом остаётся тональным, а сброс настроек — прозрачной outlined-кнопкой с подтверждением, чтобы экран не превращался в сплошное предупреждение.

## Типографика

Используется системный Roboto через `FontFamily.Default`: это не добавляет зависимость, корректно поддерживает кириллицу и системный font scale. Display-стили приложению не нужны.

| Токен | Размер / line height | Weight | Использование |
| --- | --- | --- | --- |
| `headlineMedium` | 28sp / 36sp | 600 | крупное пустое состояние только при достаточном месте |
| `headlineSmall` | 24sp / 32sp | 600 | заголовок корневого экрана |
| `titleLarge` | 22sp / 28sp | 600 | заголовок редактора и крупных секций |
| `titleMedium` | 16sp / 24sp | 600 | название заметки, заголовок карточки |
| `titleSmall` | 14sp / 20sp | 600 | заголовок компактного состояния |
| `bodyLarge` | 16sp / 24sp | 400 | поля редактора, основной текст |
| `bodyMedium` | 14sp / 20sp | 400 | строки задач, пояснения |
| `bodySmall` | 12sp / 16sp | 400 | даты и вторичные метаданные |
| `labelLarge` | 14sp / 20sp | 600 | кнопки и filter chips |
| `labelMedium` | 12sp / 16sp | 500 | Bottom Navigation, вспомогательные labels |

Текст не уменьшается ради размещения. При увеличенном шрифте компоненты растут по высоте, однострочные панели переходят в вертикальную компоновку, а контент остаётся прокручиваемым.

### Покрытие темами

Light и Dark — две реализации одной системы, а не отдельные варианты для выбранных экранов. Все состояния Notes, Editor, Tasks и Settings используют одинаковые размеры, формы, порядок компонентов и semantics; при смене темы заменяются только цветовые роли и допустимая tonal elevation.

| Экран | Light | Dark |
| --- | --- | --- |
| Notes: List, Grid, loading, empty, content, search empty, error, delete mode | да | да |
| Note Editor: new, edit, loading, inline title, image, voice, permission, error | да | да |
| Tasks: loading, empty, content, creation menu, inline create/edit, delete confirmation, recording, processing, error | да | да |
| Settings: preferences states, reset confirmation и balance loading/content/error | да | да |

## Формы

| Токен | Радиус | Использование |
| --- | --- | --- |
| `extraSmall` | 12dp | небольшие image placeholder и status mark |
| `small` | 16dp | icon button container и компактное menu item |
| `medium` | 20dp | image preview и внутренние поверхности карточки |
| `large` | 24dp | text field, task row и status panel |
| `extraLarge` | 28dp | note card, balance/error card |
| `floating` | 36dp | floating Bottom Navigation и крупный modal sheet |
| `full` | 50% | search, chips, одиночные кнопки, FAB, swatch и selected nav item |

Карточки не используют случайные радиусы. Превью изображения обрезается `medium`-формой внутри `extraLarge`-карточки. Полное скругление применяется только к одноосным элементам; большие многострочные поля остаются скруглёнными прямоугольниками, чтобы не терять полезную площадь.

## Отступы и сетка

Базовый модуль — 4dp.

| Токен | Значение | Назначение |
| --- | --- | --- |
| `space1` | 4dp | тесная связь иконки и текста |
| `space2` | 8dp | внутренний малый интервал |
| `space3` | 12dp | промежуток внутри строк и карточек |
| `space4` | 16dp | стандартный padding экрана и карточки |
| `space6` | 24dp | секции и крупные вертикальные интервалы |
| `space8` | 32dp | отделение смысловых групп |
| `space12` | 48dp | пустые состояния и крупные поля воздуха |

Экран имеет горизонтальный padding 16dp на компактной ширине. На ширине от 600dp контент центрируется и ограничивается 720dp, но требуемый Bottom Navigation сохраняется на этой итерации.

## Размеры компонентов

| Компонент | Размер |
| --- | --- |
| Минимальная touch target | 48×48dp |
| Иконка стандартная | 24dp |
| Иконка пустого состояния | 64dp |
| Top App Bar | минимум 64dp без system inset |
| Search field | 56dp высотой |
| Filled/outlined button | минимум 48dp высотой |
| FAB | 56×56dp |
| Editor Voice FAB | 56×56dp, `primaryContainer`, 16dp над Save |
| Task row | минимум 64dp высотой, радиус 24dp |
| Note preview | 96×80dp в списке |
| Note grid | 2 колонки на compact width, gap 12dp |
| Note grid preview | ширина карточки, aspect ratio 4:3 |
| Editor attachment thumbnail | круг 40×40dp внутри touch target 48×48dp |
| Palette swatch | 48×48dp; полная touch target минимум 64×64dp |
| Floating Bottom Navigation | 72dp, радиус 36dp, horizontal margin 16dp |
| Зазор под Bottom Navigation | 12dp плюс системный navigation-bar inset |

## Глубина, обводки и motion

- Основное разделение выполняют `surfaceContainer*` и outline в 1dp.
- Карточки списка имеют elevation 0; FAB — стандартную M3 elevation.
- Плавающая навигационная capsule использует небольшую elevation 3 и не сливается с системной gesture area.
- Нажатие использует ripple и лёгкое tonal-state наложение.
- Появление inline task editor и voice panel: fade + expand, 200ms.
- Переключение темы и палитры: crossfade 200ms без белой/чёрной вспышки.
- Пользовательская настройка удаления анимаций соблюдается; без motion интерфейс остаётся полностью понятным.

## Общие UI-паттерны

### Поиск

`AppSearchField` переиспользуется в Notes и Tasks: leading search icon, trailing clear button только при непустом запросе, IME action Search. Поиск запускается только по кнопке поиска или IME Search, как требует `task.md`; введённый, но ещё не применённый запрос не фильтрует список.

### Сортировка

Иконка sort открывает компактное menu с radio-selection:

- «Сначала новые»;
- «Сначала старые».

Текущий вариант имеет selected semantics. Меню не полагается только на порядок или цвет.

### Loading, empty и error

- Loading: progress indicator и конкретная подпись; при наличии полезного content он остаётся видимым.
- Empty: простая outline-иконка, короткий заголовок, пояснение и доступное основное действие.
- Error: error-иконка, понятный текст без технических деталей и кнопка восстановления (`Повторить`, `Открыть настройки` или `Записать снова`).
- Snackbar используется для краткого подтверждения действия; критические ошибки не скрываются только в Snackbar.

### Attachment в редакторе

- Пустая новая заметка не содержит image placeholder или зарезервированной области под изображение.
- Скрепка в Top App Bar открывает выбор файла или камеры.
- После выбора появляется круглая thumbnail 40dp рядом с Back; постоянного большого preview в content нет.
- Thumbnail открывает просмотр и действия «Заменить» / «Удалить». Большой preview существует только во временном overlay/sheet по запросу пользователя.

### Inline title и Voice FAB в редакторе

- Заголовок редактируется прямо в Top App Bar; отдельного outlined title field нет.
- Новая заметка показывает серое предлагаемое имя `Заметка N`. Если пользователь не вводит свой title, это имя материализуется и сохраняется при Save.
- Voice input запускается круглым tonal FAB 56dp в правом нижнем углу body. Он расположен на 16dp выше Save и не перекрывает вводимый текст.

## Accessibility и локализация

- Все интерактивные цели не меньше 48×48dp.
- Иконки-кнопки имеют локализованные content descriptions; декоративные preview/placeholder — `null`.
- Строка задачи объединяет название и checked state; checkbox сообщает `checked/unchecked` и доступен отдельно.
- Loading/status panel — polite live region; error panel — alert semantics.
- Выбранные tab, filter, theme и palette получают selected semantics.
- Заголовок note card — максимум две строки с ellipsis; дата не обрезается первой.
- Filter chips горизонтально прокручиваются при нехватке ширины.
- На большом font scale theme selector превращается из segmented control в вертикальную radio-группу, palette — в сетку 2×2; текст не масштабируется вниз.
- Контент прокладывается с учётом status/navigation bars и экранной клавиатуры.

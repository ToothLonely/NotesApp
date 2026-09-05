# Экран Note Editor

> Статус: проект для согласования.

## Назначение

Один вложенный экран создаёт новую заметку и загружает существующую для просмотра/редактирования. Заголовок редактируется прямо в Top App Bar. Приложение гарантирует непустой title через автоматически предлагаемое имя `Заметка N`; текст и изображение опциональны.

## Иерархия

1. Top App Bar: Back, optional attachment thumbnail, inline title, paperclip action.
2. Многострочное поле «Текст заметки».
3. Voice input FAB.
4. Voice/status panel при активном сценарии.
5. Primary-кнопка «Сохранить».

Bottom Navigation отсутствует. Экран вертикально прокручивается, а Save остаётся последним элементом content и поднимается над IME.

Геометрия следует обновлённой мягкой системе: body field и voice panel имеют радиус 24dp, icon actions, Voice FAB и Save — форму capsule/circle. Под изображение не резервируется место.

## Wireframe

```text
┌──────────────────────────────────┐
│ ←  Заметка 1              clip │
│ [ Текст заметки                  │
│                                  │
│                                  │
│                                ] │
│                       ( mic )   │
│                                  │
│ [          Сохранить           ] │
└──────────────────────────────────┘
```

## Состояния

| Состояние | Представление | Поведение |
| --- | --- | --- |
| Loading existing | top bar + skeleton/centered progress «Загружаем заметку…» | Back доступен, редактирование недоступно |
| New content | серое предлагаемое имя `Заметка N` в app bar, пустой body, paperclip; image placeholder отсутствует | ввод title/body, voice, attachment, Save |
| Existing content без изображения | сохранённый title обычным цветом, body и paperclip | все поля доступны |
| Existing content с изображением | thumbnail рядом с Back, сохранённый title, paperclip | thumbnail открывает preview/actions |
| Image picking/camera | modal bottom sheet «Добавить изображение»: «Выбрать файл», «Сделать фото» | sheet закрывается после выбора или отмены |
| Image attached | небольшая круглая thumbnail в app bar | tap: просмотреть, заменить или удалить |
| Saving | progress внутри Save, поля и attachment actions временно disabled | Back не запускает второе сохранение |
| Load/save error | полный error state при загрузке; inline error banner при сохранении | «Повторить»; введённый текст не теряется |
| Voice recording | Voice FAB меняется на Stop; status panel, timer и «Отмена» | остановить или отменить запись |
| Voice processing | Voice FAB disabled/progress + «Распознаём речь…» | поля видны; Save disabled до завершения |
| Voice error | error panel с короткой причиной | «Записать снова» и закрыть |
| Permission denied | пояснение рядом с вызвавшим действием | «Открыть настройки», если запрос больше нельзя показать |

### Inline title и имя по умолчанию

- В новой заметке между Back/thumbnail и paperclip отображается серое `Заметка N` в стиле placeholder, без рамки отдельного поля.
- `N` — следующий уникальный номер автоматически названной заметки. Он не меняется во время текущей сессии редактора.
- Tap переводит inline title в фокус. При вводе первого символа серое предлагаемое имя исчезает, пользовательский title отображается `onSurface`.
- Если title пуст или состоит из пробелов при Save, ViewModel подставляет предложенное `Заметка N` и сохраняет его как обычное значение. Поэтому заметка всегда удовлетворяет требованию непустого title.
- Для существующей заметки сразу показывается сохранённый title обычным цветом. Если пользователь полностью его очистил, применяется тот же fallback-механизм.
- Вне фокуса длинный title занимает одну строку и обрезается ellipsis; в фокусе поле прокручивается горизонтально. Полный текст остаётся доступен accessibility services.

### Изображение и разрешения

- В состоянии без изображения нет placeholder, preview card или пустого блока в content.
- Круглая paperclip action 48dp в Top App Bar открывает source sheet.
- Для системного Photo Picker/SAF отдельное storage-разрешение не запрашивается, если оно не требуется платформой.
- Camera и microphone permissions запрашиваются непосредственно перед соответствующим действием.
- При отказе пользователь остаётся на экране, поля не очищаются.
- После выбора рядом с Back появляется круглая thumbnail 40dp внутри touch target 48dp. Она не сдвигает title за пределы доступной ширины.
- Tap по thumbnail открывает attachment sheet с большим временным preview и действиями «Заменить изображение» и «Удалить изображение». После закрытия sheet большой preview исчезает.
- Paperclip остаётся доступной и при выбранном изображении; её описание меняется на «Заменить изображение».
- Thumbnail имеет описание «Прикреплённое изображение. Открыть действия»; delete action — «Удалить изображение из заметки».

### Голосовой ввод

- В idle-состоянии используется круглый tonal Voice FAB 56dp: `primaryContainer` / `onPrimaryContainer`, небольшая elevation.
- FAB закреплён в нижнем правом углу editor content на 16dp выше Save. Body получает внутренний bottom inset, поэтому FAB не закрывает текст и caret.
- Во время записи иконка FAB меняется с mic на Stop. Во время обработки FAB disabled и показывает progress либо сопровождается progress в status panel.
- Запись добавляет распознанный текст в конец body.
- Если body не пустой и не заканчивается whitespace, перед вставкой добавляется пробел или перевод строки.
- Ошибка записи/распознавания не изменяет существующий текст.
- Processing имеет live-region semantics, но прогресс не объявляется на каждом кадре.

## Структура Compose-компонентов

```text
feature/notes/impl/presentation/editor/
|-- NoteEditorRoute.kt
|-- NoteEditorScreen.kt
|-- NoteEditorUiState.kt
|-- NoteEditorViewModel.kt
`-- components/
    |-- NoteEditorTopBar.kt
    |-- InlineNoteTitleField.kt
    |-- NoteBodyField.kt
    |-- EditorAttachmentThumbnail.kt
    |-- ImageSourceSheet.kt
    |-- ImageAttachmentActionsSheet.kt
    |-- EditorVoiceInputFab.kt
    |-- EditorVoiceStatusPanel.kt
    `-- SaveNoteButton.kt
```

Общий визуальный шаблон voice status переиспользует tokens и state semantics дизайн-системы, но редакторский panel остаётся в пакете экрана, пока не появится вторая идентичная реализация.

## Accessibility и длинный текст

- Inline title имеет label semantics «Заголовок заметки»; серое имя объявляется как предлагаемое значение, а не как уже введённый пользовательский текст.
- Back: «Назад»; paperclip: «Добавить изображение» или «Заменить изображение»; camera: «Сделать фото»; file: «Выбрать изображение»; mic сообщает recording state.
- Voice FAB объявляется как «Начать голосовой ввод», «Остановить запись» или «Распознаём речь» в зависимости от состояния.
- Высота body растёт до разумного минимума и затем прокручивает весь экран; системный font scale не ограничивается.
- При крупном font scale thumbnail остаётся после Back, title занимает доступную ширину и обрезается ellipsis вне фокуса, а paperclip сохраняет touch target 48dp.

# Визуальные макеты

Макеты созданы встроенным генератором изображений в режиме `ui-mockup`. Точные значения, состояния и поведение определяются Markdown-спецификациями в `docs/design`; изображения служат проверкой общего визуального направления.

## Файлы

- [app-overview.png](app-overview.png) — все основные экраны в Light; Notes показан в Grid, Editor — в режиме создания без изображения.
- [app-overview-dark.png](app-overview-dark.png) — все основные экраны в Dark; Notes показан в List, Editor — в режиме редактирования с круглой attachment thumbnail.
- [screen-states.png](screen-states.png) — empty Notes, Grid delete mode, voice processing Tasks и balance error Settings.
- [tasks-management-light.png](tasks-management-light.png) — Tasks content, inline create, inline edit и delete confirmation в Light.
- [tasks-management-dark.png](tasks-management-dark.png) — тот же набор management-состояний Tasks в Dark.
- [tasks-voice-light.png](tasks-voice-light.png) — выбор способа создания, запись, обработка и ошибка голосового сценария в Light.
- [tasks-voice-dark.png](tasks-voice-dark.png) — тот же голосовой сценарий Tasks в Dark.

Тема и layout независимы. Light/Dark поддерживают и List, и Grid; на общих досках показаны разные сочетания только ради покрытия без дублирования одинакового content. Специализированные Tasks-доски намеренно дублируют один и тот же набор состояний в Light и Dark, чтобы сравнение тем было прямым.

Фотографии, даты, названия, номер новой заметки и баланс — демонстрационные fixtures. `Заметка 1` на макете представляет шаблон `Заметка N`, а не фиксированное production-значение.

## Final prompt: Light overview

```text
Use case: ui-mockup
Asset type: high-fidelity Light overview board for a production Android notes and tasks app
Primary request: show four complete 390x844dp Android screens side by side, all in Light: Notes Grid, new Note Editor, Tasks and Settings.
Visual direction: friendly highly rounded Material 3; note cards 28dp, fields and task rows 24dp, pill search/chips/buttons, circular FAB, restrained indigo accent, subtle tonal surfaces, no gradients or glass.
Navigation: on Notes, Tasks and Settings use one fully rounded floating bottom-navigation capsule, 72dp high and 36dp radius, 16dp horizontal margins and a visible 12dp gap above the system navigation inset. Labels are "Заметки", "Задачи", "Настройки". Editor has no bottom navigation.
Notes: "Заметки", rounded search, sort/List-Grid/delete actions, Grid selected, two-column grid of four rounded cards, circular create FAB.
Editor: Back; borderless inline gray suggested title "Заметка 1" on the same app-bar row; circular paperclip at top right; no image, placeholder or separate title field; one large rounded body field "Текст заметки"; tonal circular mic FAB 56dp at bottom-right, 16dp above pill button "Сохранить".
Tasks: rounded search, filters, rows and create FAB.
Settings: rounded balance card, theme segmented control with Light selected, four accent swatches and a transparent pill reset button with thin error-red outline, reset icon and error-red text "Сбросить настройки".
Constraints: practical native Android UI, minimum 48dp targets, readable Russian text, no logos, watermark, extra screens, sharp cards or gibberish.
```

## Final prompt: Dark overview

```text
Use case: ui-mockup
Asset type: high-fidelity Dark companion board for the same Android app
Primary request: show the same four screens and geometry, all in Dark: Notes List, existing Note Editor, Tasks and Settings.
Visual direction: charcoal #121318/#1E1F24 surfaces, soft indigo #BEC2FF accent, identical rounded geometry and floating navigation to Light.
Notes: List selected, three wide rounded note cards.
Editor: Back; small 40dp circular attached-image thumbnail immediately after Back; borderless inline actual title "Идеи для проекта" in normal text; circular paperclip at top right; no separate title field or large preview; large body field; tonal circular mic FAB 56dp at bottom-right, 16dp above pill "Сохранить".
Tasks and Settings: same content hierarchy as Light, Dark selected in theme control; reset stays transparent with a thin dark-theme error-pink outline, reset icon and error-pink text "Сбросить настройки".
Constraints: every screen unequivocally dark; outer navigation capsules visibly float; accessible contrast and 48dp targets; no logos, watermark, extra screens, sharp cards or gibberish.
```

## Final prompt: State coverage

```text
Use case: ui-mockup
Asset type: rounded state-coverage board for the same Android app
Primary request: four complete phones showing Notes empty in Light, Notes Grid delete mode in Dark, Tasks voice processing in Light and Settings balance error in Dark.
Visual system: match the Light/Dark overview boards; 28dp cards, 24dp fields/rows, pill actions, circular FAB, floating bottom-navigation capsules with 16dp side margins and 12dp bottom gap.
Notes empty: keep search, List/Grid/delete actions, empty icon and "Заметок пока нет", create FAB.
Delete mode: rounded banner "Выберите заметку для удаления", two-column note grid with circular trash controls, no create FAB, navigation remains enabled.
Tasks processing: progress plus "Обрабатываем запись…", list remains visible at lower emphasis, create FAB disabled.
Settings error: rounded balance error card with icon, text and pill "Повторить"; local settings remain usable; reset stays transparent with a thin dark-theme error-pink outline, reset icon and error-pink text "Сбросить настройки".
Constraints: states are conveyed by icon plus text, navigation never touches screen edges, minimum 48dp targets, no logos, watermark, sharp rectangles or gibberish.
```

Confirmation dialog не показан на обзорных досках, чтобы не перекрывать содержимое Settings. Его точный текст, действия и состояния зафиксированы в [спецификации Settings](../screens/settings.md#reset).

## Final prompt: Tasks management Light

```text
Use case: ui-mockup
Asset type: high-fidelity Light task-management state board for the same production Android notes and tasks app
Input images: app-overview.png is a visual style reference only.
Primary request: four complete 390x844dp Tasks screens side by side in Light: Content, Inline create, Inline edit, Delete confirmation.
Visual system: friendly highly rounded Material 3, #FAF9FD background, restrained indigo accent, 24dp task rows, pill search/filter chips, circular FAB and floating rounded Bottom Navigation with "Задачи" selected.
Shared header: "Задачи", sort, "Поиск задач", "Все", "Активные", "Выполненные" outside the scrolling list.
Content: rows "Подготовить презентацию", "Купить продукты", completed "Позвонить маме", checkboxes, dates and overflow.
Inline create: use the Dark companion's two-level rounded editor geometry — a full-width focused field "Название задачи" on top and a separate action row below with circular cancel on the left and confirm on the right.
Inline edit: use the same two-level editor; "Купить продукты" is edited in the full-width top field, with the separate cancel/confirm row below.
Delete: dimmed list and rounded dialog "Удалить задачу?", "Задача «Подготовить презентацию» будет удалена.", "Отмена", error-red "Удалить".
Constraints: exact readable Russian text, 48dp targets, no separate create/edit route, no task images, logos, watermark, sharp cards or gibberish.
```

Финальная правка Light: экраны Inline create и Inline edit приведены к геометрии Dark — full-width поле сверху, отдельная строка действий снизу, Cancel слева и Confirm справа; остальные экраны и светлая палитра сохранены без изменений.

## Final prompt: Tasks management Dark

```text
Use case: ui-mockup
Asset type: high-fidelity Dark companion for Tasks management
Input images: app-overview-dark.png is a visual style reference only.
Primary request: the identical four Tasks states and geometry as the Light companion: Content, Inline create, Inline edit, Delete confirmation.
Visual system: charcoal #121318, #1E1F24 tonal surfaces, soft indigo #BEC2FF, 24dp rows, pill controls, circular FAB and floating rounded Bottom Navigation with "Задачи" selected.
Content and editors: the same titles, controls and positions as Light. Inline create/edit use the approved two-level rounded editor: full-width field above, separate cancel/confirm row below.
Delete: dark rounded dialog with the same exact Russian copy; "Удалить" uses dark-theme error pink.
Constraints: every screen unequivocally Dark, identical functional coverage to Light, readable Russian text, 48dp targets, no separate route, logos, watermark, sharp cards or gibberish.
```

## Final prompt: Tasks voice Light

```text
Use case: ui-mockup
Asset type: high-fidelity Light task voice-flow state board
Input images: app-overview.png is a visual style reference only.
Primary request: four complete Tasks screens in Light: creation choice, voice recording, voice processing, voice error.
Creation choice: anchored menu above FAB with "Ввести текст" and "Продиктовать".
Recording: rounded panel "Идёт запись", "00:12", waveform, "Завершить", "Отмена"; list remains visible.
Processing: check + "Речь распознана", progress + "Формулируем задачу…"; list is lower emphasis and FAB disabled.
Error: error panel "Не удалось создать задачу", "Проверьте подключение и попробуйте снова", "Повторить", "Записать снова"; list remains usable.
Constraints: match the rounded Light system, fixed search/filters and floating Bottom Navigation, exact readable Russian text, icon plus text for states, no full-screen voice route, logos, watermark, sharp cards or gibberish.
```

## Final prompt: Tasks voice Dark

```text
Use case: ui-mockup
Asset type: high-fidelity Dark companion for the Tasks voice flow
Input images: app-overview-dark.png is a visual style reference only.
Primary request: the identical four Tasks voice states and geometry as the Light companion: creation choice, recording, processing, error.
Visual system: charcoal #121318, #1E1F24 tonal surfaces, soft indigo #BEC2FF, rounded panels and floating Bottom Navigation with "Задачи" selected.
Copy and behavior: the same exact Russian labels, list visibility, disabled processing FAB and error recovery actions as Light.
Constraints: every screen unequivocally Dark, identical functional coverage to Light, 48dp targets, no full-screen voice route, logos, watermark, sharp cards or gibberish.
```

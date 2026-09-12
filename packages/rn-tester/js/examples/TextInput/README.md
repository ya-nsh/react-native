# TextInput selection-menu actions

`editMenuItems` appends app-defined actions to the native text selection menu on
Android and iOS 16 or later. iOS 15 keeps its standard menu. System actions such
as Cut, Copy, Paste, and Select All retain their normal behavior.

```tsx
<TextInput
  defaultValue="Select some text"
  editMenuItems={[
    {id: 'quote', title: 'Quote'},
    {id: 'translate', title: 'Translate'},
  ]}
  onEditMenuItemPress={({nativeEvent}) => {
    const {id, text, selection, eventCount} = nativeEvent;
    const selectedText = text.slice(selection.start, selection.end);
    handleSelection(id, selectedText, eventCount);
  }}
/>
```

Each item needs a unique, non-empty `id` and a non-empty, localized `title`.
Malformed items and later duplicate IDs are ignored. Set the prop to `[]`,
`null`, or `undefined` to remove custom actions.

The callback receives `{id, text, selection: {start, end}, eventCount, target}`.
`text` is the full native text at activation. Selection offsets use UTF-16 code
units, matching JavaScript string indices, including emoji and non-Latin text.
`eventCount` uses the same text-edit counter as `onChange`; activating a menu
item does not increment it. Read the selected text from the event rather than
from a controlled value that may be behind the native input.

Custom actions require a non-empty selection. They are not offered for secure
inputs or when `contextMenuHidden` is true. Availability on read-only inputs follows the platform's existing selection
behavior. System actions do not invoke `onEditMenuItemPress`. The callback
does not edit text or explicitly change focus; menu dismissal follows the
platform's normal behavior.

Items are refreshed when the native menu is prepared. If text, selection, or
items change while a menu is open, outdated custom actions are ignored. Reopen
the menu to use the current actions. An input that has detached or been recycled
cannot dispatch actions from its previous menu.

## RNTester verification

Open **TextInput → Custom text selection menu** on Android and iOS 16+:

1. Select `😀` in `A😀B`, then use **Inspect selection**. The result must contain
   the complete emoji and a range length of two UTF-16 code units.
2. Test custom actions on single-line and multiline inputs, including RTL text.
3. Verify Copy/Paste and Select All still work and do not increment the custom
   action counter. Dismiss the menu without choosing an action.
4. Remove and restore actions, then reopen the menu. Verify no duplicate items.
5. Enable secure entry and hide the context menu; neither may expose custom
   actions. Disable those options and check a read-only input.
6. Switch between single-line and multiline, remount the input, and navigate
   away while a menu is visible. Reopen the example and verify no stale callback.
7. On iOS 15, verify the standard menu and input editing still work.

Automated tests cover native menu lifecycle and snapshot validation, JS input
normalization, view-config serialization, and event dispatch.

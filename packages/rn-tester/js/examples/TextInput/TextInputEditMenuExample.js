/**
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 * @flow strict-local
 * @format
 */

import type {TextInputEditMenuItemPressEvent} from 'react-native';

import RNTesterButton from '../../components/RNTesterButton';
import RNTesterText from '../../components/RNTesterText';
import ExampleTextInput from './ExampleTextInput';
import * as React from 'react';
import {useState} from 'react';
import {StyleSheet, View} from 'react-native';

const items = [
  {id: 'quote', title: 'Quote selection'},
  {id: 'inspect', title: 'Inspect selection'},
];

export default function TextInputEditMenuExample(): React.Node {
  const [multiline, setMultiline] = useState(false);
  const [secure, setSecure] = useState(false);
  const [hidden, setHidden] = useState(false);
  const [readOnly, setReadOnly] = useState(false);
  const [enabled, setEnabled] = useState(true);
  const [inputKey, setInputKey] = useState(0);
  const [result, setResult] = useState(
    'Select text, then choose a custom action.',
  );
  const [pressCount, setPressCount] = useState(0);

  function onPress(event: TextInputEditMenuItemPressEvent) {
    const {id, text, selection, eventCount} = event.nativeEvent;
    const selectedText = text.slice(selection.start, selection.end);
    setPressCount(count => count + 1);
    setResult(
      `${id}: “${selectedText}”\nUTF-16 range: ${selection.start}–${selection.end}; text event count: ${eventCount}`,
    );
  }

  return (
    <View>
      <RNTesterText>
        Custom actions appear alongside the system menu on Android and iOS 16+.
        Secure inputs, hidden menus, and collapsed selections have no custom
        actions.
      </RNTesterText>
      <ExampleTextInput
        key={inputKey}
        testID="edit-menu-input"
        accessibilityLabel="Custom edit menu input"
        defaultValue="Select A😀B, नमस्ते, or مرحبا to inspect the native selection."
        multiline={multiline}
        secureTextEntry={secure}
        contextMenuHidden={hidden}
        readOnly={readOnly}
        editMenuItems={enabled ? items : []}
        onEditMenuItemPress={onPress}
        style={styles.input}
      />
      <RNTesterText testID="edit-menu-result">{result}</RNTesterText>
      <RNTesterText>Custom actions received: {pressCount}</RNTesterText>
      <RNTesterButton onPress={() => setMultiline(value => !value)}>
        {multiline ? 'Use single line' : 'Use multiple lines'}
      </RNTesterButton>
      <RNTesterButton onPress={() => setSecure(value => !value)}>
        {secure ? 'Disable secure entry' : 'Enable secure entry'}
      </RNTesterButton>
      <RNTesterButton onPress={() => setHidden(value => !value)}>
        {hidden ? 'Show context menu' : 'Hide context menu'}
      </RNTesterButton>
      <RNTesterButton onPress={() => setReadOnly(value => !value)}>
        {readOnly ? 'Make editable' : 'Make read-only'}
      </RNTesterButton>
      <RNTesterButton onPress={() => setEnabled(value => !value)}>
        {enabled ? 'Remove custom actions' : 'Restore custom actions'}
      </RNTesterButton>
      <RNTesterButton onPress={() => setInputKey(value => value + 1)}>
        Remount input
      </RNTesterButton>
    </View>
  );
}

const styles = StyleSheet.create({
  input: {
    borderWidth: 1,
    minHeight: 60,
    marginVertical: 12,
    padding: 8,
  },
});

/**
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 * @format
 */

import * as React from 'react';
import {
  TextInput,
  type TextInputEditMenuItem,
  type TextInputEditMenuItemPressEvent,
} from 'react-native';

const actions: ReadonlyArray<TextInputEditMenuItem> = [
  {id: 'quote', title: 'Quote'},
];
const onPress = ({nativeEvent}: TextInputEditMenuItemPressEvent) => {
  const actionId: string = nativeEvent.id;
  const selectedText: string = nativeEvent.text.slice(
    nativeEvent.selection.start,
    nativeEvent.selection.end,
  );
  const eventCount: number = nativeEvent.eventCount;
  const target: number = nativeEvent.target;
  console.log(actionId, selectedText, eventCount, target);
};

<TextInput editMenuItems={actions} onEditMenuItemPress={onPress} />;
<TextInput editMenuItems={null} onEditMenuItemPress={null} />;
<TextInput editMenuItems={[]} onEditMenuItemPress={undefined} />;

// @ts-expect-error Custom actions require stable IDs as well as labels.
<TextInput editMenuItems={[{title: 'Quote'}]} />;
// @ts-expect-error IDs must be strings.
<TextInput editMenuItems={[{id: 1, title: 'Quote'}]} />;

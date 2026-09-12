/**
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 * @flow strict-local
 * @format
 */

import type {TextInputEditMenuItem} from './TextInput.flow';

/** Normalize before crossing the native boundary, including in release builds. */
export default function processEditMenuItems(
  value: unknown,
): Array<TextInputEditMenuItem> {
  if (!Array.isArray(value)) {
    return [];
  }

  const ids = new Set<string>();
  const items: Array<TextInputEditMenuItem> = [];
  for (const item of value) {
    if (item == null || typeof item !== 'object') {
      continue;
    }
    const {id, title} = item;
    if (
      typeof id !== 'string' ||
      typeof title !== 'string' ||
      id.trim().length === 0 ||
      title.trim().length === 0 ||
      ids.has(id)
    ) {
      continue;
    }
    ids.add(id);
    items.push({id, title});
  }
  return items;
}

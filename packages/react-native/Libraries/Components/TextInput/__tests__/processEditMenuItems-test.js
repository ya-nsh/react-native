/**
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 * @flow strict-local
 * @format
 */

import processEditMenuItems from '../processEditMenuItems';

it('preserves order and localized labels without mutating the input', () => {
  const items = Object.freeze([
    Object.freeze({id: 'translate', title: 'अनुवाद करें'}),
    Object.freeze({id: 'quote', title: 'اقتباس'}),
  ]);
  expect(processEditMenuItems(items)).toEqual(items);
});

it('keeps the first valid occurrence of each ID', () => {
  expect(
    processEditMenuItems([
      {id: 'quote', title: ''},
      {id: 'quote', title: 'Quote'},
      {id: 'quote', title: 'Duplicate'},
      {id: 'translate', title: 'Translate'},
    ]),
  ).toEqual([
    {id: 'quote', title: 'Quote'},
    {id: 'translate', title: 'Translate'},
  ]);
});

it('ignores malformed entries without losing valid actions', () => {
  expect(
    processEditMenuItems([
      null,
      false,
      'quote',
      {},
      {id: 5, title: 'Quote'},
      {id: 'quote', title: {}},
      {id: '  ', title: 'Quote'},
      {id: 'quote', title: '\t'},
      {id: 'quote', title: 'Quote', ignored: 'not sent to native'},
    ]),
  ).toEqual([{id: 'quote', title: 'Quote'}]);
});

it.each([null, undefined, false, 5, 'quote', {}])(
  'clears the menu for a non-array value: %p',
  value => {
    expect(processEditMenuItems(value)).toEqual([]);
  },
);

it('accepts IDs that are object property names', () => {
  expect(
    processEditMenuItems([
      {id: '__proto__', title: 'Quote'},
      {id: 'constructor', title: 'Translate'},
    ]),
  ).toHaveLength(2);
});

/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.react.views.textinput

import android.app.Activity
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.PopupMenu
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController

@RunWith(RobolectricTestRunner::class)
class ReactTextInputEditMenuTest {
  private lateinit var controller: ActivityController<Activity>
  private lateinit var view: EditText
  private lateinit var menu: Menu
  private lateinit var mode: ActionMode
  private lateinit var editMenu: ReactTextInputEditMenu
  private var allowed = true
  private val presses = mutableListOf<List<Any>>()

  @Before
  fun setup() {
    controller = Robolectric.buildActivity(Activity::class.java).setup().visible()
    val activity = controller.get()
    view = EditText(activity)
    activity.setContentView(view)
    view.requestFocus()
    view.setText("A😀B")
    view.setSelection(1, 3)
    menu = PopupMenu(activity, view).menu
    mode = mock()
    editMenu =
        ReactTextInputEditMenu(view, { allowed }) { id, text, start, end ->
          presses.add(listOf(id, text, start, end))
        }
    editMenu.items = listOf(ReactTextInputEditMenu.Item("quote", "Quote"))
  }

  @After
  fun teardown() {
    editMenu.detach()
    controller.pause().stop().destroy()
  }

  @Test
  fun preservesSystemActionsAndDoesNotDuplicateItemsOnPrepare() {
    val copy = menu.add(0, android.R.id.copy, 0, "Copy")
    editMenu.prepare(mode, menu)
    editMenu.prepare(mode, menu)
    assertThat(menu.size()).isEqualTo(2)
    assertThat(menu.findItem(android.R.id.copy)).isSameAs(copy)
    assertThat(editMenu.onActionItemClicked(mode, copy)).isFalse()
    assertThat(presses).isEmpty()
  }

  @Test
  fun dispatchesUnicodeSelectionOnceWithoutEditingText() {
    editMenu.prepare(mode, menu)
    val item = menu.getItem(0)
    assertThat(editMenu.onActionItemClicked(mode, item)).isTrue()
    assertThat(editMenu.onActionItemClicked(mode, item)).isTrue()
    assertThat(presses).containsExactly(listOf("quote", "A😀B", 1, 3))
    assertThat(view.text.toString()).isEqualTo("A😀B")
    verify(mode).finish()
  }

  @Test
  fun acceptsMenuItemWrappersWithTheSameNativeId() {
    editMenu.prepare(mode, menu)
    val item = menu.getItem(0)
    val wrapper = mock<MenuItem>()
    whenever(wrapper.itemId).thenReturn(item.itemId)
    whenever(wrapper.groupId).thenReturn(item.groupId)
    editMenu.onActionItemClicked(mode, wrapper)
    assertThat(presses).containsExactly(listOf("quote", "A😀B", 1, 3))
  }

  @Test
  fun normalizesReversedSelections() {
    view.setSelection(3, 1)
    editMenu.prepare(mode, menu)
    editMenu.onActionItemClicked(mode, menu.getItem(0))
    assertThat(presses).containsExactly(listOf("quote", "A😀B", 1, 3))
  }

  @Test
  fun doesNotOfferActionsForCollapsedSelectionOrDisallowedInput() {
    view.setSelection(1)
    editMenu.prepare(mode, menu)
    assertThat(menu.size()).isZero()
    view.setSelection(1, 3)
    allowed = false
    editMenu.prepare(mode, menu)
    assertThat(menu.size()).isZero()
  }

  @Test
  fun ignoresActionAfterNativeTextChanges() {
    editMenu.prepare(mode, menu)
    val item = menu.getItem(0)
    view.setText("AXXB")
    view.setSelection(1, 3)
    editMenu.onActionItemClicked(mode, item)
    assertThat(presses).isEmpty()
  }

  @Test
  fun ignoresActionAfterSelectionChanges() {
    editMenu.prepare(mode, menu)
    val item = menu.getItem(0)
    view.setSelection(0, 1)
    editMenu.onActionItemClicked(mode, item)
    assertThat(presses).isEmpty()
  }

  @Test
  fun ignoresActionsRemovedWhileMenuIsOpen() {
    editMenu.prepare(mode, menu)
    val item = menu.getItem(0)
    editMenu.items = emptyList()
    verify(mode).invalidate()
    editMenu.onActionItemClicked(mode, item)
    assertThat(presses).isEmpty()
    editMenu.prepare(mode, menu)
    assertThat(menu.size()).isZero()
  }

  @Test
  fun ignoresActionIfInputBecomesDisallowed() {
    editMenu.prepare(mode, menu)
    val item = menu.getItem(0)
    allowed = false
    editMenu.onActionItemClicked(mode, item)
    assertThat(presses).isEmpty()
  }

  @Test
  fun ignoresActionAfterDetachOrActionModeDestruction() {
    editMenu.prepare(mode, menu)
    val item = menu.getItem(0)
    editMenu.detach()
    editMenu.onActionItemClicked(mode, item)
    assertThat(presses).isEmpty()
    editMenu.prepare(mode, menu)
    val nextItem = menu.getItem(0)
    editMenu.onDestroyActionMode(mode)
    editMenu.onActionItemClicked(mode, nextItem)
    assertThat(presses).isEmpty()
  }

  @Test
  fun ignoresItemsFromAnEarlierMenuPreparation() {
    editMenu.prepare(mode, menu)
    val oldItem = menu.getItem(0)
    editMenu.prepare(mode, menu)
    editMenu.onActionItemClicked(mode, oldItem)
    assertThat(presses).isEmpty()
    editMenu.onActionItemClicked(mode, menu.getItem(0))
    assertThat(presses).hasSize(1)
  }
}

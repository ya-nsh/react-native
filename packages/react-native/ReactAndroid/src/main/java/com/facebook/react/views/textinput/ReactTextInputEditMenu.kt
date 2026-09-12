/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.react.views.textinput

import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText

/** Owns only app-defined items; platform actions remain under the Editor's control. */
internal class ReactTextInputEditMenu(
    private val view: EditText,
    private val isAllowed: () -> Boolean,
    private val onPress: (String, String, Int, Int) -> Unit,
) {
  data class Item(val id: String, val title: String)

  private data class Action(val item: Item, val text: String, val start: Int, val end: Int)

  private val groupId = View.generateViewId()
  private val actions = mutableMapOf<Int, Action>()
  private var actionMode: ActionMode? = null

  var items: List<Item> = emptyList()
    set(value) {
      if (field == value) {
        return
      }
      field = value
      actions.clear()
      actionMode?.invalidate()
    }

  fun prepare(mode: ActionMode, menu: Menu) {
    actionMode = mode
    menu.removeGroup(groupId)
    actions.clear()
    if (items.isEmpty() || !isAllowed()) {
      return
    }
    val text = view.text?.toString() ?: return
    val start = minOf(view.selectionStart, view.selectionEnd)
    val end = maxOf(view.selectionStart, view.selectionEnd)
    if (start < 0 || start >= end || end > text.length) {
      return
    }
    for (item in items) {
      val menuItem = menu.add(groupId, View.generateViewId(), Menu.CATEGORY_SECONDARY, item.title)
      menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
      actions[menuItem.itemId] = Action(item, text, start, end)
    }
  }

  fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
    if (item.groupId != groupId) {
      return false
    }
    // Consume stale custom actions, but never reinterpret them as system actions.
    val action = actions[item.itemId] ?: return true
    if (mode !== actionMode ||
        !view.isAttachedToWindow ||
        !view.hasFocus() ||
        !isAllowed() ||
        view.text?.toString() != action.text ||
        minOf(view.selectionStart, view.selectionEnd) != action.start ||
        maxOf(view.selectionStart, view.selectionEnd) != action.end) {
      return true
    }
    actions.clear()
    onPress(action.item.id, action.text, action.start, action.end)
    mode.finish()
    return true
  }

  fun onDestroyActionMode(mode: ActionMode) {
    if (mode === actionMode) {
      actions.clear()
      actionMode = null
    }
  }

  fun detach() {
    actions.clear()
    val mode = actionMode
    actionMode = null
    mode?.finish()
  }
}

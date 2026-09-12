/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.react.views.textinput

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.events.Event

internal class ReactTextInputEditMenuItemPressEvent(
    surfaceId: Int,
    viewId: Int,
    private val actionId: String,
    private val text: String,
    private val selectionStart: Int,
    private val selectionEnd: Int,
    private val eventCount: Int,
) : Event<ReactTextInputEditMenuItemPressEvent>(surfaceId, viewId) {
  override fun getEventName(): String = "topEditMenuItemPress"

  override fun canCoalesce(): Boolean = false

  override fun getEventData(): WritableMap =
      Arguments.createMap().apply {
        putString("id", actionId)
        putString("text", text)
        putInt("target", viewTag)
        putInt("eventCount", eventCount)
        putMap(
            "selection",
            Arguments.createMap().apply {
              putInt("start", selectionStart)
              putInt("end", selectionEnd)
            },
        )
      }
}

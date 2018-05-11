/*
 * Copyright (C) 2018 paphonb@xda
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.paphonb.androidpify.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.view.*
import android.widget.FrameLayout
import xyz.paphonb.androidpify.R

@SuppressLint("ViewConstructor")
class DecorLayout(context: Context, private val window: Window) : FrameLayout(context) {

    private val floatingInsets = Rect()
    private val frameOffsets = Rect()

    private val contentContainer: View
    private val actionBarContainer: View
    private val statusBarBackground: View
    private val navigationBarBackground: View
    private val navigationBarDivider: View
    private val navigationBarDividerSize =
            context.resources.getDimensionPixelSize(R.dimen.nav_bar_divider_size)

    var actionBarElevation: Float
        get() = actionBarContainer.elevation
        set(value) { actionBarContainer.elevation = value }

    init {
        fitsSystemWindows = false
        LayoutInflater.from(context).inflate(R.layout.decor_layout, this)

        contentContainer = findViewById(R.id.content_container)
        actionBarContainer = findViewById(R.id.action_bar_container)
        statusBarBackground = findViewById(R.id.status_bar_bg)
        navigationBarBackground = findViewById(R.id.nav_bar_bg)
        navigationBarDivider = findViewById(R.id.nav_bar_divider)
        navigationBarDivider.visibility = View.GONE
    }

    override fun onApplyWindowInsets(i: WindowInsets): WindowInsets {
        var insets = i
        val attrs = window.attributes
        floatingInsets.setEmpty()
        if (attrs.flags and WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN == 0) {
            // For dialog windows we want to make sure they don't go over the status bar or nav bar.
            // We consume the system insets and we will reuse them later during the measure phase.
            // We allow the app to ignore this and handle insets itself by using
            // FLAG_LAYOUT_IN_SCREEN.
            if (attrs.height == WindowManager.LayoutParams.WRAP_CONTENT) {
                floatingInsets.top = insets.systemWindowInsetTop
                floatingInsets.bottom = insets.systemWindowInsetBottom
                insets = insets.replaceSystemWindowInsets(insets.systemWindowInsetLeft, 0,
                        insets.systemWindowInsetRight, 0)
            }
            if (window.attributes.width == WindowManager.LayoutParams.WRAP_CONTENT) {
                floatingInsets.left = insets.systemWindowInsetTop
                floatingInsets.right = insets.systemWindowInsetBottom
                insets = insets.replaceSystemWindowInsets(0, insets.systemWindowInsetTop,
                        0, insets.systemWindowInsetBottom)
            }
        }
        frameOffsets.set(insets.systemWindowInsetLeft, insets.systemWindowInsetTop,
                insets.stableInsetRight, insets.stableInsetBottom)
        updateStatusGuard(insets)
        updateNavigationGuard(insets)
        return insets.consumeSystemWindowInsets()
    }

    private fun updateStatusGuard(insets: WindowInsets) {
        with(statusBarBackground.layoutParams as LayoutParams) {
            width = LayoutParams.MATCH_PARENT
            height = insets.systemWindowInsetTop
            leftMargin = insets.systemWindowInsetLeft
            rightMargin = insets.systemWindowInsetRight
            statusBarBackground.layoutParams = this
        }

        with(contentContainer.layoutParams as LayoutParams) {
            topMargin = insets.systemWindowInsetTop
            contentContainer.layoutParams = this
        }
    }

    @SuppressLint("RtlHardcoded")
    private fun updateNavigationGuard(insets: WindowInsets) {
        val toLeft = isNavBarToLeftEdge(insets.systemWindowInsetBottom, insets.systemWindowInsetLeft)
        val toRight = isNavBarToRightEdge(insets.systemWindowInsetBottom, insets.systemWindowInsetRight)
        val toBottom = !toLeft && !toRight
        val size = getNavBarSize(insets.systemWindowInsetBottom,
                insets.systemWindowInsetRight, insets.systemWindowInsetLeft)
        val dividerSize = navigationBarDividerSize
        val dividerMargin = size - dividerSize
        with(navigationBarBackground.layoutParams as LayoutParams) {
            if (toBottom) {
                width = LayoutParams.MATCH_PARENT
                height = size
                gravity = Gravity.BOTTOM
            } else {
                width = size
                height = LayoutParams.MATCH_PARENT
                gravity = if (toLeft) Gravity.LEFT else Gravity.RIGHT
            }
            navigationBarBackground.layoutParams = this
        }

        with(navigationBarDivider.layoutParams as LayoutParams) {
            if (toBottom) {
                width = LayoutParams.MATCH_PARENT
                height = dividerSize
                leftMargin = 0
                rightMargin = 0
                bottomMargin = dividerMargin
                gravity = Gravity.BOTTOM
            } else {
                width = dividerSize
                height = LayoutParams.MATCH_PARENT
                bottomMargin = 0
                if (toLeft) {
                    leftMargin = dividerMargin
                    rightMargin = 0
                    gravity = Gravity.LEFT
                } else {
                    leftMargin = 0
                    rightMargin = dividerMargin
                    gravity = Gravity.RIGHT
                }
            }
            navigationBarDivider.layoutParams = this
        }

        with(contentContainer.layoutParams as LayoutParams) {
            leftMargin = insets.systemWindowInsetLeft
            rightMargin = insets.systemWindowInsetRight
            bottomMargin = insets.systemWindowInsetBottom
            contentContainer.layoutParams = this
        }
    }

    private fun isNavBarToRightEdge(bottomInset: Int, rightInset: Int): Boolean {
        return bottomInset == 0 && rightInset > 0
    }

    private fun isNavBarToLeftEdge(bottomInset: Int, leftInset: Int): Boolean {
        return bottomInset == 0 && leftInset > 0
    }

    private fun getNavBarSize(bottomInset: Int, rightInset: Int, leftInset: Int): Int {
        return when {
            isNavBarToRightEdge(bottomInset, rightInset) -> rightInset
            isNavBarToLeftEdge(bottomInset, leftInset) -> leftInset
            else -> bottomInset
        }
    }
}
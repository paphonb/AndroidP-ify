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

package xyz.paphonb.androidpify.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import xyz.paphonb.androidpify.MainHook


@ColorInt
fun Context.getColorAccent(): Int {
    return getColorAttr(android.R.attr.colorAccent)
}

@ColorInt
fun Context.getColorError(): Int {
    return getColorAttr(android.R.attr.colorError)
}

@ColorInt
fun Context.getDefaultColor(resId: Int): Int {
    return resources.getColorStateList(resId, theme).defaultColor
}

@ColorInt
fun Context.getDisabled(inputColor: Int): Int {
    return applyAlphaAttr(android.R.attr.disabledAlpha, inputColor)
}

@ColorInt
fun Context.applyAlphaAttr(attr: Int, inputColor: Int): Int {
    val ta = obtainStyledAttributes(intArrayOf(attr))
    val alpha = ta.getFloat(0, 0f)
    ta.recycle()
    return applyAlpha(alpha, inputColor)
}

@ColorInt
fun applyAlpha(a: Float, inputColor: Int): Int {
    var alpha = a
    alpha *= Color.alpha(inputColor)
    return Color.argb(alpha.toInt(), Color.red(inputColor), Color.green(inputColor),
            Color.blue(inputColor))
}

@ColorInt
fun Context.getColorAttr(attr: Int): Int {
    val ta = obtainStyledAttributes(intArrayOf(attr))
    @ColorInt val colorAccent = ta.getColor(0, 0)
    ta.recycle()
    return colorAccent
}

fun Context.getThemeAttr(attr: Int): Int {
    val ta = obtainStyledAttributes(intArrayOf(attr))
    val theme = ta.getResourceId(0, 0)
    ta.recycle()
    return theme
}

fun Context.getDrawablAttr(attr: Int): Drawable? {
    val ta = obtainStyledAttributes(intArrayOf(attr))
    val drawable = ta.getDrawable(0)
    ta.recycle()
    return drawable
}

fun Any.logI(msg: String) {
    MainHook.logI(javaClass.simpleName, msg)
}

fun Any.logE(msg: String, throwable: Throwable? = null) {
    MainHook.logE(javaClass.simpleName, msg, throwable)
}

fun TextView.setGoogleSans(style: String = "Regular"): Boolean {
    if (!ConfigUtils.misc.googleSans) return false
    typeface = Typeface.createFromAsset(ResourceUtils.getInstance(context).assets, "fonts/GoogleSans-$style.ttf")
    return true
}

val View.resUtils get() = ResourceUtils.getInstance(context)!!

inline fun ViewGroup.forEachChild(body: (View) -> Unit) {
    for (i in (0 until childCount)) body(getChildAt(i))
}

fun ViewGroup.moveChildsTo(newParent: ViewGroup) {
    while (childCount > 0) {
        val child = getChildAt(0)
        removeViewAt(0)
        newParent.addView(child)
    }
}

fun Resources.getIdSystemUi(name: String) = getIdentifier(name, "id", MainHook.PACKAGE_SYSTEMUI)

fun Resources.getLayoutSystemUi(name: String) = getIdentifier(name, "layout", MainHook.PACKAGE_SYSTEMUI)

fun Resources.getDimenSystemUi(name: String) = getDimensionPixelSize(getIdentifier(name, "dimen", MainHook.PACKAGE_SYSTEMUI))

inline fun logThrowable(tag: String, message: String, body: () -> Unit) {
    try {
        body()
    } catch (t: Throwable) {
        MainHook.logE(tag, message, t)
        throw t
    }
}
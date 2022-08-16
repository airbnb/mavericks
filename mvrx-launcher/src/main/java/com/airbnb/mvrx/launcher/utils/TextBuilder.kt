package com.airbnb.mvrx.launcher.utils

import android.content.Context
import android.graphics.Typeface
import android.graphics.Typeface.BOLD
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import androidx.annotation.ColorRes
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat

internal inline fun buildText(context: Context, builder: TextBuilder.() -> Unit): CharSequence {
    return TextBuilder(context).apply(builder).build()
}

/**
 * Helper to build styled text.
 */
internal class TextBuilder(private val context: Context) {

    private val spannableStringBuilder = SpannableStringBuilder()

    fun append(@StringRes textRes: Int): TextBuilder {
        return append(context.getString(textRes))
    }

    fun append(@StringRes textRes: Int, vararg formatArgs: Any): TextBuilder {
        return append(context.getString(textRes, *formatArgs))
    }

    fun append(text: CharSequence): TextBuilder {
        spannableStringBuilder.append(text)
        return this
    }

    fun appendQuantityRes(
        @PluralsRes textRes: Int,
        quantity: Int,
        vararg formatArgs: Any
    ): TextBuilder {
        return append(context.resources.getQuantityString(textRes, quantity, *formatArgs))
    }

    fun appendWithSpans(text: CharSequence, vararg spans: Any): TextBuilder {
        val start = spannableStringBuilder.length
        val end = start + text.length
        spannableStringBuilder.append(text)
        for (span in spans) {
            spannableStringBuilder.setSpan(
                span,
                start,
                end,
                SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return this
    }

    fun appendWithColor(@StringRes textRes: Int, @ColorRes colorRes: Int): TextBuilder {
        return appendWithColor(context.getString(textRes), colorRes)
    }

    fun appendWithColor(text: CharSequence, @ColorRes colorRes: Int): TextBuilder {
        return appendWithSpans(text, ForegroundColorSpan(ContextCompat.getColor(context, colorRes)))
    }

    /**
     * @param sizeModifier Adjust the size of the text relative to the size of the rest of the TextView.
     * A value of 1.0 will result in the same size, less than 1.0 will make it smaller, and greater than 1.0 will make this text larger.
     */
    fun appendWithRelativeSize(text: CharSequence, sizeModifier: Float) =
        appendWithSpans(text, RelativeSizeSpan(sizeModifier))

    fun appendUnderline(text: CharSequence): TextBuilder {
        return appendWithSpans(text, UnderlineSpan())
    }

    fun appendItalic(text: CharSequence): TextBuilder {
        return appendWithSpans(text, StyleSpan(Typeface.ITALIC))
    }

    fun appendBold(text: CharSequence): TextBuilder {
        return appendWithSpans(text, StyleSpan(BOLD))
    }

    fun appendStrikeThrough(text: CharSequence): TextBuilder {
        return appendWithSpans(text, StrikethroughSpan())
    }

    fun appendLineBreak(): TextBuilder = append("\n")

    fun appendSpace(): TextBuilder = append(" ")

    fun build(): CharSequence = spannableStringBuilder
}

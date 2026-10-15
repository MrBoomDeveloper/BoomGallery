package com.mrboomdev.gallery.extensions

import com.google.android.material.appbar.AppBarLayout
import org.fossify.commons.views.MySearchMenu

/**
 * Lets the app bar scroll away while scrolling the content down and come back on scrolling up.
 * The scroll flags have to be set on the app bar children, they define the scrollable range.
 */
fun MySearchMenu.setCollapsibleOnScroll(collapsible: Boolean) {
    val scrollFlags = if (collapsible) {
        AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or
                AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS or
                AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP
    } else {
        0
    }

    for (i in 0 until childCount) {
        val layoutParams = getChildAt(i).layoutParams as? AppBarLayout.LayoutParams ?: continue
        layoutParams.scrollFlags = scrollFlags
    }

    requestLayout()
}

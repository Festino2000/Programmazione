package com.example.mobileapp

import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class FabMenuController(
    private val fabMenu: ExtendedFloatingActionButton,
    private val fabButtons: List<MaterialButton>,
    private val rootView: View
) {
    private var isMenuOpen = false

    init {
        fabMenu.setOnClickListener {
            toggleMenu()
        }

        // Detect touch anywhere in the root layout
        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            attachTouchInterceptor(rootView)
        }
    }

    private fun attachTouchInterceptor(view: View) {
        if (view !is ViewGroup) return

        view.setOnTouchListener { v, event ->
            if (isMenuOpen && event.action == MotionEvent.ACTION_DOWN) {
                val touchedOutsideButtons = fabButtons.all { !isTouchInsideView(event, it) }
                val touchedOutsideFab = !isTouchInsideView(event, fabMenu)

                if (touchedOutsideButtons && touchedOutsideFab) {
                    closeMenu()
                    v.performClick()
                }
            }
            false // Don't consume the event
        }

        for (i in 0 until view.childCount) {
            attachTouchInterceptor(view.getChildAt(i))
        }
    }

    private fun isTouchInsideView(event: MotionEvent, view: View): Boolean {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val x = event.rawX.toInt()
        val y = event.rawY.toInt()
        return x >= location[0] && x <= location[0] + view.width &&
                y >= location[1] && y <= location[1] + view.height
    }

    private fun toggleMenu() {
        if (isMenuOpen) closeMenu() else openMenu()
    }

    fun closeMenu() {
        isMenuOpen = false
        fabButtons.forEach { it.visibility = View.GONE }
    }

    fun openMenu() {
        isMenuOpen = true
        fabButtons.forEach { it.visibility = View.VISIBLE }
    }

    fun isOpen(): Boolean = isMenuOpen
}

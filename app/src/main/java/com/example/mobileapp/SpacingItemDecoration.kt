package com.example.mobileapp

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SpacingItemDecoration(private val spacing: Int,  private val spanCount: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view) // posizione dell'elemento
        val column = position % spanCount // colonna dell'elemento

        // Imposta i margini uniformi
        outRect.left = spacing - column * spacing / spanCount
        outRect.right = (column + 1) * spacing / spanCount
        outRect.bottom = spacing

        // Margine superiore per la prima riga
        if (position < spanCount) {
            outRect.top = spacing
        } else {
            outRect.top = spacing / 2
        }
    }
}

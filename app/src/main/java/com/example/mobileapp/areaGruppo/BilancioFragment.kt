package com.example.mobileapp.areaGruppo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.example.mobileapp.R

class BilancioFragment : Fragment(R.layout.fragment_bilancio) {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = TextView(requireContext()).apply {
        text = "Bilancio"
        textSize = 24f
        setTextColor(ResourcesCompat.getColor(resources, R.color.bilancio_text_color, null))
    }
}

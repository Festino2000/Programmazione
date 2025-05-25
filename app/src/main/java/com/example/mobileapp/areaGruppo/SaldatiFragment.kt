package com.example.mobileapp.areaGruppo

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.mobileapp.R

class SaldatiFragment : Fragment(R.layout.fragment_saldati) {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = TextView(requireContext()).apply {
        text = "Saldati"
        textSize = 24f
    }
}
package com.example.mobileapp.areaGruppo

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.mobileapp.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class SchermataSpeseFragment : Fragment(R.layout.fragment_schermata_spese) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val idGruppo = arguments?.getString("idGruppo") ?: return

        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = view.findViewById<ViewPager2>(R.id.viewPager)

        val adapter = GruppoPagerAdapter(this, idGruppo)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Spese"
                1 -> "Saldati"
                2 -> "Bilancio"
                else -> ""
            }
        }.attach()
    }
}


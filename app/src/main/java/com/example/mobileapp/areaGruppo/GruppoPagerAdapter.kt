// GruppoPagerAdapter.kt
package com.example.mobileapp.areaGruppo

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.mobileapp.areaGruppo.BilancioFragment
import com.example.mobileapp.areaGruppo.SaldatiFragment
import com.example.mobileapp.areaGruppo.SpesaCondivisaFragment

class GruppoPagerAdapter(
    fragment: Fragment,
    private val idGruppo: String
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        val bundle = Bundle().apply {
            putString("idGruppo", idGruppo)
        }

        val fragment = when (position) {
            0 -> SpesaCondivisaFragment()
            1 -> SaldatiFragment()
            2 -> BilancioFragment()
            else -> Fragment()
        }

        fragment.arguments = bundle
        return fragment
    }
}

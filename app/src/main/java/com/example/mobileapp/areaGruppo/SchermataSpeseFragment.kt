package com.example.mobileapp.areaGruppo

import android.app.AlertDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.mobileapp.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.firestore.FirebaseFirestore

class SchermataSpeseFragment : Fragment(R.layout.fragment_schermata_spese) {

    private lateinit var idGruppoFirestore: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val idGruppo = arguments?.getString("idGruppo") ?: return
        idGruppoFirestore = idGruppo

        // Setup toolbar menu
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.clear()
                menuInflater.inflate(R.menu.menu_gruppo_toolbar, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_membri -> {
                        mostraDialogMembri()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner)

        // Tab + ViewPager setup
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

    private fun mostraDialogMembri() {
        FirebaseFirestore.getInstance()
            .collection("Gruppi")
            .document(idGruppoFirestore)
            .get()
            .addOnSuccessListener { document ->
                val utentiID = document.get("utentiID") as? List<String> ?: return@addOnSuccessListener

                FirebaseFirestore.getInstance()
                    .collection("Utenti")
                    .whereIn("utenteID", utentiID)
                    .get()
                    .addOnSuccessListener { utentiDocs ->
                        val listaUtenti = utentiDocs.mapNotNull {
                            val id = it.getString("utenteID")
                            val nick = it.getString("nickname")
                            if (id != null && nick != null) Utente(id, nick) else null
                        }

                        MembriGruppoDialog(listaUtenti)
                            .show(parentFragmentManager, "MembriGruppoDialog")
                    }
            }
    }
}

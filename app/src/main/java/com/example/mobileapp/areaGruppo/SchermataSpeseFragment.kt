package com.example.mobileapp.areaGruppo

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.mobileapp.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class SchermataSpeseFragment : Fragment(R.layout.fragment_schermata_spese) {

    private lateinit var idGruppoFirestore: String
    private var listaUtentiGruppo: List<Utente> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val idGruppo = arguments?.getString("idGruppo") ?: return
        idGruppoFirestore = idGruppo

        caricaUtentiGruppo()

        requireActivity().findViewById<TabLayout>(R.id.tabLayout)?.visibility = View.VISIBLE

        val tabLayout = requireActivity().findViewById<TabLayout>(R.id.tabLayout)
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

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                requireActivity().invalidateOptionsMenu()
            }
        })
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.clear()
                menuInflater.inflate(R.menu.menu_gruppo_fragment_schermata, menu)

                val currentTab = viewPager.currentItem
                menu.findItem(R.id.action_filter)?.isVisible = (currentTab == 1)
                menu.findItem(R.id.action_search)?.isVisible = (currentTab == 1)

                menu.findItem(R.id.action_membri)?.isVisible = (currentTab == 0 || currentTab == 1)

                val searchItem = menu.findItem(R.id.action_search)
                val searchView = searchItem.actionView as SearchView
                searchView.queryHint = "Cerca spese..."

                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean = true
                    override fun onQueryTextChange(newText: String?): Boolean {
                        (getCurrentFragment() as? SaldatiFragment)?.eseguiRicerca(newText.orEmpty())
                        return true
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                val fragment = getCurrentFragment()
                return when (menuItem.itemId) {
                    R.id.action_filter -> {
                        (fragment as? SaldatiFragment)?.mostraDialogFiltri()
                        true
                    }
                    R.id.action_membri -> {
                        mostraDialogMembri()
                        true
                    }
                    else -> false
                }
            }

        }, viewLifecycleOwner)

    }

    private fun caricaUtentiGruppo() {
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
                        listaUtentiGruppo = utentiDocs.mapNotNull {
                            val id = it.getString("utenteID")
                            val nick = it.getString("nickname")
                            if (id != null && nick != null) Utente(id, nick) else null
                        }
                    }
            }
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
    private fun getCurrentFragment(): Fragment? {
        val viewPager = requireView().findViewById<ViewPager2>(R.id.viewPager)
        val currentItem = viewPager.currentItem
        return childFragmentManager.findFragmentByTag("f$currentItem")
    }
    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().findViewById<TabLayout>(R.id.tabLayout)?.visibility = View.GONE
    }


}

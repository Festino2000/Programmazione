package com.example.mobileapp.areaGruppo.gruppoFragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.mobileapp.R
import com.example.mobileapp.adapters.GruppoPagerAdapter
import com.example.mobileapp.areaGruppo.gruppoDialogs.MembriGruppoDialog
import com.example.mobileapp.areaGruppo.gruppoDataClasses.Utente
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.example.mobileapp.areaGruppo.gruppoActivities.GruppoActivity

// Fragment principale che gestisce la schermata delle spese del gruppo
class SchermataSpeseFragment : Fragment(R.layout.fragment_schermata_spese) {

    // ID del gruppo preso da Firestore
    private lateinit var idGruppoFirestore: String
    // Lista degli utenti del gruppo
    private var listaUtentiGruppo: List<Utente> = emptyList()

    // Richiamato quando la view è stata creata
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Recupera l'id del gruppo passato come argomento
        val idGruppo = arguments?.getString("idGruppo") ?: return
        idGruppoFirestore = idGruppo

        // Carica gli utenti del gruppo da Firestore
        caricaUtentiGruppo()

        // Rende visibile la TabLayout (le tab in alto)
        requireActivity().findViewById<TabLayout>(R.id.tabLayout)?.visibility = View.VISIBLE

        // Inizializza tabLayout e viewPager per la navigazione tra le schede
        val tabLayout = requireActivity().findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = view.findViewById<ViewPager2>(R.id.viewPager)
        val adapter = GruppoPagerAdapter(this, idGruppo)
        viewPager.adapter = adapter

        // Collega le tab al ViewPager
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Spese"
                1 -> "Saldati"
                2 -> "Statistiche"
                else -> ""
            }
        }.attach()

        // Quando cambia pagina, aggiorna il menu
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                requireActivity().invalidateOptionsMenu()
            }
        })

        // Gestisce la creazione e la selezione delle voci di menu nella toolbar
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.clear()
                menuInflater.inflate(R.menu.menu_gruppo_fragment_schermata, menu)

                // Mostra o nasconde le azioni in base alla tab attiva
                val currentTab = viewPager.currentItem
                menu.findItem(R.id.action_filter)?.isVisible = (currentTab == 1)
                menu.findItem(R.id.action_search)?.isVisible = (currentTab == 1)
                menu.findItem(R.id.action_membri)?.isVisible = (currentTab == 0 || currentTab == 1)

                // Imposta la SearchView per cercare tra le spese
                val searchItem = menu.findItem(R.id.action_search)
                val searchView = searchItem.actionView as SearchView
                searchView.queryHint = "Cerca spese..."

                // Listener per la ricerca in tempo reale
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
                    R.id.action_abbandona_gruppo -> {
                        mostraDialogAbbandonaGruppo()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner)
    }


    // Carica la lista degli utenti del gruppo da Firestore
    private fun caricaUtentiGruppo() {
        FirebaseFirestore.getInstance()
            .collection("Gruppi")
            .document(idGruppoFirestore)
            .get()
            .addOnSuccessListener { document ->
                // Recupera la lista degli ID degli utenti dal documento
                val utentiID = (document.get("utentiID") as? List<*>)?.mapNotNull { it as? String }
                    ?: return@addOnSuccessListener

                Log.d("DEBUG", "ID Utenti da cercare: $utentiID")

                if (utentiID.isEmpty()) return@addOnSuccessListener

                // Recupera i dettagli degli utenti a partire dagli ID
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

                        Log.d("DEBUG", "Utenti caricati: ${listaUtentiGruppo.size}")
                    }
                    .addOnFailureListener {
                        Log.e("Firestore", "Errore nella query utenti", it)
                    }
            }
            .addOnFailureListener {
                Log.e("Firestore", "Errore nel recupero gruppo", it)
            }
    }

    // Mostra un dialog con la lista dei membri del gruppo
    private fun mostraDialogMembri() {
        FirebaseFirestore.getInstance()
            .collection("Gruppi")
            .document(idGruppoFirestore)
            .get()
            .addOnSuccessListener { document ->
                val utentiID = (document.get("utentiID") as? List<*>)?.mapNotNull { it as? String }

                if (utentiID.isNullOrEmpty()) {
                    Toast.makeText(requireContext(), "Nessun membro trovato", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                Log.d("DEBUG", "ID membri trovati: $utentiID")

                // Recupera i documenti degli utenti usando gli ID come documentId
                FirebaseFirestore.getInstance()
                    .collection("Utenti")
                    .whereIn(FieldPath.documentId(), utentiID)
                    .get()
                    .addOnSuccessListener { utentiDocs ->
                        val listaUtenti = utentiDocs.mapNotNull {
                            val id = it.id // usa l'ID del documento
                            val nick = it.getString("nickname")
                            if (nick != null) Utente(id, nick) else null
                        }

                        if (listaUtenti.isEmpty()) {
                            Toast.makeText(
                                requireContext(),
                                "Nessun utente corrispondente trovato",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@addOnSuccessListener
                        }

                        // Mostra il dialog con la lista dei membri
                        MembriGruppoDialog(listaUtenti)
                            .show(parentFragmentManager, "MembriGruppoDialog")
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            requireContext(),
                            "Errore nella query utenti",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("Firestore", "Errore query utenti", it)
                    }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Errore nel recupero gruppo", Toast.LENGTH_SHORT).show()
                Log.e("Firestore", "Errore gruppo", it)
            }
    }

    // Restituisce il fragment attualmente visibile nel ViewPager
    private fun getCurrentFragment(): Fragment? {
        val viewPager = requireView().findViewById<ViewPager2>(R.id.viewPager)
        val currentItem = viewPager.currentItem
        return childFragmentManager.findFragmentByTag("f$currentItem")
    }

    // Quando la view viene distrutta, nasconde la TabLayout
    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().findViewById<TabLayout>(R.id.tabLayout)?.visibility = View.GONE
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_membri -> { // <-- CORRETTO
                mostraDialogMembri()
                true
            }
            R.id.action_abbandona_gruppo -> {
                mostraDialogAbbandonaGruppo()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun abbandonaGruppo() {
        val gruppoId = arguments?.getString("idGruppo") ?: return
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val gruppoRef = FirebaseFirestore.getInstance()
            .collection("gruppi")
            .document(gruppoId)

        gruppoRef.update("membri", FieldValue.arrayRemove(userId))
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Hai abbandonato il gruppo", Toast.LENGTH_SHORT).show()
                val intent = Intent(requireContext(), GruppoActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                activity?.finish()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Errore durante l'abbandono", Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostraDialogAbbandonaGruppo() {
        AlertDialog.Builder(requireContext())
            .setTitle("Abbandona gruppo")
            .setMessage("Sei sicuro di voler uscire da questo gruppo?")
            .setPositiveButton("Sì") { _, _ ->
                abbandonaGruppo()
            }
            .setNegativeButton("Annulla", null)
            .show()
    }
}
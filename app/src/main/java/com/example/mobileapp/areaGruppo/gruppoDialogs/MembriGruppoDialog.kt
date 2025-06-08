package com.example.mobileapp.areaGruppo.gruppoDialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapp.R
import com.example.mobileapp.adapters.UtenteAdapter
import com.example.mobileapp.areaGruppo.gruppoDataClasses.Utente

class MembriGruppoDialog(
    private val membri: List<Utente>
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view: View = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_membri_gruppo, null)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewMembriGruppo)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = UtenteAdapter(membri)

        return AlertDialog.Builder(requireContext())
            .setTitle("Membri del Gruppo")
            .setView(view)
            .setPositiveButton("Chiudi", null)
            .create()
    }
}

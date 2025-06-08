package com.example.mobileapp.areaPersonale.singoloFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.mobileapp.R

class ListaSpeseFragment : Fragment(R.layout.fragment_lista_spese) {

    companion object {
        private const val ARG_CATEGORIA = "categoria"

        fun newInstance(category: String): ListaSpeseFragment {
            val fragment = ListaSpeseFragment()
            val args =
                Bundle() //bundle per passare i dati (poi imposta la cateforia come argomento e restituisce l'istanza del fragment)
            args.putString(ARG_CATEGORIA, category)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView( //effettua l'inflate(creare una vista a partire da un file xml) del layout
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_lista_spese, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val category = arguments?.getString(ARG_CATEGORIA) ?: "Sconosciuto"
        val textView = view.findViewById<TextView>(R.id.categoria)
        textView.text = "Lista delle spese per: $category"
    }
}
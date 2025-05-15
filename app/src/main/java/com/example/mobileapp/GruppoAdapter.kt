package com.example.mobileapp
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GruppoAdapter(private val gruppiList: List<String>) : RecyclerView.Adapter<GruppoAdapter.GruppiViewHolder>() {

    inner class GruppiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewNomeGruppo: TextView = itemView.findViewById(R.id.textViewNomeGruppo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GruppiViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_gruppo, parent, false)
        return GruppiViewHolder(view)
    }

    override fun onBindViewHolder(holder: GruppiViewHolder, position: Int) {
        holder.textViewNomeGruppo.text = gruppiList[position]
    }

    override fun getItemCount(): Int {
        return gruppiList.size
    }
}
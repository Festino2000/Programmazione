import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapp.R
import com.example.mobileapp.areaGruppo.Gruppo

class GruppoAdapter(private val gruppiList: List<Gruppo>) :
    RecyclerView.Adapter<GruppoAdapter.GruppiViewHolder>() {

    inner class GruppiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titolo: TextView = itemView.findViewById(R.id.textViewTitoloGruppo)
        val descrizione: TextView = itemView.findViewById(R.id.textViewDescrizioneGruppo)
        val idUnico: TextView = itemView.findViewById(R.id.textViewIdGruppo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GruppiViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gruppo, parent, false)
        return GruppiViewHolder(view)
    }

    override fun onBindViewHolder(holder: GruppiViewHolder, position: Int) {
        val gruppo = gruppiList[position]
        holder.titolo.text = gruppo.titolo
        holder.descrizione.text = gruppo.descrizione
        holder.idUnico.text = "ID: ${gruppo.idUnico}"
    }

    override fun getItemCount(): Int = gruppiList.size
}

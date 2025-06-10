import com.example.mobileapp.areaGruppo.gruppoDataClasses.SpesaCondivisa
import com.example.mobileapp.areaGruppo.gruppoFragments.SpesaCondivisaFragment
import org.junit.Assert.assertEquals
import org.junit.Test


class SpesaCondivisaFragmentTest {

    @Test
    fun testCalcolaTotaliInterni() {
        val mioId = "user1"

        val spese = listOf(
            SpesaCondivisa(
                titolo = "Cena",
                descrizione = "Pizza con amici",
                giorno = 1,
                mese = 6,
                anno = 2025,
                importo = 60.0,
                idUtentiCoinvolti = listOf("user1", "user3"),
                creatoreID = "user2"
            ),
            SpesaCondivisa(
                titolo = "Cinema",
                descrizione = "Biglietti",
                giorno = 2,
                mese = 6,
                anno = 2025,
                importo = 90.0,
                idUtentiCoinvolti = listOf("user2", "user3"),
                creatoreID = "user1"
            )
        )

        val fragment = SpesaCondivisaFragment()
        val (daPagare, daRicevere) = fragment.calcolaTotali(spese, mioId)

        // Spesa 1: user1 paga 60 / 3 = 20
        // Spesa 2: user1 riceve 2 quote da 90 / 3 = 30 × 2 = 60

        assertEquals(20.0, daPagare, 0.01)
        assertEquals(60.0, daRicevere, 0.01)
    }
}

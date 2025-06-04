import androidx.room.*
import com.example.mobileapp.local.SpesaLocale
@Dao
interface SpesaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun inserisci(spesa: SpesaLocale)

    @Query("SELECT * FROM SpesaLocale")
    fun getTutte(): List<SpesaLocale>

    @Delete
    fun elimina(spesa: SpesaLocale)
}

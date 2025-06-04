import android.content.Context
import androidx.room.RoomDatabase
import androidx.room.Database
import com.example.mobileapp.local.SpesaLocale
import androidx.room.TypeConverters
import com.example.mobileapp.areaPersonale.UriListConverter
import androidx.room.Room

@Database(entities = [SpesaLocale::class], version = 1)
@TypeConverters(UriListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun spesaDao(): SpesaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "spesa_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

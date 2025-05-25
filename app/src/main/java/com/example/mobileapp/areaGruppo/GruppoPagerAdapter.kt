import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.mobileapp.areaGruppo.SaldatiFragment
import com.example.mobileapp.areaGruppo.BilancioFragment
import com.example.mobileapp.areaGruppo.SpesaCondivisaFragment

class GruppoPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> SpesaCondivisaFragment()
            1 -> SaldatiFragment()
            2 -> BilancioFragment()
            else -> Fragment()
        }
    }
}

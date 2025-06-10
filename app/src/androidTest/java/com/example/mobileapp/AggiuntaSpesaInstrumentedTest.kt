package com.example.mobileapp

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mobileapp.areaPersonale.singoloActivities.SoloActivity
import org.hamcrest.Matchers.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AggiuntaSpesaInstrumentedTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(SoloActivity::class.java)

    @Test
    fun aggiuntaSpesa_compilaCampi_e_verificaTitoloInLista() {
        // Clic sul bottone per aprire il fragment
        onView(withId(R.id.btnAggiungiSpesa)).perform(click())

        // Titolo e descrizione
        onView(withId(R.id.titoloSpesa)).perform(typeText("Test Espresso"), closeSoftKeyboard())
        onView(withId(R.id.descrizioneSpesa)).perform(typeText("Spesa testata"), closeSoftKeyboard())

        // Data
        onView(withId(R.id.DataSelezionata)).perform(click())
        Thread.sleep(500)
        onView(withText("OK")).perform(click())

        // Importo
        onView(withId(R.id.importoSpesa)).perform(typeText("42.00"), closeSoftKeyboard())

        // Categoria
        onView(withId(R.id.categoriaSpesa)).perform(click())
        onView(withId(R.id.categoriaSpesa)).perform(replaceText("Alimentari"), closeSoftKeyboard())
        Thread.sleep(500)
        onData(`is`("Alimentari")).inRoot(isPlatformPopup()).perform(click())

        // Conferma
        onView(withId(R.id.btnConfermaSpesa)).perform(click())
        Thread.sleep(1500)

        // Verifica
        onView(withText("Test Espresso")).check(matches(isDisplayed()))
    }
}

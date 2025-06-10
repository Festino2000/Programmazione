package com.example.mobileapp

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mobileapp.gestioneAccesso.LoginActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginInstrumentedTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)

    @Test
    fun loginConCredenzialiCorrette_mostraSchermataPrincipale() {
        onView(withId(R.id.editTextEmail))
            .perform(typeText("03cherry27@gmail.com"), closeSoftKeyboard())

        onView(withId(R.id.editTextPassword))
            .perform(typeText("Registrati.27"), closeSoftKeyboard())

        onView(withId(R.id.buttonLogin)).perform(click())

        // Attendi che si carichi la schermata successiva
        Thread.sleep(2000)

        // Verifica che qualcosa della schermata principale sia visibile (es. un TextView, RecyclerView, ecc.)
        onView(withId(R.id.textView3))
            .check(matches(isDisplayed()))
    }
}

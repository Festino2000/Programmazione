package com.example.mobileapp.tests

import androidx.appcompat.widget.SearchView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mobileapp.R
import com.example.mobileapp.areaPersonale.singoloActivities.SoloActivity
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RicercaSpesaTest {

    @Test
    fun ricercaMostraSoloSpeseCorrette() {
        val scenario = ActivityScenario.launch(SoloActivity::class.java)

        scenario.onActivity { activity ->
            val searchItem = activity.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
                .menu.findItem(R.id.action_search)
            val searchView = searchItem.actionView as SearchView
            searchItem.expandActionView() // forza apertura
            searchView.setQuery("spesa", true) // "spesa" dev'essere una voce visibile
        }

        // Verifica che la spesa cercata sia visibile
        onView(withText("spesa")).check(matches(isDisplayed()))

        // Verifica che una spesa non cercata non compaia
        onView(withText("Regalo")).check(doesNotExist())
    }
}

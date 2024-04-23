package com.barapp.ui.loginSignUp
import android.widget.EditText
import android.widget.TextView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.barapp.R
import com.barapp.ui.MainActivity
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4::class)
class PantallaResultadosBusquedaTest {

  @Rule
  @JvmField
  var mActivityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

  @Before
  fun login() {

    // Check if the user is already logged in by checking if a specific view is displayed
    val isUserLoggedIn = try {
      val labelDestacados = onView(
        Matchers.allOf(
          isAssignableFrom(TextView::class.java),
          withId(R.id.labelDestacados),
          isDisplayed()
        )
      )
      labelDestacados.check(matches(isDisplayed()))
      true
    } catch (e: NoMatchingViewException) {
      false
    }

    // If the user is not logged in, perform the login process
    if (!isUserLoggedIn) {
      // Enter email
      val emailInput = onView(
        Matchers.allOf(
          isDescendantOfA(withId(R.id.txtViewEmail)), isAssignableFrom(
            EditText::class.java
          )
        )
      )
      emailInput.perform(
        ViewActions.replaceText("usuariodeprueba@gmail.com"),
        ViewActions.closeSoftKeyboard()
      )

      // Enter password
      val passwordInput = onView(
        Matchers.allOf(
          isDescendantOfA(withId(R.id.txtViewContrasenia)), isAssignableFrom(
            EditText::class.java
          )
        )
      )
      passwordInput.perform(ViewActions.replaceText("prueba123"), ViewActions.closeSoftKeyboard())

      // Click on login button
      val loginButton = onView(withId(R.id.botonIngresar))
      loginButton.perform(click())

      // Wait a bit
      Thread.sleep(2000)
    }
    val searchButton = onView(withId(R.id.floating_action_button_buscar))
    searchButton.perform(click())
  }

  @Test
  fun test1EmptyRestaurantListIfTextTooSpecific() {
    val buscarInput = onView(
      Matchers.allOf(
        isDescendantOfA(withId(R.id.textField)), isAssignableFrom(
          EditText::class.java
        )
      )
    )
    buscarInput.perform(
      ViewActions.replaceText("ESTO ES UNA PRUEBA"),
      ViewActions.closeSoftKeyboard()
    )

    // Press enter on input
    onView(withId(R.id.textViewBuscar)).perform(ViewActions.pressImeActionButton())

    // Wait a bit
    Thread.sleep(2000)

    val labelListaVacia = onView(
      Matchers.allOf(
        isAssignableFrom(TextView::class.java),
        withId(R.id.textListaVacia),
        isDisplayed()
      )
    )
    labelListaVacia.check(matches(isDisplayed()))
  }

  @Test
  fun test2NotEmptyRestaurantListIfInputEmpty() {
    // Press enter on input
    onView(withId(R.id.textViewBuscar)).perform(ViewActions.pressImeActionButton())

    // Wait a bit
    Thread.sleep(2000)

    onView(withId(R.id.recyclerViewRestaurantes)).check(matches(isDisplayed()))
  }
}
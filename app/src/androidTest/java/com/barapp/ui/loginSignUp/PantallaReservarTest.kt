package com.barapp.ui.loginSignUp
import android.Manifest
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.barapp.BuildConfig
import com.barapp.R
import com.barapp.ui.MainActivity
import com.barapp.ui.recyclerViewAdapters.HorizontalRecyclerViewAdapter.RestaurantesViewHolder
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import org.hamcrest.Matcher
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
class PantallaReservarTest {

    @Rule
    @JvmField
    var mActivityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setUp() {
        mActivityScenarioRule.scenario.onActivity { activity ->
            activity.supportFragmentManager.beginTransaction()
        }
    }

    @Before
    fun setup() {
        val notificationsPermission = Manifest.permission.POST_NOTIFICATIONS
        val locationPermission = Manifest.permission.ACCESS_FINE_LOCATION
        getInstrumentation().uiAutomation.grantRuntimePermission(BuildConfig.APPLICATION_ID, notificationsPermission)
        Thread.sleep(1000)
        getInstrumentation().uiAutomation.grantRuntimePermission(BuildConfig.APPLICATION_ID, locationPermission)
    }

    @Before
    fun login() {

        // Wait a bit
        Thread.sleep(5000)

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
                ViewActions.replaceText("pruebacu@gmail.com"),
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
            passwordInput.perform(ViewActions.replaceText("password123"), ViewActions.closeSoftKeyboard())

            // Click on login button
            val loginButton = onView(withId(R.id.botonIngresar))
            loginButton.perform(click())

            // Wait a bit
            Thread.sleep(5000)
        }
    }
    @Test
    fun test1SuccessfulBooking() {
        Thread.sleep(5000)

        onView(withId(R.id.recyclerViewHorizontalRestaurantesDestacados)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RestaurantesViewHolder>(
                0,
                click()
            )
        )

        Thread.sleep(1000)
        onView(withId(R.id.fabReservar)).perform(click())

        Thread.sleep(1000)
        onView(withId(R.id.botonAgregarPersona)).perform(click())

        Thread.sleep(1000)

        setDate(R.id.textInputFechaReserva, 2024, 11, 15);

        Thread.sleep(1000)

        // Perform a click on the first enabled chip in the ChipGroup
        onView(withId(R.id.chipGroup))
            .check(matches(isDisplayed()))
            .perform(object : ViewAction {
                override fun getConstraints(): Matcher<View> {
                    return isAssignableFrom(ChipGroup::class.java)
                }

                override fun getDescription(): String {
                    return "Click on an enabled chip"
                }

                override fun perform(uiController: UiController, view: View) {
                    val chipGroup = view as ChipGroup
                    for (i in 0 until chipGroup.childCount) {
                        val child = chipGroup.getChildAt(i)
                        if (child is Chip && child.isEnabled) {
                            child.performClick()
                            break
                        }
                    }
                }
            })

        // Click on the confirm button
        onView(withId(R.id.botonCrearReserva)).perform(click())

        // Wait a bit
        Thread.sleep(5000)

        // Click on confirm button
        onView(withId(R.id.botonConfirmarReserva)).perform(click())
    }

    fun setDate(datePickerLaunchViewId: Int, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        onView(withId(datePickerLaunchViewId)).perform(click())
        Thread.sleep(2000) // Add a delay to ensure the DatePicker dialog is displayed
        onView(withText(R.string.boton_seleccionar)).perform(click())
    }
}
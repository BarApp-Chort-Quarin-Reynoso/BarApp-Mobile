package com.barapp.ui.loginSignUp
import android.Manifest
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
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.barapp.BuildConfig
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
class PantallaMisReservasTest {

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

        val myReservationsNavigation = onView(withId(R.id.pantallaMisReservas))
        myReservationsNavigation.perform(click())
    }

    @Test
    fun test1SeeMyReservations() {
        // Wait a bit
        Thread.sleep(2000)

        val myReservationsToolbar = onView(
            Matchers.allOf(
                withId(R.id.toolbar),
                isDisplayed()
            )
        )
        myReservationsToolbar.check(matches(isDisplayed()))
    }

    @Test
    fun test2SeeMyPendingReservationsIfExists() {
        // Wait a bit
        Thread.sleep(2000)

        val hasPendingReservations = try {
            val labelPendingReservations = onView(
                Matchers.allOf(
                    isAssignableFrom(TextView::class.java),
                    withId(R.id.labelPendientes),
                    isDisplayed()
                )
            )
            labelPendingReservations.check(matches(isDisplayed()))
            true
        } catch (e: NoMatchingViewException) {
            false
        }

        if (!hasPendingReservations) {
            return
        }

        onView(withId(R.id.recycler_view_reservas_pendientes)).check(matches(isDisplayed()))
    }

    @Test
    fun test3SeeMyPastReservationsIfExists() {
        // Wait a bit
        Thread.sleep(2000)

        val hasPastReservations = try {
            val labelPastReservations = onView(
                Matchers.allOf(
                    isAssignableFrom(TextView::class.java),
                    withId(R.id.labelPasadas),
                    isDisplayed()
                )
            )
            labelPastReservations.check(matches(isDisplayed()))
            true
        } catch (e: NoMatchingViewException) {
            false
        }

        if (!hasPastReservations) {
            return
        }

        onView(withId(R.id.recycler_view_reservas_pasadas)).check(matches(isDisplayed()))
    }
}
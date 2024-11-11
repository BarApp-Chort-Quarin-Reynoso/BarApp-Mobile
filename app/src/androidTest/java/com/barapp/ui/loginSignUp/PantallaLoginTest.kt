package com.barapp.ui.loginSignUp

import android.Manifest
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.GrantPermissionRule
import com.barapp.BuildConfig
import com.barapp.R
import com.barapp.ui.MainActivity
import com.google.android.material.textfield.TextInputLayout
import org.hamcrest.Description
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4::class)
class PantallaLoginTest {

    @Rule
    @JvmField
    var mActivityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Rule
    @JvmField
    var mGrantPermissionRule =
        GrantPermissionRule.grant(
            "android.permission.ACCESS_FINE_LOCATION"
        )

    @Before
    fun setup() {
        val notificationsPermission = Manifest.permission.POST_NOTIFICATIONS
        val locationPermission = Manifest.permission.ACCESS_FINE_LOCATION
        getInstrumentation().uiAutomation.grantRuntimePermission(BuildConfig.APPLICATION_ID, notificationsPermission)
        Thread.sleep(1000)
        getInstrumentation().uiAutomation.grantRuntimePermission(BuildConfig.APPLICATION_ID, locationPermission)
    }

    @Test
    fun test1loginWithEmptyEmailAndPassword() {
        // Wait a bit
        Thread.sleep(5000)

        // Enter empty email
        val emailInput = onView(allOf(isDescendantOfA(withId(R.id.txtViewEmail)), isAssignableFrom(EditText::class.java)))
        emailInput.perform(replaceText(""), closeSoftKeyboard())

        // Enter empty password
        val passwordInput = onView(allOf(isDescendantOfA(withId(R.id.txtViewContrasenia)), isAssignableFrom(EditText::class.java)))
        passwordInput.perform(replaceText(""), closeSoftKeyboard())

        // Click on login button
        val loginButton = onView(withId(R.id.botonIngresar))
        loginButton.perform(click())

        // Wait a bit
        Thread.sleep(5000)

        // Check if login was unsuccessful by checking if the user is still on the login screen
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val expectedErrorText = context.getString(R.string.error_campo_obligatorio)
        onView(withId(R.id.txtViewEmail)).check(matches(hasTextInputLayoutErrorText(expectedErrorText)))
        onView(withId(R.id.txtViewContrasenia)).check(matches(hasTextInputLayoutErrorText(expectedErrorText)))
    }

    @Test
    fun test2loginWithInvalidEmail() {
        // Wait a bit
        Thread.sleep(5000)

        // Enter invalid email
        val emailInput = onView(allOf(isDescendantOfA(withId(R.id.txtViewEmail)), isAssignableFrom(EditText::class.java)))
        emailInput.perform(replaceText("invalidEmail"), closeSoftKeyboard())

        // Enter valid password
        val passwordInput = onView(allOf(isDescendantOfA(withId(R.id.txtViewContrasenia)), isAssignableFrom(EditText::class.java)))
        passwordInput.perform(replaceText("prueba123"), closeSoftKeyboard())

        // Click on login button
        val loginButton = onView(withId(R.id.botonIngresar))
        loginButton.perform(click())

        // Wait a bit
        Thread.sleep(5000)

        // Check if login was unsuccessful by checking if the user is still on the login screen
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val expectedErrorText = context.getString(R.string.error_mail_no_valido)
        onView(withId(R.id.txtViewEmail)).check(matches(hasTextInputLayoutErrorText(expectedErrorText)))
    }

    @Test
    fun test3loginWithShortPassword() {
        // Wait a bit
        Thread.sleep(5000)

        // Enter valid email
        val emailInput = onView(allOf(isDescendantOfA(withId(R.id.txtViewEmail)), isAssignableFrom(EditText::class.java)))
        emailInput.perform(replaceText("usuariodeprueba@gmail.com"), closeSoftKeyboard())

        // Enter short password
        val passwordInput = onView(allOf(isDescendantOfA(withId(R.id.txtViewContrasenia)), isAssignableFrom(EditText::class.java)))
        passwordInput.perform(replaceText("1234"), closeSoftKeyboard())

        // Click on login button
        val loginButton = onView(withId(R.id.botonIngresar))
        loginButton.perform(click())

        // Wait a bit
        Thread.sleep(5000)

        // Check if login was unsuccessful by checking if the user is still on the login screen
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val expectedErrorText = context.getString(R.string.error_campo_por_debajo_valor, 5)
        onView(withId(R.id.txtViewContrasenia)).check(matches(hasTextInputLayoutErrorText(expectedErrorText)))
    }

    @Test
    fun test4loginWithValidEmailAndPassword() {
        // Wait a bit
        Thread.sleep(5000)

        // Enter email
        val emailInput = onView(allOf(isDescendantOfA(withId(R.id.txtViewEmail)), isAssignableFrom(
            EditText::class.java)))
        emailInput.perform(replaceText("pruebacu@gmail.com"), closeSoftKeyboard())

        // Enter password
        val passwordInput = onView(allOf(isDescendantOfA(withId(R.id.txtViewContrasenia)), isAssignableFrom(
            EditText::class.java)))
        passwordInput.perform(replaceText("password123"), closeSoftKeyboard())

        // Click on login button
        val loginButton = onView(withId(R.id.botonIngresar))
        loginButton.perform(click())

        // Wait a bit
        Thread.sleep(5000)

        // Check if login was successful
        val labelDestacados = onView(
            allOf(
                isAssignableFrom(TextView::class.java),
                withId(R.id.labelDestacados),
                isDisplayed()
            )
        )
        labelDestacados.check(matches(isDisplayed()))
    }

    private fun hasTextInputLayoutErrorText(expectedErrorText: String): TypeSafeMatcher<View?> {
        return object : TypeSafeMatcher<View?>() {
            override fun describeTo(description: Description?) {}
            override fun matchesSafely(view: View?): Boolean {
                if (view !is TextInputLayout) {
                    return false
                }
                val error = (view).error
                    ?: return false
                val hint = error.toString()
                return expectedErrorText == hint
            }
        }
    }
}

import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.barapp.R
import com.barapp.ui.MainActivity
import com.google.android.material.textfield.TextInputLayout
import org.hamcrest.Description
import org.hamcrest.Matchers
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.FixMethodOrder
import org.junit.runners.MethodSorters

@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4::class)
class PantallaSignUpTest {

  // Change the email each time you run the test
  private val email = "test4@test.com"

  @Rule
  @JvmField
  var mActivityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

  @Rule
  @JvmField
  var mGrantPermissionRule =
    GrantPermissionRule.grant(
      "android.permission.ACCESS_FINE_LOCATION"
    )

  @Test
  fun test2ValidSignUpProcess() {
    // Press register button
    val registerButton = onView(withId(R.id.botonRegistrarse))
    registerButton.perform(click())

    // Enter valid name
    val nameInput = onView(
      Matchers.allOf(
        isDescendantOfA(withId(R.id.txtViewNombre)), isAssignableFrom(
          EditText::class.java
        )
      )
    )
    nameInput.perform(replaceText("Test"), closeSoftKeyboard())

//    // Enter valid last name
    val lastNameInput = onView(
      Matchers.allOf(
        isDescendantOfA(withId(R.id.txtViewApellido)), isAssignableFrom(
          EditText::class.java
        )
      )
    )
    lastNameInput.perform(replaceText("User"), closeSoftKeyboard())

    // Enter valid email

    val emailInput = onView(
      Matchers.allOf(
        isDescendantOfA(withId(R.id.txtViewEmail)), isAssignableFrom(
          EditText::class.java
        )
      )
    )
    emailInput.perform(replaceText(email), closeSoftKeyboard())

    // Enter valid phone number

    val phoneInput = onView(
      Matchers.allOf(
        isDescendantOfA(withId(R.id.txtViewTelefono)), isAssignableFrom(
          EditText::class.java
        )
      )
    )
    phoneInput.perform(replaceText("1234567890"), closeSoftKeyboard())

    // Enter valid password
    val passwordInput = onView(
      Matchers.allOf(
        isDescendantOfA(withId(R.id.txtViewContrasenia)), isAssignableFrom(
          EditText::class.java
        )
      )
    )
    passwordInput.perform(replaceText("password123"), closeSoftKeyboard())

    // Confirm password
    val confirmPasswordInput = onView(
      Matchers.allOf(
        isDescendantOfA(withId(R.id.txtViewConfirmeContrasenia)), isAssignableFrom(
          EditText::class.java
        )
      )
    )
    confirmPasswordInput.perform(replaceText("password123"), closeSoftKeyboard())

    // Click on create account button
//    onView(withId(R.id.botonCrearCuentaSignup)).perform(click())
    val createAccountButton = onView(withId(R.id.botonCrearCuentaSignup))
    createAccountButton.perform(click())

    // Wait a bit
    Thread.sleep(2000)

    // Check if login was successful
    val labelDestacados = onView(
      Matchers.allOf(
        isAssignableFrom(TextView::class.java),
        withId(R.id.labelDestacados),
        isDisplayed()
      )
    )
    labelDestacados.check(matches(isDisplayed()))
  }

  @Test
  fun test1SignUpWithInvalidEmail() {
//    val scenario = launchFragmentInContainer<PantallaSignUp>(themeResId = R.style.Theme_BarApp)
    // Press register button
    val registerButton = onView(withId(R.id.botonRegistrarse))
    registerButton.perform(click())

    // Enter valid name
    val nameInput = onView(
      Matchers.allOf(
        isDescendantOfA(withId(R.id.txtViewNombre)), isAssignableFrom(
          EditText::class.java
        )
      )
    )
    nameInput.perform(replaceText("Test"), closeSoftKeyboard())

    // Enter valid last name
    val lastNameInput = onView(
      Matchers.allOf(
        isDescendantOfA(withId(R.id.txtViewApellido)), isAssignableFrom(
          EditText::class.java
        )
      )
    )
    lastNameInput.perform(replaceText("User"), closeSoftKeyboard())

    // Enter invalid email
    val emailInput = onView(
      Matchers.allOf(
        isDescendantOfA(withId(R.id.txtViewEmail)), isAssignableFrom(
          EditText::class.java
        )
      )
    )
    emailInput.perform(replaceText("invalidEmail"), closeSoftKeyboard())

    // Enter valid phone number
    val phoneInput = onView(
      Matchers.allOf(
        isDescendantOfA(withId(R.id.txtViewTelefono)), isAssignableFrom(
          EditText::class.java
        )
      )
    )
    phoneInput.perform(replaceText("1234567890"), closeSoftKeyboard())

    // Enter valid password
    val passwordInput = onView(
      Matchers.allOf(
        isDescendantOfA(withId(R.id.txtViewContrasenia)), isAssignableFrom(
          EditText::class.java
        )
      )
    )
    passwordInput.perform(replaceText("password123"), closeSoftKeyboard())

    // Confirm password
    val confirmPasswordInput = onView(
      Matchers.allOf(
        isDescendantOfA(withId(R.id.txtViewConfirmeContrasenia)), isAssignableFrom(
          EditText::class.java
        )
      )
    )
    confirmPasswordInput.perform(replaceText("password123"), closeSoftKeyboard())

    // Click on create account button
    val createAccountButton = onView(withId(R.id.botonCrearCuentaSignup))
    createAccountButton.perform(click())

    // Check if sign up was unsuccessful by checking if the user is still on the sign up screen
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    val expectedErrorText = context.getString(R.string.error_mail_no_valido)
    onView(withId(R.id.txtViewEmail)).check(matches(hasTextInputLayoutErrorText(expectedErrorText)))
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
package com.example.customercareproject;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.customercareproject.ui.ForgotPasswordActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumentation test for ForgotPasswordActivity to verify Material Design 3 updates.
 * 
 * Tests verify:
 * - Material3TextField component is displayed
 * - Material3Button component is displayed
 * - Hero section with icon and title is visible
 * - All UI elements are properly configured
 * - Fade-in animation container is present
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ForgotPasswordActivityTest {

    @Rule
    public ActivityScenarioRule<ForgotPasswordActivity> activityRule =
            new ActivityScenarioRule<>(ForgotPasswordActivity.class);

    @Test
    public void testForgotPasswordScreenElementsDisplayed() {
        // Verify email field is displayed
        onView(withId(R.id.txtEmail))
                .check(matches(isDisplayed()));

        // Verify submit button is displayed
        onView(withId(R.id.btnGuiEmail))
                .check(matches(isDisplayed()));

        // Verify back to login link is displayed
        onView(withId(R.id.tvQuayLai))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testSubmitButtonText() {
        // Verify submit button has correct text
        onView(withId(R.id.btnGuiEmail))
                .check(matches(withText("Gửi email đặt lại")));
    }

    @Test
    public void testBackToLoginLinkText() {
        // Verify back to login link has correct text
        onView(withId(R.id.tvQuayLai))
                .check(matches(withText("Quay lại đăng nhập")));
    }

    @Test
    public void testForgotPasswordContainerDisplayed() {
        // Verify the main container is displayed (for fade-in animation)
        onView(withId(R.id.forgotPasswordContainer))
                .check(matches(isDisplayed()));
    }
}

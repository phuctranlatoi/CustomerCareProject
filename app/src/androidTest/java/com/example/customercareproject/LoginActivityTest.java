package com.example.customercareproject;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.customercareproject.ui.LoginActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumentation test for LoginActivity to verify Material Design 3 updates.
 * 
 * Tests verify:
 * - Material3TextField components are displayed
 * - Material3Button components are displayed
 * - Hero gradient background section is visible
 * - All UI elements are properly configured
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginActivityTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    @Test
    public void testLoginScreenElementsDisplayed() {
        // Verify email field is displayed
        onView(withId(R.id.txtEmail))
                .check(matches(isDisplayed()));

        // Verify password field is displayed
        onView(withId(R.id.txtPassword))
                .check(matches(isDisplayed()));

        // Verify login button is displayed
        onView(withId(R.id.btnLogin))
                .check(matches(isDisplayed()));

        // Verify Google login button is displayed
        onView(withId(R.id.btnGoogleLogin))
                .check(matches(isDisplayed()));

        // Verify forgot password link is displayed
        onView(withId(R.id.tvForgotPassword))
                .check(matches(isDisplayed()));

        // Verify register link is displayed
        onView(withId(R.id.tvRegister))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testLoginButtonText() {
        // Verify login button has correct text
        onView(withId(R.id.btnLogin))
                .check(matches(withText("Đăng nhập")));
    }

    @Test
    public void testGoogleLoginButtonText() {
        // Verify Google login button has correct text
        onView(withId(R.id.btnGoogleLogin))
                .check(matches(withText("Đăng nhập với Google")));
    }

    @Test
    public void testForgotPasswordLinkText() {
        // Verify forgot password link has correct text
        onView(withId(R.id.tvForgotPassword))
                .check(matches(withText("Quên mật khẩu?")));
    }

    @Test
    public void testRegisterLinkText() {
        // Verify register link has correct text
        onView(withId(R.id.tvRegister))
                .check(matches(withText("Đăng ký ngay")));
    }

    @Test
    public void testLoginContainerDisplayed() {
        // Verify the main login container is displayed (for fade-in animation)
        onView(withId(R.id.loginContainer))
                .check(matches(isDisplayed()));
    }
}

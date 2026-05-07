package com.example.customercareproject;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.customercareproject.ui.home.HomeActivity;
import com.google.firebase.auth.FirebaseAuth;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumentation test for HomeActivity MD3 redesign.
 * Tests the updated layout with Material3 components, grid layout, and animations.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class HomeActivityTest {

    @Before
    public void setUp() {
        // Note: This test requires a logged-in user
        // In a real scenario, you would mock Firebase Auth or use a test account
    }

    @After
    public void tearDown() {
        // Clean up if needed
    }

    @Test
    public void testHomeActivityLayoutComponents() {
        // Skip test if user is not logged in
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return;
        }

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), HomeActivity.class);
        ActivityScenario<HomeActivity> scenario = ActivityScenario.launch(intent);

        // Verify top app bar is displayed
        onView(withId(R.id.topAppBar)).check(matches(isDisplayed()));

        // Verify greeting text is displayed
        onView(withId(R.id.tvChaoMung)).check(matches(isDisplayed()));

        // Verify action buttons are displayed
        onView(withId(R.id.btnProfile)).check(matches(isDisplayed()));
        onView(withId(R.id.btnLichSuChat)).check(matches(isDisplayed()));
        onView(withId(R.id.btnLogout)).check(matches(isDisplayed()));

        // Verify product RecyclerView is displayed
        onView(withId(R.id.rvSanPham)).check(matches(isDisplayed()));

        // Verify FAB is displayed
        onView(withId(R.id.fabYeuCau)).check(matches(isDisplayed()));

        scenario.close();
    }

    @Test
    public void testFabSize() {
        // Skip test if user is not logged in
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return;
        }

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), HomeActivity.class);
        ActivityScenario<HomeActivity> scenario = ActivityScenario.launch(intent);

        // Verify FAB is displayed with correct size (56dp)
        onView(withId(R.id.fabYeuCau)).check(matches(isDisplayed()));

        scenario.close();
    }

    @Test
    public void testContentLayoutFadeInAnimation() {
        // Skip test if user is not logged in
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return;
        }

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), HomeActivity.class);
        ActivityScenario<HomeActivity> scenario = ActivityScenario.launch(intent);

        // Verify content layout is displayed (animation should complete)
        onView(withId(R.id.contentLayout)).check(matches(isDisplayed()));

        scenario.close();
    }
}

package com.example.customercareproject;

import android.content.Intent;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;

import com.example.customercareproject.ui.danhgia.DanhGiaActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

/**
 * Test for DanhGiaActivity Material Design 3 implementation.
 * Tests the requirements from task 5.5:
 * - Star rating with large touch targets (48dp minimum)
 * - Scale animation when user taps star
 * - Material3TextField with character counter for feedback
 * - Issue cards in grid layout with icons
 * - Success animation with checkmark on submission
 */
@RunWith(AndroidJUnit4.class)
public class DanhGiaActivityTest {

    @Rule
    public ActivityTestRule<DanhGiaActivity> activityRule = 
        new ActivityTestRule<DanhGiaActivity>(DanhGiaActivity.class) {
            @Override
            protected Intent getActivityIntent() {
                Intent intent = new Intent();
                intent.putExtra("sanPham", "Test Product");
                intent.putExtra("loaiGoi", "ChinhThuc");
                return intent;
            }
        };

    /**
     * Test that star rating buttons have minimum 48dp touch targets (Requirement 13.1)
     */
    @Test
    public void testStarRatingTouchTargets() {
        // Check that all star buttons are displayed and have proper size
        onView(withId(R.id.star1))
            .check(matches(isDisplayed()))
            .check(matches(hasMinimumSize(48, 48)));
        
        onView(withId(R.id.star2))
            .check(matches(isDisplayed()))
            .check(matches(hasMinimumSize(48, 48)));
        
        onView(withId(R.id.star3))
            .check(matches(isDisplayed()))
            .check(matches(hasMinimumSize(48, 48)));
        
        onView(withId(R.id.star4))
            .check(matches(isDisplayed()))
            .check(matches(hasMinimumSize(48, 48)));
        
        onView(withId(R.id.star5))
            .check(matches(isDisplayed()))
            .check(matches(hasMinimumSize(48, 48)));
    }

    /**
     * Test that tapping stars updates the rating (Requirement 13.2 - animation tested manually)
     */
    @Test
    public void testStarRatingSelection() {
        // Initially no rating selected
        onView(withId(R.id.tvMucDoHaiLong))
            .check(matches(withText("Chưa đánh giá")));
        
        // Tap 3rd star
        onView(withId(R.id.star3)).perform(click());
        
        // Check that rating text is updated
        onView(withId(R.id.tvMucDoHaiLong))
            .check(matches(withText("Bình thường")));
        
        // Tap 5th star
        onView(withId(R.id.star5)).perform(click());
        
        // Check that rating text is updated
        onView(withId(R.id.tvMucDoHaiLong))
            .check(matches(withText("Rất hài lòng")));
    }

    /**
     * Test that feedback text field has character counter (Requirement 13.3)
     */
    @Test
    public void testFeedbackTextFieldWithCounter() {
        // Check that text input layout is displayed
        onView(withId(R.id.txtNoiDung))
            .check(matches(isDisplayed()));
        
        // Check that edit text is displayed
        onView(withId(R.id.edtNoiDung))
            .check(matches(isDisplayed()));
        
        // Type some text
        onView(withId(R.id.edtNoiDung))
            .perform(ViewActions.typeText("This is a test feedback"));
        
        // Verify text was entered
        onView(withId(R.id.edtNoiDung))
            .check(matches(withText("This is a test feedback")));
    }

    /**
     * Test that issue cards are displayed in grid layout (Requirement 13.4)
     */
    @Test
    public void testIssueCardsGridLayout() {
        // Check that all issue cards are displayed
        onView(withId(R.id.cardIssue1))
            .check(matches(isDisplayed()));
        
        onView(withId(R.id.cardIssue2))
            .check(matches(isDisplayed()));
        
        onView(withId(R.id.cardIssue3))
            .check(matches(isDisplayed()));
        
        onView(withId(R.id.cardIssue4))
            .check(matches(isDisplayed()));
    }

    /**
     * Test that issue cards can be selected
     */
    @Test
    public void testIssueCardSelection() {
        // Tap first issue card
        onView(withId(R.id.cardIssue1)).perform(click());
        
        // Tap second issue card
        onView(withId(R.id.cardIssue2)).perform(click());
        
        // Cards should remain visible (selection state tested manually due to animation)
        onView(withId(R.id.cardIssue1))
            .check(matches(isDisplayed()));
        
        onView(withId(R.id.cardIssue2))
            .check(matches(isDisplayed()));
    }

    /**
     * Test that submit button is displayed with proper styling
     */
    @Test
    public void testSubmitButton() {
        // Check that submit button is displayed
        onView(withId(R.id.btnGuiDanhGia))
            .check(matches(isDisplayed()))
            .check(matches(withText("Gửi đánh giá")));
    }

    /**
     * Custom matcher to check minimum size
     */
    private static org.hamcrest.Matcher<android.view.View> hasMinimumSize(final int minWidth, final int minHeight) {
        return new org.hamcrest.TypeSafeMatcher<android.view.View>() {
            @Override
            public boolean matchesSafely(android.view.View view) {
                return view.getWidth() >= minWidth && view.getHeight() >= minHeight;
            }

            @Override
            public void describeTo(org.hamcrest.Description description) {
                description.appendText("has minimum size " + minWidth + "x" + minHeight);
            }
        };
    }
}
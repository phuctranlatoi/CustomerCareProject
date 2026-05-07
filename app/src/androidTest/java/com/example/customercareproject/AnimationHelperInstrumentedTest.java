package com.example.customercareproject;

import android.view.View;
import android.widget.FrameLayout;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.customercareproject.utils.AnimationHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented tests for AnimationHelper with actual Android views.
 * These tests verify that animations can be applied to real views without crashing.
 */
@RunWith(AndroidJUnit4.class)
public class AnimationHelperInstrumentedTest {
    
    private View testView;
    
    @Before
    public void setUp() {
        // Create a test view in the instrumentation context
        testView = new FrameLayout(InstrumentationRegistry.getInstrumentation().getTargetContext());
        testView.setLayoutParams(new FrameLayout.LayoutParams(100, 100));
    }
    
    @Test
    public void testFadeInAnimation() {
        // Test that fadeIn can be applied without crashing
        AnimationHelper.fadeIn(testView);
        
        // Verify view is visible
        assertEquals("View should be visible after fadeIn", View.VISIBLE, testView.getVisibility());
    }
    
    @Test
    public void testFadeOutAnimation() {
        // Ensure view is visible first
        testView.setVisibility(View.VISIBLE);
        testView.setAlpha(1f);
        
        // Test that fadeOut can be applied without crashing
        AnimationHelper.fadeOut(testView);
        
        // Note: View visibility will be GONE after animation completes
        // We just verify the animation starts without crashing
        assertNotNull("View should not be null after fadeOut", testView);
    }
    
    @Test
    public void testScalePressAnimation() {
        // Test that scalePress can be applied without crashing
        AnimationHelper.scalePress(testView);
        
        // Verify view is not null
        assertNotNull("View should not be null after scalePress", testView);
    }
    
    @Test
    public void testScaleReleaseAnimation() {
        // Test that scaleRelease can be applied without crashing
        AnimationHelper.scaleRelease(testView);
        
        // Verify view is not null
        assertNotNull("View should not be null after scaleRelease", testView);
    }
    
    @Test
    public void testSlideUpAnimation() {
        // Test that slideUp can be applied without crashing
        AnimationHelper.slideUp(testView);
        
        // Verify view is visible
        assertEquals("View should be visible after slideUp", View.VISIBLE, testView.getVisibility());
    }
    
    @Test
    public void testSlideDownAnimation() {
        // Ensure view is visible first
        testView.setVisibility(View.VISIBLE);
        
        // Test that slideDown can be applied without crashing
        AnimationHelper.slideDown(testView);
        
        // Verify view is not null
        assertNotNull("View should not be null after slideDown", testView);
    }
    
    @Test
    public void testAnimateListItem() {
        // Test that animateListItem can be applied without crashing
        AnimationHelper.animateListItem(testView, 0);
        AnimationHelper.animateListItem(testView, 5);
        
        // Verify view is not null
        assertNotNull("View should not be null after animateListItem", testView);
    }
    
    @Test
    public void testShakeAnimation() {
        // Test that shake can be applied without crashing
        AnimationHelper.shake(testView);
        
        // Verify view is not null
        assertNotNull("View should not be null after shake", testView);
    }
    
    @Test
    public void testBounceAnimation() {
        // Test that bounce can be applied without crashing
        AnimationHelper.bounce(testView);
        
        // Verify view is not null
        assertNotNull("View should not be null after bounce", testView);
    }
    
    @Test
    public void testRotateAnimation() {
        // Test that rotate can be applied without crashing
        AnimationHelper.rotate(testView, 90f, AnimationHelper.DURATION_STANDARD);
        
        // Verify view is not null
        assertNotNull("View should not be null after rotate", testView);
    }
    
    @Test
    public void testPulseAnimation() {
        // Test that pulse can be applied without crashing
        AnimationHelper.pulse(testView, 3);
        
        // Verify view is not null
        assertNotNull("View should not be null after pulse", testView);
    }
    
    @Test
    public void testCancelAnimations() {
        // Start an animation
        AnimationHelper.fadeIn(testView);
        
        // Cancel it
        AnimationHelper.cancelAnimations(testView);
        
        // Verify view is not null
        assertNotNull("View should not be null after cancelAnimations", testView);
    }
    
    @Test
    public void testResetView() {
        // Modify view state
        testView.setAlpha(0.5f);
        testView.setScaleX(1.5f);
        testView.setScaleY(1.5f);
        testView.setTranslationX(100f);
        testView.setTranslationY(100f);
        testView.setRotation(45f);
        
        // Reset view
        AnimationHelper.resetView(testView);
        
        // Verify view is reset to default state
        assertEquals("Alpha should be 1", 1f, testView.getAlpha(), 0.01f);
        assertEquals("ScaleX should be 1", 1f, testView.getScaleX(), 0.01f);
        assertEquals("ScaleY should be 1", 1f, testView.getScaleY(), 0.01f);
        assertEquals("TranslationX should be 0", 0f, testView.getTranslationX(), 0.01f);
        assertEquals("TranslationY should be 0", 0f, testView.getTranslationY(), 0.01f);
        assertEquals("Rotation should be 0", 0f, testView.getRotation(), 0.01f);
    }
    
    @Test
    public void testAnimationDurationConstants() {
        // Verify constants are accessible and have correct values
        assertEquals(100, AnimationHelper.DURATION_FAST);
        assertEquals(200, AnimationHelper.DURATION_STANDARD);
        assertEquals(300, AnimationHelper.DURATION_MEDIUM);
        assertEquals(400, AnimationHelper.DURATION_SLOW);
    }
}

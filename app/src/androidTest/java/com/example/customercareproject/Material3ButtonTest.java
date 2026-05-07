package com.example.customercareproject;

import android.content.Context;
import android.view.MotionEvent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.customercareproject.ui.components.Material3Button;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test for Material3Button component.
 * Tests the custom button functionality including loading state, press animations, and disabled state.
 */
@RunWith(AndroidJUnit4.class)
public class Material3ButtonTest {
    
    private Context context;
    private Material3Button button;
    
    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        button = new Material3Button(context);
    }
    
    /**
     * Test that button is created with default state.
     */
    @Test
    public void testButtonCreation() {
        assertNotNull("Button should be created", button);
        assertTrue("Button should be enabled by default", button.isEnabled());
        assertFalse("Button should not be loading by default", button.isLoading());
        assertEquals("Button should have full opacity", 1.0f, button.getAlpha(), 0.01f);
    }
    
    /**
     * Test that button enters loading state correctly.
     */
    @Test
    public void testSetLoadingTrue() {
        // Set initial text
        button.setText("Submit");
        button.setEnabled(true);
        
        // Set loading to true
        button.setLoading(true);
        
        // Verify loading state
        assertTrue("Button should be in loading state", button.isLoading());
        assertFalse("Button should be disabled when loading", button.isEnabled());
        assertEquals("Button text should be empty when loading", "", button.getText().toString());
    }
    
    /**
     * Test that button exits loading state correctly.
     */
    @Test
    public void testSetLoadingFalse() {
        // Set initial state
        button.setText("Submit");
        button.setEnabled(true);
        
        // Enter loading state
        button.setLoading(true);
        
        // Exit loading state
        button.setLoading(false);
        
        // Verify state is restored
        assertFalse("Button should not be in loading state", button.isLoading());
        assertTrue("Button should be enabled after loading", button.isEnabled());
        assertEquals("Button text should be restored", "Submit", button.getText().toString());
    }
    
    /**
     * Test that loading state preserves original enabled state.
     */
    @Test
    public void testLoadingPreservesEnabledState() {
        // Disable button before loading
        button.setText("Submit");
        button.setEnabled(false);
        
        // Enter and exit loading state
        button.setLoading(true);
        button.setLoading(false);
        
        // Verify disabled state is preserved
        assertFalse("Button should remain disabled after loading", button.isEnabled());
    }
    
    /**
     * Test that disabled state applies correct opacity.
     */
    @Test
    public void testDisabledStateOpacity() {
        // Enable button
        button.setEnabled(true);
        assertEquals("Enabled button should have full opacity", 1.0f, button.getAlpha(), 0.01f);
        
        // Disable button
        button.setEnabled(false);
        assertEquals("Disabled button should have reduced opacity", 0.38f, button.getAlpha(), 0.01f);
    }
    
    /**
     * Test that button cannot be clicked when loading.
     */
    @Test
    public void testClickBlockedWhenLoading() {
        final boolean[] clicked = {false};
        
        button.setOnClickListener(v -> clicked[0] = true);
        
        // Set loading state
        button.setLoading(true);
        
        // Try to click
        button.performClick();
        
        // Verify click was blocked
        assertFalse("Button should not respond to clicks when loading", clicked[0]);
    }
    
    /**
     * Test that button can be clicked when not loading.
     */
    @Test
    public void testClickAllowedWhenNotLoading() {
        final boolean[] clicked = {false};
        
        button.setOnClickListener(v -> clicked[0] = true);
        button.setEnabled(true);
        
        // Click button
        button.performClick();
        
        // Verify click was processed
        assertTrue("Button should respond to clicks when not loading", clicked[0]);
    }
    
    /**
     * Test that ripple effect is enabled.
     */
    @Test
    public void testRippleEffectEnabled() {
        assertTrue("Button should be clickable for ripple effect", button.isClickable());
        assertTrue("Button should be focusable for ripple effect", button.isFocusable());
        assertNotNull("Button should have ripple color", button.getRippleColor());
    }
    
    /**
     * Test that button scale is reset when disabled during press.
     */
    @Test
    public void testScaleResetOnDisable() throws InterruptedException {
        button.setEnabled(true);
        
        // Simulate press
        MotionEvent downEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 0, 0, 0);
        button.dispatchTouchEvent(downEvent);
        downEvent.recycle();
        
        // Wait for animation to start
        Thread.sleep(50);
        
        // Disable button (should reset scale)
        button.setEnabled(false);
        
        // Wait for animation to complete
        Thread.sleep(150);
        
        // Verify scale is reset (approximately 1.0)
        // Note: Due to animation timing, we allow some tolerance
        assertTrue("Button scale should be close to 1.0 when disabled", 
                   Math.abs(button.getScaleX() - 1.0f) < 0.05f);
    }
    
    /**
     * Test that loading state can be toggled multiple times.
     */
    @Test
    public void testMultipleLoadingToggles() {
        button.setText("Submit");
        button.setEnabled(true);
        
        // Toggle loading multiple times
        for (int i = 0; i < 3; i++) {
            button.setLoading(true);
            assertTrue("Button should be loading", button.isLoading());
            
            button.setLoading(false);
            assertFalse("Button should not be loading", button.isLoading());
            assertEquals("Button text should be restored", "Submit", button.getText().toString());
        }
    }
    
    /**
     * Test that setting loading to same state is idempotent.
     */
    @Test
    public void testLoadingIdempotent() {
        button.setText("Submit");
        
        // Set loading to true twice
        button.setLoading(true);
        button.setLoading(true);
        
        assertTrue("Button should be loading", button.isLoading());
        
        // Set loading to false twice
        button.setLoading(false);
        button.setLoading(false);
        
        assertFalse("Button should not be loading", button.isLoading());
        assertEquals("Button text should be restored", "Submit", button.getText().toString());
    }
}

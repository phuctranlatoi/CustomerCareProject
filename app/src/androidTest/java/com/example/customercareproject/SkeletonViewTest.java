package com.example.customercareproject;

import android.content.Context;
import android.graphics.Color;
import android.view.View;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.customercareproject.ui.components.SkeletonView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumentation tests for SkeletonView component.
 * 
 * Tests verify:
 * - Shape configuration (rectangle, circle)
 * - Color customization
 * - Animation control (start, stop, state)
 * - Visibility behavior
 */
@RunWith(AndroidJUnit4.class)
public class SkeletonViewTest {
    
    private Context context;
    private SkeletonView skeletonView;
    
    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        skeletonView = new SkeletonView(context);
        
        // Set size for the view (required for animation)
        skeletonView.measure(
            View.MeasureSpec.makeMeasureSpec(200, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(200, View.MeasureSpec.EXACTLY)
        );
        skeletonView.layout(0, 0, 200, 200);
    }
    
    @Test
    public void testDefaultShape() {
        // Default shape should be RECTANGLE
        assertEquals(SkeletonView.Shape.RECTANGLE, skeletonView.getSkeletonShape());
    }
    
    @Test
    public void testSetShapeRectangle() {
        skeletonView.setSkeletonShape(SkeletonView.Shape.RECTANGLE);
        assertEquals(SkeletonView.Shape.RECTANGLE, skeletonView.getSkeletonShape());
    }
    
    @Test
    public void testSetShapeCircle() {
        skeletonView.setSkeletonShape(SkeletonView.Shape.CIRCLE);
        assertEquals(SkeletonView.Shape.CIRCLE, skeletonView.getSkeletonShape());
    }
    
    @Test
    public void testSetBaseColor() {
        int testColor = Color.BLUE;
        skeletonView.setBaseColor(testColor);
        assertEquals(testColor, skeletonView.getBaseColor());
    }
    
    @Test
    public void testSetShimmerColor() {
        int testColor = Color.WHITE;
        skeletonView.setShimmerColor(testColor);
        assertEquals(testColor, skeletonView.getShimmerColor());
    }
    
    @Test
    public void testSetShimmerDuration() {
        int testDuration = 2000;
        skeletonView.setShimmerDuration(testDuration);
        assertEquals(testDuration, skeletonView.getShimmerDuration());
    }
    
    @Test
    public void testStartShimmer() {
        // Initially shimmer should not be running
        assertFalse(skeletonView.isShimmerRunning());
        
        // Start shimmer
        skeletonView.startShimmer();
        
        // Shimmer should now be running
        assertTrue(skeletonView.isShimmerRunning());
    }
    
    @Test
    public void testStopShimmer() {
        // Start shimmer first
        skeletonView.startShimmer();
        assertTrue(skeletonView.isShimmerRunning());
        
        // Stop shimmer
        skeletonView.stopShimmer();
        
        // Shimmer should not be running
        assertFalse(skeletonView.isShimmerRunning());
    }
    
    @Test
    public void testShimmerStateAfterMultipleStarts() {
        // Start shimmer multiple times
        skeletonView.startShimmer();
        skeletonView.startShimmer();
        skeletonView.startShimmer();
        
        // Should still be running (not create multiple animators)
        assertTrue(skeletonView.isShimmerRunning());
        
        // Stop once should stop it
        skeletonView.stopShimmer();
        assertFalse(skeletonView.isShimmerRunning());
    }
    
    @Test
    public void testShapeEnumValues() {
        // Test Shape enum values
        assertEquals(0, SkeletonView.Shape.RECTANGLE.getValue());
        assertEquals(1, SkeletonView.Shape.CIRCLE.getValue());
    }
    
    @Test
    public void testShapeFromValue() {
        // Test Shape.fromValue()
        assertEquals(SkeletonView.Shape.RECTANGLE, SkeletonView.Shape.fromValue(0));
        assertEquals(SkeletonView.Shape.CIRCLE, SkeletonView.Shape.fromValue(1));
        
        // Invalid value should return default (RECTANGLE)
        assertEquals(SkeletonView.Shape.RECTANGLE, SkeletonView.Shape.fromValue(999));
    }
    
    @Test
    public void testViewVisibility() {
        // View should be visible by default
        assertEquals(View.VISIBLE, skeletonView.getVisibility());
        
        // Change visibility
        skeletonView.setVisibility(View.GONE);
        assertEquals(View.GONE, skeletonView.getVisibility());
        
        skeletonView.setVisibility(View.INVISIBLE);
        assertEquals(View.INVISIBLE, skeletonView.getVisibility());
    }
    
    @Test
    public void testShimmerDurationChange() {
        // Start shimmer with default duration
        skeletonView.startShimmer();
        assertTrue(skeletonView.isShimmerRunning());
        
        // Change duration (should restart animation)
        skeletonView.setShimmerDuration(3000);
        assertEquals(3000, skeletonView.getShimmerDuration());
        
        // Shimmer should still be running after duration change
        assertTrue(skeletonView.isShimmerRunning());
    }
    
    @Test
    public void testMultipleShapeChanges() {
        // Change shape multiple times
        skeletonView.setSkeletonShape(SkeletonView.Shape.CIRCLE);
        assertEquals(SkeletonView.Shape.CIRCLE, skeletonView.getSkeletonShape());
        
        skeletonView.setSkeletonShape(SkeletonView.Shape.RECTANGLE);
        assertEquals(SkeletonView.Shape.RECTANGLE, skeletonView.getSkeletonShape());
        
        skeletonView.setSkeletonShape(SkeletonView.Shape.CIRCLE);
        assertEquals(SkeletonView.Shape.CIRCLE, skeletonView.getSkeletonShape());
    }
    
    @Test
    public void testColorChanges() {
        // Change colors multiple times
        skeletonView.setBaseColor(Color.RED);
        assertEquals(Color.RED, skeletonView.getBaseColor());
        
        skeletonView.setBaseColor(Color.GREEN);
        assertEquals(Color.GREEN, skeletonView.getBaseColor());
        
        skeletonView.setShimmerColor(Color.YELLOW);
        assertEquals(Color.YELLOW, skeletonView.getShimmerColor());
        
        skeletonView.setShimmerColor(Color.CYAN);
        assertEquals(Color.CYAN, skeletonView.getShimmerColor());
    }
}

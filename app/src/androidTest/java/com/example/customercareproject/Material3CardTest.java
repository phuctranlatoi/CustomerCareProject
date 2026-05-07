package com.example.customercareproject;

import android.content.Context;
import android.view.View;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.customercareproject.ui.components.Material3Card;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test for Material3Card component.
 * 
 * Tests:
 * - Corner radius size configuration
 * - Clickable state with ripple effect
 * - Hover effect enable/disable
 * - State reset functionality
 * - Default values
 */
@RunWith(AndroidJUnit4.class)
public class Material3CardTest {
    
    private Context context;
    private Material3Card card;
    
    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        card = new Material3Card(context);
    }
    
    /**
     * Test default values when card is created.
     */
    @Test
    public void testDefaultValues() {
        // Default corner radius should be MEDIUM
        assertEquals(Material3Card.CornerRadiusSize.MEDIUM, card.getCornerRadiusSize());
        
        // Default clickable should be false
        assertFalse(card.isCardClickable());
        
        // Default hover effect should be enabled
        assertTrue(card.isHoverEffectEnabled());
        
        // Card should not be clickable by default
        assertFalse(card.isClickable());
    }
    
    /**
     * Test setting corner radius size.
     */
    @Test
    public void testSetCornerRadiusSize() {
        // Test SMALL
        card.setCornerRadiusSize(Material3Card.CornerRadiusSize.SMALL);
        assertEquals(Material3Card.CornerRadiusSize.SMALL, card.getCornerRadiusSize());
        
        // Test MEDIUM
        card.setCornerRadiusSize(Material3Card.CornerRadiusSize.MEDIUM);
        assertEquals(Material3Card.CornerRadiusSize.MEDIUM, card.getCornerRadiusSize());
        
        // Test LARGE
        card.setCornerRadiusSize(Material3Card.CornerRadiusSize.LARGE);
        assertEquals(Material3Card.CornerRadiusSize.LARGE, card.getCornerRadiusSize());
        
        // Test EXTRA_LARGE
        card.setCornerRadiusSize(Material3Card.CornerRadiusSize.EXTRA_LARGE);
        assertEquals(Material3Card.CornerRadiusSize.EXTRA_LARGE, card.getCornerRadiusSize());
    }
    
    /**
     * Test corner radius size enum values.
     */
    @Test
    public void testCornerRadiusSizeEnumValues() {
        assertEquals(0, Material3Card.CornerRadiusSize.SMALL.getValue());
        assertEquals(1, Material3Card.CornerRadiusSize.MEDIUM.getValue());
        assertEquals(2, Material3Card.CornerRadiusSize.LARGE.getValue());
        assertEquals(3, Material3Card.CornerRadiusSize.EXTRA_LARGE.getValue());
    }
    
    /**
     * Test corner radius size from value.
     */
    @Test
    public void testCornerRadiusSizeFromValue() {
        assertEquals(Material3Card.CornerRadiusSize.SMALL, 
            Material3Card.CornerRadiusSize.fromValue(0));
        assertEquals(Material3Card.CornerRadiusSize.MEDIUM, 
            Material3Card.CornerRadiusSize.fromValue(1));
        assertEquals(Material3Card.CornerRadiusSize.LARGE, 
            Material3Card.CornerRadiusSize.fromValue(2));
        assertEquals(Material3Card.CornerRadiusSize.EXTRA_LARGE, 
            Material3Card.CornerRadiusSize.fromValue(3));
        
        // Invalid value should return MEDIUM (default)
        assertEquals(Material3Card.CornerRadiusSize.MEDIUM, 
            Material3Card.CornerRadiusSize.fromValue(999));
    }
    
    /**
     * Test setting card clickable state.
     */
    @Test
    public void testSetCardClickable() {
        // Initially not clickable
        assertFalse(card.isCardClickable());
        assertFalse(card.isClickable());
        
        // Enable clickable
        card.setCardClickable(true);
        assertTrue(card.isCardClickable());
        assertFalse(card.isClickable()); // isClickable() is separate from cardClickable
        
        // Disable clickable
        card.setCardClickable(false);
        assertFalse(card.isCardClickable());
        assertFalse(card.isClickable());
    }
    
    /**
     * Test enabling/disabling hover effect.
     */
    @Test
    public void testSetEnableHoverEffect() {
        // Initially enabled
        assertTrue(card.isHoverEffectEnabled());
        
        // Disable hover effect
        card.setEnableHoverEffect(false);
        assertFalse(card.isHoverEffectEnabled());
        
        // Enable hover effect
        card.setEnableHoverEffect(true);
        assertTrue(card.isHoverEffectEnabled());
    }
    
    /**
     * Test reset state functionality.
     */
    @Test
    public void testResetState() {
        // Set some scale values
        card.setScaleX(0.98f);
        card.setScaleY(0.98f);
        
        // Reset state
        card.resetState();
        
        // Scale should be back to normal (1.0)
        assertEquals(1.0f, card.getScaleX(), 0.01f);
        assertEquals(1.0f, card.getScaleY(), 0.01f);
    }
    
    /**
     * Test that card has proper visibility.
     */
    @Test
    public void testCardVisibility() {
        // Card should be visible by default
        assertEquals(View.VISIBLE, card.getVisibility());
    }
    
    /**
     * Test multiple corner radius changes.
     */
    @Test
    public void testMultipleCornerRadiusChanges() {
        // Change corner radius multiple times
        card.setCornerRadiusSize(Material3Card.CornerRadiusSize.SMALL);
        assertEquals(Material3Card.CornerRadiusSize.SMALL, card.getCornerRadiusSize());
        
        card.setCornerRadiusSize(Material3Card.CornerRadiusSize.EXTRA_LARGE);
        assertEquals(Material3Card.CornerRadiusSize.EXTRA_LARGE, card.getCornerRadiusSize());
        
        card.setCornerRadiusSize(Material3Card.CornerRadiusSize.MEDIUM);
        assertEquals(Material3Card.CornerRadiusSize.MEDIUM, card.getCornerRadiusSize());
    }
    
    /**
     * Test that setting same value doesn't cause issues.
     */
    @Test
    public void testSetSameValueMultipleTimes() {
        // Set corner radius to MEDIUM multiple times
        card.setCornerRadiusSize(Material3Card.CornerRadiusSize.MEDIUM);
        card.setCornerRadiusSize(Material3Card.CornerRadiusSize.MEDIUM);
        card.setCornerRadiusSize(Material3Card.CornerRadiusSize.MEDIUM);
        
        assertEquals(Material3Card.CornerRadiusSize.MEDIUM, card.getCornerRadiusSize());
        
        // Set clickable to true multiple times
        card.setCardClickable(true);
        card.setCardClickable(true);
        card.setCardClickable(true);
        
        assertTrue(card.isCardClickable());
        
        // Set hover effect to false multiple times
        card.setEnableHoverEffect(false);
        card.setEnableHoverEffect(false);
        card.setEnableHoverEffect(false);
        
        assertFalse(card.isHoverEffectEnabled());
    }
    
    /**
     * Test card configuration combination.
     */
    @Test
    public void testCardConfigurationCombination() {
        // Configure card with all options
        card.setCornerRadiusSize(Material3Card.CornerRadiusSize.LARGE);
        card.setCardClickable(true);
        card.setEnableHoverEffect(true);
        
        // Verify all settings
        assertEquals(Material3Card.CornerRadiusSize.LARGE, card.getCornerRadiusSize());
        assertTrue(card.isCardClickable());
        assertTrue(card.isHoverEffectEnabled());
        
        // Change configuration
        card.setCornerRadiusSize(Material3Card.CornerRadiusSize.SMALL);
        card.setCardClickable(false);
        card.setEnableHoverEffect(false);
        
        // Verify new settings
        assertEquals(Material3Card.CornerRadiusSize.SMALL, card.getCornerRadiusSize());
        assertFalse(card.isCardClickable());
        assertFalse(card.isHoverEffectEnabled());
    }
}

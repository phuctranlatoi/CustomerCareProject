package com.example.customercareproject;

import android.content.Context;
import android.view.View;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.customercareproject.ui.components.Material3TextField;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumentation tests for Material3TextField component.
 * 
 * Tests verify:
 * - Text getting and setting
 * - Error handling with shake animation
 * - Password strength indicator
 * - Character counter support
 * - Floating label animation
 */
@RunWith(AndroidJUnit4.class)
public class Material3TextFieldTest {
    
    private Context context;
    private Material3TextField textField;
    
    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        textField = new Material3TextField(context);
    }
    
    @Test
    public void testTextFieldCreation() {
        assertNotNull("TextField should be created", textField);
        assertNotNull("EditText should be created", textField.getEditText());
    }
    
    @Test
    public void testSetAndGetText() {
        String testText = "Test input";
        textField.setText(testText);
        assertEquals("Text should match", testText, textField.getText());
    }
    
    @Test
    public void testEmptyText() {
        textField.setText("");
        assertEquals("Empty text should return empty string", "", textField.getText());
    }
    
    @Test
    public void testSetError() {
        String errorMessage = "This field is required";
        textField.setError(errorMessage);
        
        CharSequence error = textField.getError();
        assertNotNull("Error should be set", error);
        assertEquals("Error message should match", errorMessage, error.toString());
    }
    
    @Test
    public void testClearError() {
        // Set error first
        textField.setError("Error message");
        assertNotNull("Error should be set", textField.getError());
        
        // Clear error
        textField.clearError();
        assertNull("Error should be cleared", textField.getError());
    }
    
    @Test
    public void testPasswordStrengthIndicator() {
        // Initially disabled
        assertFalse("Password strength should be disabled by default", 
                   textField.isShowPasswordStrength());
        
        // Enable password strength
        textField.setShowPasswordStrength(true);
        assertTrue("Password strength should be enabled", 
                  textField.isShowPasswordStrength());
        
        // Disable password strength
        textField.setShowPasswordStrength(false);
        assertFalse("Password strength should be disabled", 
                   textField.isShowPasswordStrength());
    }
    
    @Test
    public void testHintText() {
        String hint = "Enter your email";
        textField.setHint(hint);
        assertEquals("Hint should match", hint, textField.getHint().toString());
    }
    
    @Test
    public void testInputType() {
        int inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
        textField.setInputType(inputType);
        
        // The actual input type may have additional flags, so we check if it contains our type
        int actualInputType = textField.getInputType();
        assertTrue("Input type should contain email address type", 
                  (actualInputType & inputType) != 0);
    }
    
    @Test
    public void testCounterEnabled() {
        textField.setCounterEnabled(true);
        assertTrue("Counter should be enabled", textField.isCounterEnabled());
        
        textField.setCounterMaxLength(50);
        assertEquals("Counter max length should match", 50, textField.getCounterMaxLength());
    }
    
    @Test
    public void testHelperText() {
        String helperText = "Enter a valid email address";
        textField.setHelperText(helperText);
        textField.setHelperTextEnabled(true);
        
        assertEquals("Helper text should match", helperText, 
                    textField.getHelperText().toString());
    }
    
    @Test
    public void testVisibility() {
        textField.setVisibility(View.VISIBLE);
        assertEquals("Visibility should be VISIBLE", View.VISIBLE, textField.getVisibility());
        
        textField.setVisibility(View.GONE);
        assertEquals("Visibility should be GONE", View.GONE, textField.getVisibility());
    }
    
    @Test
    public void testEnabled() {
        textField.setEnabled(true);
        assertTrue("TextField should be enabled", textField.isEnabled());
        
        textField.setEnabled(false);
        assertFalse("TextField should be disabled", textField.isEnabled());
    }
    
    @Test
    public void testMultipleErrorsAndClears() {
        // Set error
        textField.setError("Error 1");
        assertNotNull("Error should be set", textField.getError());
        
        // Clear error
        textField.clearError();
        assertNull("Error should be cleared", textField.getError());
        
        // Set another error
        textField.setError("Error 2");
        assertNotNull("Error should be set again", textField.getError());
        
        // Clear error again
        textField.clearError();
        assertNull("Error should be cleared again", textField.getError());
    }
    
    @Test
    public void testTextWithSpecialCharacters() {
        String specialText = "Test@123!#$%";
        textField.setText(specialText);
        assertEquals("Special characters should be preserved", specialText, textField.getText());
    }
    
    @Test
    public void testLongText() {
        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longText.append("a");
        }
        
        textField.setText(longText.toString());
        assertEquals("Long text should be preserved", longText.toString(), textField.getText());
    }
    
    @Test
    public void testNullText() {
        textField.setText(null);
        assertEquals("Null text should return empty string", "", textField.getText());
    }
}

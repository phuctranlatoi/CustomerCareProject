package com.example.customercareproject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * Property-based test for YeuCauHoTroActivity form validation logic.
 * 
 * **Validates: Requirements 7.2, 7.4, 17.3**
 * 
 * This test verifies the form validation logic used in YeuCauHoTroActivity
 * to ensure proper validation of user input fields.
 */
@RunWith(JUnit4.class)
public class YeuCauHoTroFormValidationTest {

    // Email validation pattern (same as used in YeuCauHoTroActivity)
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");

    // Phone validation pattern (Vietnamese phone numbers)
    private static final Pattern PHONE_PATTERN = 
        Pattern.compile("^(\\+84|84|0)[3-9]\\d{8}$");

    /**
     * Property: Valid names should be accepted
     * A valid name has at least 2 characters and contains letters
     */
    @Test
    public void testValidNameProperty() {
        // Test various valid names
        assertTrue("Two character name should be valid", isValidName("An"));
        assertTrue("Normal name should be valid", isValidName("Nguyen Van A"));
        assertTrue("Name with spaces should be valid", isValidName("Le Thi B"));
        assertTrue("Long name should be valid", isValidName("Nguyen Thi Minh Chau"));
        assertTrue("Name with accents should be valid", isValidName("Nguyễn Văn Á"));
    }

    /**
     * Property: Invalid names should be rejected
     * Invalid names are empty, too short, or contain only numbers/symbols
     */
    @Test
    public void testInvalidNameProperty() {
        // Test various invalid names
        assertFalse("Empty name should be invalid", isValidName(""));
        assertFalse("Single character should be invalid", isValidName("A"));
        assertFalse("Only spaces should be invalid", isValidName("   "));
        assertFalse("Only numbers should be invalid", isValidName("123"));
        assertFalse("Only symbols should be invalid", isValidName("@#$"));
    }

    /**
     * Property: Valid Vietnamese phone numbers should be accepted
     * Valid formats: 0xxxxxxxxx, +84xxxxxxxxx, 84xxxxxxxxx
     */
    @Test
    public void testValidPhoneProperty() {
        // Test various valid phone formats
        assertTrue("Standard mobile format should be valid", isValidPhone("0123456789"));
        assertTrue("International format with + should be valid", isValidPhone("+84123456789"));
        assertTrue("International format without + should be valid", isValidPhone("84123456789"));
        assertTrue("Different mobile prefix should be valid", isValidPhone("0987654321"));
        assertTrue("Another mobile prefix should be valid", isValidPhone("0345678901"));
    }

    /**
     * Property: Invalid phone numbers should be rejected
     * Invalid formats include wrong length, invalid prefixes, non-numeric characters
     */
    @Test
    public void testInvalidPhoneProperty() {
        // Test various invalid phone formats
        assertFalse("Too short should be invalid", isValidPhone("012345"));
        assertFalse("Too long should be invalid", isValidPhone("01234567890"));
        assertFalse("Invalid prefix should be invalid", isValidPhone("0123456789"));
        assertFalse("Landline format should be invalid", isValidPhone("0212345678"));
        assertFalse("Contains letters should be invalid", isValidPhone("012345678a"));
        assertFalse("Contains spaces should be invalid", isValidPhone("012 345 6789"));
        assertFalse("Contains dashes should be invalid", isValidPhone("012-345-6789"));
        assertFalse("Empty should be invalid", isValidPhone(""));
    }

    /**
     * Property: Valid email addresses should be accepted
     * Valid emails follow standard email format with @ and domain
     */
    @Test
    public void testValidEmailProperty() {
        // Test various valid email formats
        assertTrue("Simple email should be valid", isValidEmail("test@example.com"));
        assertTrue("Email with subdomain should be valid", isValidEmail("user@mail.example.com"));
        assertTrue("Email with numbers should be valid", isValidEmail("user123@example.com"));
        assertTrue("Email with dots should be valid", isValidEmail("first.last@example.com"));
        assertTrue("Email with plus should be valid", isValidEmail("user+tag@example.com"));
        assertTrue("Email with underscore should be valid", isValidEmail("user_name@example.com"));
    }

    /**
     * Property: Invalid email addresses should be rejected
     * Invalid emails lack @, domain, or have invalid format
     */
    @Test
    public void testInvalidEmailProperty() {
        // Test various invalid email formats
        assertFalse("Missing @ should be invalid", isValidEmail("testexample.com"));
        assertFalse("Missing domain should be invalid", isValidEmail("test@"));
        assertFalse("Missing local part should be invalid", isValidEmail("@example.com"));
        assertFalse("Missing TLD should be invalid", isValidEmail("test@example"));
        assertFalse("Double @ should be invalid", isValidEmail("test@@example.com"));
        assertFalse("Spaces should be invalid", isValidEmail("test @example.com"));
        assertFalse("Empty should be invalid", isValidEmail(""));
    }

    /**
     * Property: Valid descriptions should be accepted
     * Valid descriptions have at least 10 characters
     */
    @Test
    public void testValidDescriptionProperty() {
        // Test various valid descriptions
        assertTrue("Minimum length description should be valid", 
                   isValidDescription("This is a test description"));
        assertTrue("Long description should be valid", 
                   isValidDescription("This is a very long description that explains the problem in detail"));
        assertTrue("Description with numbers should be valid", 
                   isValidDescription("Error code 404 occurred when accessing the system"));
        assertTrue("Description with special characters should be valid", 
                   isValidDescription("System shows error: 'Connection failed!' message"));
    }

    /**
     * Property: Invalid descriptions should be rejected
     * Invalid descriptions are empty or too short (less than 10 characters)
     */
    @Test
    public void testInvalidDescriptionProperty() {
        // Test various invalid descriptions
        assertFalse("Empty description should be invalid", isValidDescription(""));
        assertFalse("Too short description should be invalid", isValidDescription("Short"));
        assertFalse("Only spaces should be invalid", isValidDescription("         "));
        assertFalse("9 characters should be invalid", isValidDescription("123456789"));
    }

    // Helper methods that mirror the validation logic in YeuCauHoTroActivity

    private boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return name.trim().length() >= 2;
    }

    private boolean isValidPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        String cleanPhone = phone.replaceAll("[\\s\\-\\(\\)]", "");
        return PHONE_PATTERN.matcher(cleanPhone).matches();
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return true; // Email is optional
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    private boolean isValidDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            return false;
        }
        return description.trim().length() >= 10;
    }
}
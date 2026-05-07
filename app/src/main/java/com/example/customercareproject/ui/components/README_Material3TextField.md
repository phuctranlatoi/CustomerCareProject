# Material3TextField Component

## Overview

`Material3TextField` is a custom text field component that extends `TextInputLayout` with enhanced features for consistent input field styling and validation across the app. It follows Material Design 3 guidelines and provides built-in support for error handling, character counting, and password strength indication.

## Features

- **Floating Label Animation**: Smooth label animation when user focuses on the field (built-in with TextInputLayout)
- **Inline Error Messages**: Display error messages below the field with red color
- **Shake Animation**: Automatically shakes the field when an error is set
- **Character Counter**: Built-in support for character counting (via TextInputLayout)
- **Password Strength Indicator**: Visual indicator showing password strength (weak/medium/strong)
- **Material Design 3 Styling**: Follows MD3 specifications with proper colors, shapes, and spacing

## Usage

### Basic Usage in XML

```xml
<com.example.customercareproject.ui.components.Material3TextField
    android:id="@+id/txtEmail"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:hint="Email"
    app:counterEnabled="true"
    app:counterMaxLength="50" />
```

### Password Field with Strength Indicator

```xml
<com.example.customercareproject.ui.components.Material3TextField
    android:id="@+id/txtPassword"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:hint="Password"
    android:inputType="textPassword"
    app:showPasswordStrength="true"
    app:passwordToggleEnabled="true" />
```

### Usage in Java

```java
// Get reference to text field
Material3TextField emailField = findViewById(R.id.txtEmail);

// Set text
emailField.setText("user@example.com");

// Get text
String email = emailField.getText();

// Set error (triggers shake animation)
emailField.setError("Invalid email format");

// Clear error
emailField.clearError();

// Enable password strength indicator
Material3TextField passwordField = findViewById(R.id.txtPassword);
passwordField.setShowPasswordStrength(true);

// Add text change listener
emailField.addTextChangedListener(new TextWatcher() {
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // Validate input
        if (!isValidEmail(s.toString())) {
            emailField.setError("Invalid email");
        } else {
            emailField.clearError();
        }
    }
    // ... other methods
});
```

## Attributes

### Custom Attributes

| Attribute | Type | Description | Default |
|-----------|------|-------------|---------|
| `app:showPasswordStrength` | boolean | Show password strength indicator | false |
| `android:hint` | string | Hint text for the field | null |
| `android:inputType` | enum | Input type (text, email, password, etc.) | text |

### Inherited TextInputLayout Attributes

All standard `TextInputLayout` attributes are supported:

- `app:counterEnabled` - Enable character counter
- `app:counterMaxLength` - Maximum character count
- `app:passwordToggleEnabled` - Show/hide password toggle
- `app:endIconMode` - End icon mode (password toggle, clear text, etc.)
- `app:startIconDrawable` - Start icon drawable
- `app:helperText` - Helper text below the field
- `app:helperTextEnabled` - Enable helper text

## Password Strength Indicator

When `showPasswordStrength="true"` is set on a password field, the component displays a visual indicator showing password strength:

### Strength Levels

1. **Weak** (Red)
   - Less than 6 characters
   - Only letters or only numbers

2. **Medium** (Orange)
   - 6-8 characters with letters and numbers
   - OR any combination of 2 criteria

3. **Strong** (Green)
   - More than 8 characters
   - Contains letters, numbers, AND special characters

### Visual Indicator

The indicator consists of:
- A horizontal progress bar showing strength level (0-2)
- A text label showing "Yếu", "Trung bình", or "Mạnh"
- Color-coded based on strength (red/orange/green)

## Error Handling

### Setting Errors

```java
// Set error with shake animation
textField.setError("This field is required");

// Error appears below the field in red color
// Field automatically shakes to draw attention
```

### Clearing Errors

```java
// Clear error message
textField.clearError();
```

### Shake Animation

When an error is set, the field automatically shakes horizontally using `AnimationHelper.shake()`. This provides visual feedback to the user that something is wrong.

## Styling

### Material Design 3 Styling

The component automatically applies MD3 styling:

- **Box Style**: Outlined (default for MD3)
- **Corner Radius**: 8dp (small, from shape system)
- **Box Stroke Color**: Primary color from theme
- **Error Color**: Error color from theme (#EF4444)
- **Floating Label**: Enabled with smooth animation

### Customizing Colors

Colors are automatically pulled from the theme:

```xml
<!-- In themes.xml -->
<item name="colorPrimary">@color/primary</item>
<item name="colorError">@color/error</item>
```

## Examples

### Email Field with Validation

```java
Material3TextField emailField = findViewById(R.id.txtEmail);

emailField.addTextChangedListener(new TextWatcher() {
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String email = s.toString();
        if (email.isEmpty()) {
            emailField.setError("Email is required");
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailField.setError("Invalid email format");
        } else {
            emailField.clearError();
        }
    }
    
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
    
    @Override
    public void afterTextChanged(Editable s) {}
});
```

### Password Field with Strength Indicator

```xml
<com.example.customercareproject.ui.components.Material3TextField
    android:id="@+id/txtPassword"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:hint="Password"
    android:inputType="textPassword"
    app:showPasswordStrength="true"
    app:passwordToggleEnabled="true"
    app:counterEnabled="true"
    app:counterMaxLength="50" />
```

### Phone Number Field

```xml
<com.example.customercareproject.ui.components.Material3TextField
    android:id="@+id/txtPhone"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:hint="Phone Number"
    android:inputType="phone"
    app:startIconDrawable="@drawable/ic_phone"
    app:counterEnabled="true"
    app:counterMaxLength="15" />
```

### Multi-line Text Field

```xml
<com.example.customercareproject.ui.components.Material3TextField
    android:id="@+id/txtDescription"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:hint="Description"
    android:inputType="textMultiLine"
    android:minLines="3"
    app:counterEnabled="true"
    app:counterMaxLength="500" />
```

## API Reference

### Public Methods

#### Text Operations

```java
// Get text value
String getText()

// Set text value
void setText(String text)

// Get EditText for advanced customization
TextInputEditText getEditText()
```

#### Error Handling

```java
// Set error with shake animation
void setError(@Nullable CharSequence error)

// Clear error
void clearError()
```

#### Password Strength

```java
// Enable/disable password strength indicator
void setShowPasswordStrength(boolean show)

// Check if password strength is enabled
boolean isShowPasswordStrength()
```

#### Input Type

```java
// Set input type
void setInputType(int type)

// Get input type
int getInputType()
```

#### Text Watchers

```java
// Add text change listener
void addTextChangedListener(TextWatcher watcher)

// Remove text change listener
void removeTextChangedListener(TextWatcher watcher)
```

## Requirements Mapping

This component satisfies the following requirements from the Modern UI Redesign spec:

- **Requirement 1.5**: Uses Material Design 3 components (TextInputLayout)
- **Requirement 7.2**: Outlined text fields with floating labels
- **Requirement 7.3**: Password strength indicator on registration screen
- **Requirement 7.4**: Inline error messages with red color
- **Requirement 17.3**: Inline error messages below form fields with red color

## Dependencies

- `com.google.android.material:material:1.11.0+` - Material Components
- `AnimationHelper` - For shake animation

## Notes

- The component extends `TextInputLayout`, so all standard TextInputLayout features are available
- Password strength calculation is based on length and character variety (letters, numbers, special characters)
- Shake animation is automatically triggered when `setError()` is called with a non-null message
- The floating label animation is built into TextInputLayout and works automatically
- Character counter is provided by TextInputLayout via `app:counterEnabled` and `app:counterMaxLength`

## See Also

- [Material3Button](README_Material3Button.md)
- [Material3Card](README_Material3Card.md)
- [AnimationHelper](../../utils/AnimationHelper.java)

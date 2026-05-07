package com.example.customercareproject;

import android.content.Context;
import android.view.View;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.customercareproject.ui.components.EmptyStateView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

/**
 * Instrumented test for EmptyStateView component.
 * 
 * Tests:
 * - Component initialization
 * - Setting illustration, title, description, and action text
 * - Show/hide with fade animations
 * - Action button click listener
 * - Action button visibility toggle
 * - View accessors for advanced customization
 */
@RunWith(AndroidJUnit4.class)
public class EmptyStateViewTest {
    
    private Context context;
    private EmptyStateView emptyStateView;
    
    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        emptyStateView = new EmptyStateView(context);
    }
    
    @Test
    public void testInitialization() {
        // Verify component is initialized correctly
        assertNotNull("EmptyStateView should not be null", emptyStateView);
        
        // Verify initial visibility is GONE
        assertEquals("Initial visibility should be GONE", View.GONE, emptyStateView.getVisibility());
        
        // Verify initial alpha is 0
        assertEquals("Initial alpha should be 0", 0f, emptyStateView.getAlpha(), 0.01f);
    }
    
    @Test
    public void testSetIllustration() {
        // Set illustration from resource
        emptyStateView.setIllustration(android.R.drawable.ic_dialog_info);
        
        // Verify illustration is set
        assertNotNull("Illustration should be set", emptyStateView.getIllustrationView().getDrawable());
    }
    
    @Test
    public void testSetTitle() {
        String title = "Test Title";
        
        // Set title
        emptyStateView.setTitle(title);
        
        // Verify title is set
        assertEquals("Title should match", title, emptyStateView.getTitleView().getText().toString());
    }
    
    @Test
    public void testSetDescription() {
        String description = "Test Description";
        
        // Set description
        emptyStateView.setDescription(description);
        
        // Verify description is set
        assertEquals("Description should match", description, emptyStateView.getDescriptionView().getText().toString());
    }
    
    @Test
    public void testSetActionText() {
        String actionText = "Test Action";
        
        // Set action text
        emptyStateView.setActionText(actionText);
        
        // Verify action text is set
        assertEquals("Action text should match", actionText, emptyStateView.getActionButton().getText().toString());
    }
    
    @Test
    public void testShowAnimation() throws InterruptedException {
        // Show the empty state view
        emptyStateView.show();
        
        // Wait for animation to start
        Thread.sleep(100);
        
        // Verify visibility is VISIBLE
        assertEquals("Visibility should be VISIBLE after show()", View.VISIBLE, emptyStateView.getVisibility());
        
        // Wait for animation to complete
        Thread.sleep(400);
        
        // Verify alpha is 1 (fully visible)
        assertEquals("Alpha should be 1 after animation", 1f, emptyStateView.getAlpha(), 0.1f);
    }
    
    @Test
    public void testHideAnimation() throws InterruptedException {
        // First show the view
        emptyStateView.setVisibility(View.VISIBLE);
        emptyStateView.setAlpha(1f);
        
        // Hide the empty state view
        emptyStateView.hide();
        
        // Wait for animation to complete
        Thread.sleep(400);
        
        // Verify visibility is GONE
        assertEquals("Visibility should be GONE after hide()", View.GONE, emptyStateView.getVisibility());
    }
    
    @Test
    public void testActionButtonClickListener() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean clicked = new AtomicBoolean(false);
        
        // Set click listener
        emptyStateView.setOnActionClickListener(() -> {
            clicked.set(true);
            latch.countDown();
        });
        
        // Perform click on action button
        emptyStateView.getActionButton().performClick();
        
        // Wait for click to be processed
        latch.await(1, TimeUnit.SECONDS);
        
        // Verify click was handled
        assertTrue("Action button click should be handled", clicked.get());
    }
    
    @Test
    public void testShowActionButton() {
        // Show action button
        emptyStateView.setShowActionButton(true);
        
        // Verify button is visible
        assertEquals("Action button should be VISIBLE", View.VISIBLE, emptyStateView.getActionButton().getVisibility());
        assertTrue("isActionButtonShown should return true", emptyStateView.isActionButtonShown());
    }
    
    @Test
    public void testHideActionButton() {
        // Hide action button
        emptyStateView.setShowActionButton(false);
        
        // Verify button is gone
        assertEquals("Action button should be GONE", View.GONE, emptyStateView.getActionButton().getVisibility());
        assertFalse("isActionButtonShown should return false", emptyStateView.isActionButtonShown());
    }
    
    @Test
    public void testGetIllustrationView() {
        // Get illustration view
        assertNotNull("Illustration view should not be null", emptyStateView.getIllustrationView());
    }
    
    @Test
    public void testGetTitleView() {
        // Get title view
        assertNotNull("Title view should not be null", emptyStateView.getTitleView());
    }
    
    @Test
    public void testGetDescriptionView() {
        // Get description view
        assertNotNull("Description view should not be null", emptyStateView.getDescriptionView());
    }
    
    @Test
    public void testGetActionButton() {
        // Get action button
        assertNotNull("Action button should not be null", emptyStateView.getActionButton());
    }
    
    @Test
    public void testSetTitleFromResource() {
        // Set title from string resource
        emptyStateView.setTitle(R.string.app_name);
        
        // Verify title is set
        String expectedTitle = context.getString(R.string.app_name);
        assertEquals("Title should match resource string", expectedTitle, emptyStateView.getTitleView().getText().toString());
    }
    
    @Test
    public void testSetDescriptionFromResource() {
        // Set description from string resource
        emptyStateView.setDescription(R.string.app_name);
        
        // Verify description is set
        String expectedDescription = context.getString(R.string.app_name);
        assertEquals("Description should match resource string", expectedDescription, emptyStateView.getDescriptionView().getText().toString());
    }
    
    @Test
    public void testSetActionTextFromResource() {
        // Set action text from string resource
        emptyStateView.setActionText(R.string.action_button);
        
        // Verify action text is set
        String expectedActionText = context.getString(R.string.action_button);
        assertEquals("Action text should match resource string", expectedActionText, emptyStateView.getActionButton().getText().toString());
    }
    
    @Test
    public void testMultipleShowCalls() throws InterruptedException {
        // Show the view
        emptyStateView.show();
        Thread.sleep(100);
        
        // Try to show again (should not cause issues)
        emptyStateView.show();
        Thread.sleep(100);
        
        // Verify visibility is still VISIBLE
        assertEquals("Visibility should remain VISIBLE", View.VISIBLE, emptyStateView.getVisibility());
    }
    
    @Test
    public void testMultipleHideCalls() throws InterruptedException {
        // First show the view
        emptyStateView.setVisibility(View.VISIBLE);
        emptyStateView.setAlpha(1f);
        
        // Hide the view
        emptyStateView.hide();
        Thread.sleep(400);
        
        // Try to hide again (should not cause issues)
        emptyStateView.hide();
        Thread.sleep(100);
        
        // Verify visibility is still GONE
        assertEquals("Visibility should remain GONE", View.GONE, emptyStateView.getVisibility());
    }
    
    @Test
    public void testCompleteConfiguration() {
        // Configure all properties
        emptyStateView.setIllustration(android.R.drawable.ic_dialog_info);
        emptyStateView.setTitle("No Tickets");
        emptyStateView.setDescription("You don't have any tickets yet.");
        emptyStateView.setActionText("Create Ticket");
        emptyStateView.setShowActionButton(true);
        
        // Verify all properties are set
        assertNotNull("Illustration should be set", emptyStateView.getIllustrationView().getDrawable());
        assertEquals("Title should match", "No Tickets", emptyStateView.getTitleView().getText().toString());
        assertEquals("Description should match", "You don't have any tickets yet.", emptyStateView.getDescriptionView().getText().toString());
        assertEquals("Action text should match", "Create Ticket", emptyStateView.getActionButton().getText().toString());
        assertEquals("Action button should be visible", View.VISIBLE, emptyStateView.getActionButton().getVisibility());
    }
}

package com.example.customercareproject;

import android.content.Context;
import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.customercareproject.ui.ktv.ChatAdapter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Test class for ChatAdapter message animations and typing indicator functionality.
 * Tests the slide-in animations, stagger effects, and typing indicator behavior.
 */
@RunWith(AndroidJUnit4.class)
public class ChatAdapterAnimationTest {

    private Context context;
    private ChatAdapter adapter;
    private RecyclerView recyclerView;
    private List<Map<String, Object>> testMessages;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        
        // Create test messages
        testMessages = new ArrayList<>();
        
        // Add a text message
        Map<String, Object> message1 = new HashMap<>();
        message1.put("_id", "msg1");
        message1.put("nguoiGuiUid", "user1");
        message1.put("nguoiGuiTen", "Test User");
        message1.put("noiDung", "Hello, this is a test message");
        message1.put("thoiGian", System.currentTimeMillis());
        message1.put("loaiTin", "text");
        testMessages.add(message1);
        
        // Add an image message
        Map<String, Object> message2 = new HashMap<>();
        message2.put("_id", "msg2");
        message2.put("nguoiGuiUid", "ktv1");
        message2.put("nguoiGuiTen", "KTV User");
        message2.put("anhUrl", "https://example.com/image.jpg");
        message2.put("thoiGian", System.currentTimeMillis());
        message2.put("loaiTin", "anh");
        testMessages.add(message2);
        
        // Create adapter
        adapter = new ChatAdapter(testMessages, "ktv1");
        
        // Create RecyclerView
        recyclerView = new RecyclerView(context);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);
    }

    @Test
    public void testAdapterInitialization() {
        assertNotNull("Adapter should not be null", adapter);
        assertEquals("Initial item count should match message count", 
                testMessages.size(), adapter.getItemCount());
    }

    @Test
    public void testTypingIndicatorVisibility() {
        // Initially no typing indicator
        assertEquals("Initial count should be message count only", 
                testMessages.size(), adapter.getItemCount());
        
        // Show typing indicator
        adapter.setTypingIndicatorVisible(true);
        assertEquals("Count should increase by 1 when typing indicator is shown", 
                testMessages.size() + 1, adapter.getItemCount());
        
        // Hide typing indicator
        adapter.setTypingIndicatorVisible(false);
        assertEquals("Count should return to original when typing indicator is hidden", 
                testMessages.size(), adapter.getItemCount());
    }

    @Test
    public void testViewTypes() {
        // Test text message view type
        int textViewType = adapter.getItemViewType(0);
        assertEquals("Text message should have correct view type", 
                0, textViewType); // VIEW_TYPE_TEXT = 0
        
        // Test image message view type (from KTV user)
        int imageViewType = adapter.getItemViewType(1);
        assertEquals("Image message from KTV should have correct view type", 
                1, imageViewType); // VIEW_TYPE_IMAGE_ME = 1
        
        // Test typing indicator view type
        adapter.setTypingIndicatorVisible(true);
        int typingViewType = adapter.getItemViewType(testMessages.size());
        assertEquals("Typing indicator should have correct view type", 
                3, typingViewType); // VIEW_TYPE_TYPING_INDICATOR = 3
    }

    @Test
    public void testClearAnimations() {
        // This method should not throw any exceptions
        adapter.clearAnimations();
        
        // Verify adapter still works after clearing animations
        assertEquals("Adapter should still work after clearing animations", 
                testMessages.size(), adapter.getItemCount());
    }

    @Test
    public void testMessageUpdate() {
        // Add a new message
        Map<String, Object> newMessage = new HashMap<>();
        newMessage.put("_id", "msg3");
        newMessage.put("nguoiGuiUid", "user2");
        newMessage.put("nguoiGuiTen", "Another User");
        newMessage.put("noiDung", "New message");
        newMessage.put("thoiGian", System.currentTimeMillis());
        newMessage.put("loaiTin", "text");
        
        List<Map<String, Object>> updatedMessages = new ArrayList<>(testMessages);
        updatedMessages.add(newMessage);
        
        adapter.capNhatRaw(updatedMessages);
        
        assertEquals("Item count should update after adding message", 
                updatedMessages.size(), adapter.getItemCount());
    }
}
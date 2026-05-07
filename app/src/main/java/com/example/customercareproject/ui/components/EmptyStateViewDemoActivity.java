package com.example.customercareproject.ui.components;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.customercareproject.R;
import com.google.android.material.button.MaterialButton;

/**
 * Demo activity for EmptyStateView component.
 * 
 * This activity demonstrates the usage of EmptyStateView with different configurations:
 * - Empty tickets state
 * - Empty messages state
 * - Empty search results state
 * - Toggle visibility with fade animations
 */
public class EmptyStateViewDemoActivity extends AppCompatActivity {
    
    private EmptyStateView emptyStateTickets;
    private EmptyStateView emptyStateMessages;
    private EmptyStateView emptyStateSearch;
    
    private MaterialButton btnToggleTickets;
    private MaterialButton btnToggleMessages;
    private MaterialButton btnToggleSearch;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty_state_view_demo);
        
        // Setup toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("EmptyStateView Demo");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        // Find views
        emptyStateTickets = findViewById(R.id.emptyStateTickets);
        emptyStateMessages = findViewById(R.id.emptyStateMessages);
        emptyStateSearch = findViewById(R.id.emptyStateSearch);
        
        btnToggleTickets = findViewById(R.id.btnToggleTickets);
        btnToggleMessages = findViewById(R.id.btnToggleMessages);
        btnToggleSearch = findViewById(R.id.btnToggleSearch);
        
        // Setup empty state views
        setupEmptyStateTickets();
        setupEmptyStateMessages();
        setupEmptyStateSearch();
        
        // Setup toggle buttons
        setupToggleButtons();
    }
    
    /**
     * Setup empty state for tickets.
     */
    private void setupEmptyStateTickets() {
        emptyStateTickets.setIllustration(android.R.drawable.ic_dialog_info);
        emptyStateTickets.setTitle("No Tickets");
        emptyStateTickets.setDescription("You don't have any tickets yet.\nCreate one to get started.");
        emptyStateTickets.setActionText("Create Ticket");
        emptyStateTickets.setShowActionButton(true);
        
        emptyStateTickets.setOnActionClickListener(() -> {
            Toast.makeText(this, "Create Ticket clicked", Toast.LENGTH_SHORT).show();
        });
    }
    
    /**
     * Setup empty state for messages.
     */
    private void setupEmptyStateMessages() {
        emptyStateMessages.setIllustration(android.R.drawable.ic_dialog_email);
        emptyStateMessages.setTitle("No Messages");
        emptyStateMessages.setDescription("You don't have any messages yet.\nStart a conversation to get help.");
        emptyStateMessages.setActionText("Start Chat");
        emptyStateMessages.setShowActionButton(true);
        
        emptyStateMessages.setOnActionClickListener(() -> {
            Toast.makeText(this, "Start Chat clicked", Toast.LENGTH_SHORT).show();
        });
    }
    
    /**
     * Setup empty state for search results.
     */
    private void setupEmptyStateSearch() {
        emptyStateSearch.setIllustration(android.R.drawable.ic_menu_search);
        emptyStateSearch.setTitle("No Results Found");
        emptyStateSearch.setDescription("We couldn't find any results matching your search.\nTry different keywords.");
        emptyStateSearch.setActionText("Clear Search");
        emptyStateSearch.setShowActionButton(true);
        
        emptyStateSearch.setOnActionClickListener(() -> {
            Toast.makeText(this, "Clear Search clicked", Toast.LENGTH_SHORT).show();
        });
    }
    
    /**
     * Setup toggle buttons to show/hide empty states.
     */
    private void setupToggleButtons() {
        btnToggleTickets.setOnClickListener(v -> {
            if (emptyStateTickets.getVisibility() == android.view.View.VISIBLE) {
                emptyStateTickets.hide();
                btnToggleTickets.setText("Show Tickets Empty State");
            } else {
                emptyStateTickets.show();
                btnToggleTickets.setText("Hide Tickets Empty State");
            }
        });
        
        btnToggleMessages.setOnClickListener(v -> {
            if (emptyStateMessages.getVisibility() == android.view.View.VISIBLE) {
                emptyStateMessages.hide();
                btnToggleMessages.setText("Show Messages Empty State");
            } else {
                emptyStateMessages.show();
                btnToggleMessages.setText("Hide Messages Empty State");
            }
        });
        
        btnToggleSearch.setOnClickListener(v -> {
            if (emptyStateSearch.getVisibility() == android.view.View.VISIBLE) {
                emptyStateSearch.hide();
                btnToggleSearch.setText("Show Search Empty State");
            } else {
                emptyStateSearch.show();
                btnToggleSearch.setText("Hide Search Empty State");
            }
        });
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

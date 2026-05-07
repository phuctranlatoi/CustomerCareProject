package com.example.customercareproject.ui.components;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.customercareproject.R;

/**
 * Demo activity showcasing Material3Card component usage.
 * 
 * This activity demonstrates:
 * - Different corner radius sizes (small, medium, large, extra_large)
 * - Clickable cards with ripple effect
 * - Non-clickable cards (statistics)
 * - Hover effects
 * - Different elevations
 */
public class Material3CardDemoActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material3_card_demo);
        
        setupCards();
    }
    
    /**
     * Setup card click listeners and configurations.
     */
    private void setupCards() {
        // Product card with medium corners (clickable)
        Material3Card cardProduct = findViewById(R.id.cardProduct);
        if (cardProduct != null) {
            cardProduct.setOnClickListener(v -> 
                Toast.makeText(this, "Product card clicked!", Toast.LENGTH_SHORT).show()
            );
        }
        
        // Ticket card with extra large corners (clickable)
        Material3Card cardTicket = findViewById(R.id.cardTicket);
        if (cardTicket != null) {
            cardTicket.setOnClickListener(v -> 
                Toast.makeText(this, "Ticket card clicked!", Toast.LENGTH_SHORT).show()
            );
        }
        
        // Statistics cards (non-clickable, no hover effect)
        Material3Card cardStat1 = findViewById(R.id.cardStat1);
        Material3Card cardStat2 = findViewById(R.id.cardStat2);
        
        // These cards are configured in XML as non-clickable
        // No click listeners needed
        
        // Small corner card (clickable)
        Material3Card cardSmall = findViewById(R.id.cardSmall);
        if (cardSmall != null) {
            cardSmall.setOnClickListener(v -> 
                Toast.makeText(this, "Small corner card clicked!", Toast.LENGTH_SHORT).show()
            );
        }
        
        // Large corner card (clickable)
        Material3Card cardLarge = findViewById(R.id.cardLarge);
        if (cardLarge != null) {
            cardLarge.setOnClickListener(v -> 
                Toast.makeText(this, "Large corner card clicked!", Toast.LENGTH_SHORT).show()
            );
        }
        
        // Programmatic configuration example
        Material3Card cardProgrammatic = findViewById(R.id.cardProgrammatic);
        if (cardProgrammatic != null) {
            // Configure programmatically
            cardProgrammatic.setCornerRadiusSize(1); // 1 = medium
            cardProgrammatic.setHoverEffectEnabled(true);
            
            cardProgrammatic.setOnClickListener(v -> {
                Toast.makeText(this, "Programmatically configured card clicked!", Toast.LENGTH_SHORT).show();
                
                // Example: Cycle through corner radius sizes
                // Since we can't get current size, we'll just cycle through them
                // This is a simplified example
                cardProgrammatic.setCornerRadiusSize(2); // large
                Toast.makeText(this, "Corner radius changed to large", Toast.LENGTH_SHORT).show();
            });
        }
    }
}

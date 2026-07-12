package com.surplusfood.marketplace.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.surplusfood.marketplace.dto.ChatbotResponse;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ChatbotServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ChatbotService chatbotService = new ChatbotService(objectMapper);

    @Test
    public void testExtractLocally_VegFriedRice() {
        String description = "I have 10 portions of veg fried rice, refrigerated. Best before midnight today. Packaging is packed.";
        
        ChatbotResponse response = chatbotService.extractInformation(description);
        
        assertNotNull(response);
        assertEquals("veg fried rice", response.foodName().toLowerCase());
        assertEquals("Prepared Meals", response.foodCategory());
        assertEquals("10 portions", response.quantity());
        assertEquals("Refrigerated", response.storageType());
        assertEquals("Vegetarian", response.foodType());
        assertEquals("Packed", response.packagingStatus());
        assertEquals("65", response.confidence());
    }

    @Test
    public void testExtractLocally_BakeryBread() {
        String description = "5 boxes of chocolate muffins cooked 2 hours ago. Room temp. Vegetarian.";
        
        ChatbotResponse response = chatbotService.extractInformation(description);
        
        assertNotNull(response);
        assertEquals("chocolate muffins", response.foodName().toLowerCase());
        assertEquals("Bakery", response.foodCategory());
        assertEquals("5 boxes", response.quantity());
        assertEquals("Room Temperature", response.storageType());
        assertEquals("Vegetarian", response.foodType());
        assertEquals("65", response.confidence());
    }

    @Test
    public void testExtractLocally_NonVegPasta() {
        String description = "We got 2 kg of chicken pasta, stored in the freezer. Non-veg. Loose container.";
        
        ChatbotResponse response = chatbotService.extractInformation(description);
        
        assertNotNull(response);
        assertEquals("chicken pasta", response.foodName().toLowerCase());
        assertEquals("Prepared Meals", response.foodCategory());
        assertEquals("2 kg", response.quantity());
        assertEquals("Frozen", response.storageType());
        assertEquals("Non-Vegetarian", response.foodType());
        assertEquals("Not Packed", response.packagingStatus());
        assertEquals("65", response.confidence());
    }
}

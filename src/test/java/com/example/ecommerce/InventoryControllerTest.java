package com.example.ecommerce;

import com.example.ecommerce.controller.InventoryController;
import com.example.ecommerce.dto.CancelRequest;
import com.example.ecommerce.dto.ReserveRequest;
import com.example.ecommerce.dto.SupplyRequest;
import com.example.ecommerce.entity.Inventory;
import com.example.ecommerce.entity.Reservation;
import com.example.ecommerce.service.InventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InventoryController.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private InventoryService inventoryService;

    // --- createSupply ---

    @Test
    void createSupply_shouldReturn201() throws Exception {
        SupplyRequest request = new SupplyRequest();
        request.setSku("IPH15");
        request.setItemName("iPhone 15");
        request.setQuantity(10);

        Inventory mockInventory = Inventory.builder()
                .sku("IPH15").itemName("iPhone 15")
                .totalQuantity(10).availableQuantity(10).reservedQuantity(0)
                .build();

        when(inventoryService.createSupply(any())).thenReturn(mockInventory);

        mockMvc.perform(post("/api/inventory/supply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku").value("IPH15"))
                .andExpect(jsonPath("$.availableQuantity").value(10));
    }

    @Test
    void createSupply_missingFields_shouldReturn400() throws Exception {
        // empty body — validation should reject it
        mockMvc.perform(post("/api/inventory/supply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // --- reserveItem ---

    @Test
    void reserve_shouldReturn201() throws Exception {
        ReserveRequest request = new ReserveRequest();
        request.setSku("IPH15");
        request.setQuantity(3);

        Reservation mockReservation = Reservation.builder()
                .sku("IPH15").status("RESERVED").quantity(3)
                .build();

        when(inventoryService.reserveItem(any())).thenReturn(mockReservation);

        mockMvc.perform(post("/api/inventory/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("RESERVED"))
                .andExpect(jsonPath("$.quantity").value(3));
    }

    @Test
    void reserve_insufficientStock_shouldReturn400() throws Exception {
        ReserveRequest request = new ReserveRequest();
        request.setSku("IPH15");
        request.setQuantity(100);

        when(inventoryService.reserveItem(any()))
                .thenThrow(new RuntimeException("Not Enough Stock Available"));

        mockMvc.perform(post("/api/inventory/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Not Enough Stock Available"));
    }

    // --- cancelReservation ---

    @Test
    void cancel_shouldReturn200() throws Exception {
        CancelRequest request = new CancelRequest();
        request.setQuantity(2);

        Reservation mockReservation = Reservation.builder()
                .sku("IPH15").status("RESERVED").quantity(1)
                .build();

        when(inventoryService.cancelReservedItem(eq(1L), any()))
                .thenReturn(mockReservation);

        mockMvc.perform(post("/api/inventory/reservation/1/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void cancel_notFound_shouldReturn400() throws Exception {
        CancelRequest request = new CancelRequest();
        request.setQuantity(1);

        when(inventoryService.cancelReservedItem(eq(99L), any()))
                .thenThrow(new RuntimeException("Reservation not Found"));

        mockMvc.perform(post("/api/inventory/reservation/99/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Reservation not Found"));
    }

    // --- getAvailability ---

    @Test
    void getAvailability_shouldReturn200() throws Exception {
        Inventory mockInventory = Inventory.builder()
                .sku("IPH15").itemName("iPhone 15")
                .totalQuantity(10).availableQuantity(7).reservedQuantity(3)
                .build();

        when(inventoryService.getAvailability("IPH15")).thenReturn(mockInventory);

        mockMvc.perform(get("/api/inventory/availability")
                        .param("sku", "IPH15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("IPH15"))
                .andExpect(jsonPath("$.availableQuantity").value(7));
    }

    @Test
    void getAvailability_notFound_shouldReturn400() throws Exception {
        when(inventoryService.getAvailability("UNKNOWN"))
                .thenThrow(new RuntimeException("Item not found"));

        mockMvc.perform(get("/api/inventory/availability")
                        .param("sku", "UNKNOWN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Item not found"));
    }
}
package com.example.ecommerce;

import com.example.ecommerce.dto.CancelRequest;
import com.example.ecommerce.dto.ReserveRequest;
import com.example.ecommerce.dto.SupplyRequest;
import com.example.ecommerce.entity.Inventory;
import com.example.ecommerce.entity.Reservation;
import com.example.ecommerce.repository.InventoryRepository;
import com.example.ecommerce.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class InventoryServiceTest {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Test
    void reserveItem_success() {
        SupplyRequest supplyRequest = new SupplyRequest();
        supplyRequest.setSku("IPH15");
        supplyRequest.setItemName("iPhone 15");
        supplyRequest.setQuantity(10);

        inventoryService.createSupply(supplyRequest);

        ReserveRequest reserveRequest = new ReserveRequest();
        reserveRequest.setSku("IPH15");
        reserveRequest.setQuantity(3);

        Reservation reservation = inventoryService.reserveItem(reserveRequest);

        assertNotNull(reservation.getId());
        assertEquals("IPH15", reservation.getSku());
        assertEquals(3, reservation.getQuantity());
        assertEquals("RESERVED", reservation.getStatus());

        Inventory inventory = inventoryRepository.findBySku("IPH15").orElseThrow();

        assertEquals(7, inventory.getAvailableQuantity());
        assertEquals(3, inventory.getReservedQuantity());
    }

    @Test
    void reserveItem_insufficientStock_shouldThrowException() {
        SupplyRequest supplyRequest = new SupplyRequest();
        supplyRequest.setSku("SAMS24");
        supplyRequest.setItemName("Samsung S24");
        supplyRequest.setQuantity(5);

        inventoryService.createSupply(supplyRequest);

        ReserveRequest reserveRequest = new ReserveRequest();
        reserveRequest.setSku("SAMS24");
        reserveRequest.setQuantity(10);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> inventoryService.reserveItem(reserveRequest)
        );

        assertEquals("Not Enough Stock Available", exception.getMessage());
    }

    @Test
    void cancelReservation_success() {
        SupplyRequest supplyRequest = new SupplyRequest();
        supplyRequest.setSku("MACBOOK");
        supplyRequest.setItemName("MacBook Air");
        supplyRequest.setQuantity(10);

        inventoryService.createSupply(supplyRequest);

        ReserveRequest reserveRequest = new ReserveRequest();
        reserveRequest.setSku("MACBOOK");
        reserveRequest.setQuantity(4);

        Reservation reservation = inventoryService.reserveItem(reserveRequest);

        CancelRequest cancelRequest = new CancelRequest();
        cancelRequest.setQuantity(2);

        Reservation updatedReservation =
                inventoryService.cancelReservedItem(reservation.getId(), cancelRequest);

        assertEquals(2, updatedReservation.getQuantity());
        assertEquals("RESERVED", updatedReservation.getStatus());

        Inventory inventory = inventoryRepository.findBySku("MACBOOK").orElseThrow();

        assertEquals(8, inventory.getAvailableQuantity());
        assertEquals(2, inventory.getReservedQuantity());
    }

    @Test
    void concurrentReservation_shouldNotOversellStock() throws InterruptedException {
        SupplyRequest supplyRequest = new SupplyRequest();
        supplyRequest.setSku("PS5");
        supplyRequest.setItemName("PlayStation 5");
        supplyRequest.setQuantity(5);

        inventoryService.createSupply(supplyRequest);

        ReserveRequest reserveRequest = new ReserveRequest();
        reserveRequest.setSku("PS5");
        reserveRequest.setQuantity(4);

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        CountDownLatch latch = new CountDownLatch(1);

        Runnable task = () -> {
            try {
                latch.await();
                inventoryService.reserveItem(reserveRequest);
                successCount.incrementAndGet();
            } catch (Exception e) {
                failureCount.incrementAndGet();
            }
        };

        executorService.submit(task);
        executorService.submit(task);

        latch.countDown();

        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);

        Inventory inventory = inventoryRepository.findBySku("PS5").orElseThrow();

        assertEquals(1, successCount.get());
        assertEquals(1, failureCount.get());
        assertTrue(inventory.getAvailableQuantity() >= 0);
        assertEquals(1, inventory.getAvailableQuantity());
        assertEquals(4, inventory.getReservedQuantity());
    }

    @Test
    void transactionRollback_whenReservationFails_shouldNotChangeInventory() {
        SupplyRequest supplyRequest = new SupplyRequest();
        supplyRequest.setSku("TV55");
        supplyRequest.setItemName("Samsung TV 55 Inch");
        supplyRequest.setQuantity(5);

        inventoryService.createSupply(supplyRequest);

        ReserveRequest reserveRequest = new ReserveRequest();
        reserveRequest.setSku("TV55");
        reserveRequest.setQuantity(10);

        assertThrows(
                RuntimeException.class,
                () -> inventoryService.reserveItem(reserveRequest)
        );

        Inventory inventory = inventoryRepository.findBySku("TV55").orElseThrow();

        assertEquals(5, inventory.getAvailableQuantity());
        assertEquals(0, inventory.getReservedQuantity());
        assertEquals(5, inventory.getTotalQuantity());
    }
}
package com.example.ecommerce.service;

import com.example.ecommerce.dto.CancelRequest;
import com.example.ecommerce.dto.ReserveRequest;
import com.example.ecommerce.dto.SupplyRequest;
import com.example.ecommerce.entity.Inventory;
import com.example.ecommerce.entity.Reservation;
import com.example.ecommerce.repository.InventoryRepository;
import com.example.ecommerce.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final ReservationRepository reservationRepository;

    @Transactional
    @CacheEvict(value = "inventory", key = "#supplyRequest.sku")
    public Inventory createSupply(SupplyRequest supplyRequest){
        Inventory inventory = inventoryRepository.findBySku(supplyRequest.getSku()).
                orElse(null);
        if(inventory!=null){
            inventory.setTotalQuantity(inventory.getTotalQuantity()+supplyRequest.getQuantity());
            inventory.setAvailableQuantity(inventory.getAvailableQuantity()+supplyRequest.getQuantity());
        }
        else{
            inventory=Inventory.builder().
                    sku(supplyRequest.getSku()).
                    itemName(supplyRequest.getItemName())
                    .totalQuantity(supplyRequest.getQuantity()).
                    availableQuantity(supplyRequest.getQuantity()).
                    reservedQuantity(0).
                    build();
        }
        return inventoryRepository.save(inventory);

    }
    @Transactional
    @CacheEvict(value = "inventory", key = "#reserveRequest.sku")
    public Reservation reserveItem(ReserveRequest reserveRequest){
        Inventory inventory=inventoryRepository.findBySku(reserveRequest.getSku()).
                orElseThrow(()->new RuntimeException("Item not found"));
        if(inventory.getAvailableQuantity()< reserveRequest.getQuantity()){
           throw new RuntimeException("Not Enough Stock Available");
        }
        inventory.setReservedQuantity(inventory.getReservedQuantity()+reserveRequest.getQuantity());
        inventory.setAvailableQuantity(inventory.getAvailableQuantity()-reserveRequest.getQuantity());
        inventoryRepository.save(inventory);
        Reservation reservation=Reservation.builder().
                sku(reserveRequest.getSku()).
                status("RESERVED").
                quantity(reserveRequest.getQuantity()).
                build();
      return reservationRepository.save(reservation);
    }
    @Transactional
    @CacheEvict(value = "inventory", allEntries = true)
    public Reservation cancelReservedItem(Long reservationId , CancelRequest cancelRequest){
        Reservation reservation=reservationRepository.findById(reservationId).
                orElseThrow(()->new RuntimeException("Reservation not Found"));
        if(reservation.getStatus().equals("CANCELLED")){
            throw new RuntimeException("Reservation already cancelled");
        }
        if(reservation.getQuantity()< cancelRequest.getQuantity()){
            throw new RuntimeException("Cancel quantity can not exceed reserved quantity");
        }
        Inventory inventory=inventoryRepository.findBySku(reservation.getSku()).
                orElseThrow(()->new RuntimeException("Item not found"));
        inventory.setAvailableQuantity(inventory.getAvailableQuantity()+cancelRequest.getQuantity());
        inventory.setReservedQuantity(inventory.getReservedQuantity()-cancelRequest.getQuantity());

        reservation.setQuantity(reservation.getQuantity()-cancelRequest.getQuantity());
        if(reservation.getQuantity()==0)reservation.setStatus("CANCELLED");

        inventoryRepository.save(inventory);
        return reservationRepository.save(reservation);
    }
    @Cacheable(value = "inventory", key = "#sku")
    public Inventory getAvailability(String sku){
        return inventoryRepository.findBySku(sku).
                orElseThrow(()->new RuntimeException("Item not found"));

    }

}

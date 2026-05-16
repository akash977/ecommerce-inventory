package com.example.ecommerce.controller;

import com.example.ecommerce.dto.CancelRequest;
import com.example.ecommerce.dto.ReserveRequest;
import com.example.ecommerce.dto.SupplyRequest;
import com.example.ecommerce.entity.Inventory;
import com.example.ecommerce.entity.Reservation;
import com.example.ecommerce.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/supply")
    @ResponseStatus(HttpStatus.CREATED)
    public Inventory createSupply(@Valid @RequestBody SupplyRequest supplyRequest){
        return inventoryService.createSupply(supplyRequest);
    }
    @PostMapping("/reserve")
    @ResponseStatus(HttpStatus.CREATED)
    public Reservation reserve(@Valid @RequestBody ReserveRequest reserveRequest){
        return inventoryService.reserveItem(reserveRequest);
    }
    @PostMapping("/reservation/{id}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public Reservation cancel(@PathVariable Long id,@Valid @RequestBody CancelRequest cancelRequest ){
        return inventoryService.cancelReservedItem(id,cancelRequest);
    }
    @GetMapping("/availability")
    @ResponseStatus(HttpStatus.OK)
    public Inventory getAvailability(@RequestParam String sku){
        return inventoryService.getAvailability(sku);
    }

}

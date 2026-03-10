package com.example.carreracaballos.api;

import com.example.carreracaballos.service.StoreService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/store")
public class StoreController {
    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @PostMapping("/purchase")
    public PurchasePointsResponse purchase(@Valid @RequestBody PurchasePointsRequest request) {
        return storeService.buyPoints(request.userId(), request.packageCount());
    }
}

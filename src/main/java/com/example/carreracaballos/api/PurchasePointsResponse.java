package com.example.carreracaballos.api;

public record PurchasePointsResponse(
        long purchasedPoints,
        long amountCop,
        long newBalance
) {
}

package com.example.carreracaballos.service;

import com.example.carreracaballos.api.PurchasePointsResponse;
import com.example.carreracaballos.model.PointPurchase;
import com.example.carreracaballos.model.UserAccount;
import com.example.carreracaballos.repository.PointPurchaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class StoreService {
    public static final long PACKAGE_POINTS = 1_000L;
    public static final long PACKAGE_PRICE_COP = 10_000L;

    private final UserAccountService userAccountService;
    private final PointPurchaseRepository pointPurchaseRepository;

    public StoreService(UserAccountService userAccountService, PointPurchaseRepository pointPurchaseRepository) {
        this.userAccountService = userAccountService;
        this.pointPurchaseRepository = pointPurchaseRepository;
    }

    @Transactional
    public PurchasePointsResponse buyPoints(Long userId, int packageCount) {
        UserAccount user = userAccountService.getRequiredUser(userId);
        long purchasedPoints = packageCount * PACKAGE_POINTS;
        long amountCop = packageCount * PACKAGE_PRICE_COP;
        user.setPointsBalance(user.getPointsBalance() + purchasedPoints);
        userAccountService.save(user);
        pointPurchaseRepository.save(new PointPurchase(user, purchasedPoints, amountCop, Instant.now()));
        return new PurchasePointsResponse(purchasedPoints, amountCop, user.getPointsBalance());
    }
}

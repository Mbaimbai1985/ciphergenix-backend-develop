package com.sphesihlemhlongo.paymentservicepractice.controller;

import com.sphesihlemhlongo.paymentservicepractice.dto.PaymentRequest;
import com.sphesihlemhlongo.paymentservicepractice.dto.StripeResponse;
import com.sphesihlemhlongo.paymentservicepractice.service.StripeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/subscribe/v1")
public class SubscriptionCheckoutController {
    private StripeService stripeService;

    public SubscriptionCheckoutController(StripeService stripeService) {
        this.stripeService = stripeService;
    }
    @PostMapping("checkout")
    public ResponseEntity<StripeResponse> checkoutSubscription(@RequestBody PaymentRequest paymentRequest){
        StripeResponse stripeResponse = stripeService.checkoutSubscription(paymentRequest);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(stripeResponse);
    }
}

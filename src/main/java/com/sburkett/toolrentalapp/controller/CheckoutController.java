package com.sburkett.toolrentalapp.controller;

import com.sburkett.toolrentalapp.dto.CheckoutRequest;
import com.sburkett.toolrentalapp.dto.RentalAgreementResponse;
import com.sburkett.toolrentalapp.services.CheckoutService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping(value = "/checkout")
    public RentalAgreementResponse checkoutController(@RequestBody CheckoutRequest request) {

        return checkoutService.processCheckout(request);
    }
}

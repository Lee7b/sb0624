package com.sburkett.toolrentalapp.controller;

import com.sburkett.toolrentalapp.dto.CheckoutRequest;
import com.sburkett.toolrentalapp.dto.CheckoutResponse;
import com.sburkett.toolrentalapp.services.CheckoutService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping(value = "/checkout")
    public CheckoutResponse checkoutController(@Valid @RequestBody CheckoutRequest request) {

        return checkoutService.processCheckout(request);
    }
}

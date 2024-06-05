package com.sburkett.toolrentalapp.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CheckoutRequest {
    private String toolCode;
    private int rentalDayCount;
    private String discountPercent;
    private String checkoutDate;
}

package com.sburkett.toolrentalapp.dto;

import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;

@Data
@Builder
public class CheckoutRequest {
    private String toolCode;
    @Min(value = 1, message = "rentalDayCount must be 1 or greater")
    private int rentalDayCount;
    @Range(min = 0, max = 100, message = "discountPercent must be in range of 0-100")
    private int discountPercent;
    private String checkoutDate;
}

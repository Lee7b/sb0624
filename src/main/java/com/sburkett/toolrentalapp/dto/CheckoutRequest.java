package com.sburkett.toolrentalapp.dto;

import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
@Builder
public class CheckoutRequest {
    @NotBlank
    private String toolCode;
    @Min(value = 1, message = "rentalDayCount must be 1 or greater")
    private int rentalDayCount;
    @Range(min = 0, max = 100, message = "discountPercent must be in range of 0-100")
    private int discountPercent;
    @NotBlank
    private String checkoutDate;
}

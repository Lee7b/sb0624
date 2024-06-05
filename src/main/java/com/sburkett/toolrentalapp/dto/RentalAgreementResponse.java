package com.sburkett.toolrentalapp.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class RentalAgreementResponse {
    private String toolCode; // Specified at checkout
    private String toolType; // From tool info
    private String toolBrand; // From tool info
    private String rentalDays; // Specified at checkout
    private String checkOutDate; // Specified at checkout
    private String dueDate; // Calculated from checkout date and rental days
    private String dailyRentalCharge; // Amount per day, specified by the tool type
    private String chargeDays; // Count of chargeable days, from day after checkout through and including due date, excluding
                                // no-charge days as specified by the tool type
    private String preDiscountCharge; // Calculated as charge days X daily charge, resulting total rounded half up to cents
    private String discountPercent; // Specified at checkout
    private String discountAmount; // Calculated from discount percent and pre-discount charge. Result rounded half up to cents
    private String finalCharge; // Calculated as pre discount charge-discount amount
}

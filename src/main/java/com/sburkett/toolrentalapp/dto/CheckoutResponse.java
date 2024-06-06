package com.sburkett.toolrentalapp.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckoutResponse {
    private String toolCode;
    private String toolType;
    private String toolBrand;
    private String rentalDays;
    private String checkOutDate;
    private String dueDate;
    private String dailyRentalCharge;
    private String chargeDays;
    private String preDiscountCharge;
    private String discountPercent;
    private String discountAmount;
    private String finalCharge;

    @Override
    public String toString() {
        return  "toolCode:" + toolCode + '\n' +
                "toolType:" + toolType + '\n' +
                "toolBrand:" + toolBrand + '\n' +
                "rentalDays:" + rentalDays + '\n' +
                "checkOutDate:" + checkOutDate + '\n' +
                "dueDate:" + dueDate + '\n' +
                "dailyRentalCharge:" + dailyRentalCharge + '\n' +
                "chargeDays:" + chargeDays + '\n' +
                "preDiscountCharge:" + preDiscountCharge + '\n' +
                "discountPercent:" + discountPercent + '\n' +
                "discountAmount:" + discountAmount + '\n' +
                "finalCharge:" + finalCharge;
    }
}

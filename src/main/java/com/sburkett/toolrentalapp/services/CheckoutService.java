package com.sburkett.toolrentalapp.services;

import com.sburkett.toolrentalapp.constants.Tool;
import com.sburkett.toolrentalapp.dto.CheckoutRequest;
import com.sburkett.toolrentalapp.dto.RentalAgreementResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
@Slf4j
public class CheckoutService {
    Map<String, Tool> toolInfoMap = new HashMap<>();
    Map<String, String> toolPriceMap = new HashMap<>();
    Set<LocalDate> holidays = new HashSet<>();
    private final String requestId = UUID.randomUUID().toString();

    public CheckoutService() {
        initialize();
    }

    private void initialize() {
        toolInfoMap.put("CHNS", new Tool("CHNS", "Chainsaw", "Stihl"));
        toolInfoMap.put("LADW", new Tool("LADW", "Ladder", "Werner"));
        toolInfoMap.put("JAKD", new Tool("JAKD", "Jackhammer", "DeWalt"));
        toolInfoMap.put("JAKR", new Tool("JAKR", "Jackhammer", "Ridgid"));

        toolPriceMap.put("Ladder", "1.99");
        toolPriceMap.put("Chainsaw", "1.49");
        toolPriceMap.put("Jackhammer", "2.99");
    }

    public RentalAgreementResponse processCheckout(CheckoutRequest request) {
        return buildCheckoutResponseAndLog(request);
    }

    private RentalAgreementResponse buildCheckoutResponseAndLog(CheckoutRequest checkoutRequest) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        LocalDate checkoutDate = LocalDate.parse(checkoutRequest.getCheckoutDate(), formatter);

        holidays.add(LocalDate.of(checkoutDate.getYear(), 7, 4)); // July 4
        holidays.add(getLaborDay(checkoutDate.getYear()));

        String toolType = toolInfoMap.get(checkoutRequest.getToolCode()).getToolType();
        LocalDate dueDate = checkoutDate.plusDays(checkoutRequest.getRentalDayCount());
        String chargeDays = getNumberOfChargeableDays(toolType, checkoutDate, dueDate);
        String preDiscountCharge = calculatePreDiscountCharge(toolType, chargeDays);
        String discountAmount = calculateDiscountAmount(checkoutRequest.getDiscountPercent(), preDiscountCharge);
        String finalCharge = calculateFinalCharge(preDiscountCharge, discountAmount);

        RentalAgreementResponse response = RentalAgreementResponse.builder()
                .toolCode(toolInfoMap.get(checkoutRequest.getToolCode()).getToolCode())
                .toolType(toolType)
                .toolBrand(toolInfoMap.get(checkoutRequest.getToolCode()).getToolBrand())
                .rentalDays(String.valueOf(checkoutRequest.getRentalDayCount()))
                .checkOutDate(checkoutDate.format(formatter))
                .dueDate(dueDate.format(formatter))
                .dailyRentalCharge(toolPriceMap.get(toolInfoMap.get(checkoutRequest.getToolCode()).getToolType()))
                .chargeDays(chargeDays)
                .preDiscountCharge(String.format("$%s", preDiscountCharge))
                .discountPercent(checkoutRequest.getDiscountPercent() + "%")
                .discountAmount(String.format("$%s", discountAmount))
                .finalCharge(String.format("$%s", finalCharge))
                .build();

        log.info("RESPONSE FOR ID: {} {}", requestId, response);

        return response;
    }

    private String getNumberOfChargeableDays(String toolType, LocalDate checkoutDate, LocalDate dueDate) {
        int chargeableDays = 0;

        // Iterate from day after checkout to the due date
        for (LocalDate date = checkoutDate.plusDays(1); !date.isAfter(dueDate); date = date.plusDays(1)) {
            boolean isWeekend = date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;

            if ("ladder".equalsIgnoreCase(toolType) && holidays.contains(date)) {
                continue;
            } else if ("chainsaw".equalsIgnoreCase(toolType) && (isWeekend)) {
                continue;
            } else if ("jackhammer".equalsIgnoreCase(toolType) && (isWeekend) || holidays.contains(date)) {
                continue;
            }

            chargeableDays++;
        }

        return String.valueOf(chargeableDays);
    }

    private String calculatePreDiscountCharge(String toolType, String chargeDays) {
       return new BigDecimal(chargeDays).multiply(new BigDecimal(toolPriceMap.get(toolType))).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String calculateDiscountAmount(String discountPercent, String preDiscountCharge) {
        return new BigDecimal(preDiscountCharge).multiply(new BigDecimal(discountPercent).divide(new BigDecimal(100))).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String calculateFinalCharge(String preDiscountCharge, String discountAmount) {
        return new BigDecimal(preDiscountCharge).subtract(new BigDecimal(discountAmount)).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private LocalDate getLaborDay(int year) {
        return LocalDate.of(year, Month.SEPTEMBER, 1).with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY));
    }
}

package com.sburkett.toolrentalapp.services;

import com.sburkett.toolrentalapp.config.ToolConfig;
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
    private final Set<LocalDate> holidays = new HashSet<>();
    private final String requestId = UUID.randomUUID().toString();
    private final ToolConfig toolConfig;

    public CheckoutService(ToolConfig toolConfig) {
        this.toolConfig = toolConfig;
    }

    public RentalAgreementResponse processCheckout(CheckoutRequest request) {
        return buildCheckoutResponseAndLog(request);
    }

    private RentalAgreementResponse buildCheckoutResponseAndLog(CheckoutRequest checkoutRequest) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        LocalDate checkoutDate = LocalDate.parse(checkoutRequest.getCheckoutDate(), formatter);

        holidays.add(LocalDate.of(checkoutDate.getYear(), 7, 4)); // July 4
        holidays.add(getLaborDay(checkoutDate.getYear()));

        String toolType = toolConfig.getToolInfoMap().get(checkoutRequest.getToolCode()).get("tooltype");
        LocalDate dueDate = checkoutDate.plusDays(checkoutRequest.getRentalDayCount());
        String chargeDays = getNumberOfChargeableDays(toolType, checkoutDate, dueDate);
        String dailyRentalCharge = toolConfig.getToolPriceMap().get(toolType.toLowerCase()).get("dailycharge");
        String preDiscountCharge = calculatePreDiscountCharge(dailyRentalCharge, chargeDays);
        String discountAmount = calculateDiscountAmount(checkoutRequest.getDiscountPercent(), preDiscountCharge);
        String finalCharge = calculateFinalCharge(preDiscountCharge, discountAmount);

        RentalAgreementResponse response = RentalAgreementResponse.builder()
                .toolCode(checkoutRequest.getToolCode())
                .toolType(toolType)
                .toolBrand(toolConfig.getToolInfoMap().get(checkoutRequest.getToolCode()).get("brand"))
                .rentalDays(String.valueOf(checkoutRequest.getRentalDayCount()))
                .checkOutDate(checkoutDate.format(formatter))
                .dueDate(dueDate.format(formatter))
                .dailyRentalCharge(dailyRentalCharge)
                .chargeDays(chargeDays)
                .preDiscountCharge(String.format("$%s", preDiscountCharge))
                .discountPercent(checkoutRequest.getDiscountPercent() + "%")
                .discountAmount(String.format("$%s", discountAmount))
                .finalCharge(String.format("$%s", finalCharge))
                .build();

        log.info("RESPONSE: {} {}", requestId, response);

        return response;
    }

    private String getNumberOfChargeableDays(String toolType, LocalDate checkoutDate, LocalDate dueDate) {
        int chargeableDays = 0;

        // Iterate from day after checkout to the due date
        for (LocalDate date = checkoutDate.plusDays(1); !date.isAfter(dueDate); date = date.plusDays(1)) {
            boolean isWeekend = date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
            boolean isHoliday = holidays.contains(date);
            boolean weekdayCharge = Boolean.parseBoolean(toolConfig.getToolPriceMap().get(toolType.toLowerCase()).get("weekdaycharge"));
            boolean weekendCharge = Boolean.parseBoolean(toolConfig.getToolPriceMap().get(toolType.toLowerCase()).get("weekendcharge"));
            boolean holidayCharge = Boolean.parseBoolean(toolConfig.getToolPriceMap().get(toolType.toLowerCase()).get("holidaycharge"));

            // Don't increment the daily charge if no charge is applicable to that day
            if (isHoliday && !holidayCharge) continue;
            else if (!isWeekend && !weekdayCharge) continue; // checks if weekday
            else if (isWeekend && !weekendCharge) continue;

            chargeableDays++;
        }

        return String.valueOf(chargeableDays);
    }

    private String calculatePreDiscountCharge(String dailyRentalCharge, String chargeDays) {
        return new BigDecimal(dailyRentalCharge).multiply(new BigDecimal(chargeDays)).setScale(2, RoundingMode.HALF_UP).toPlainString();
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

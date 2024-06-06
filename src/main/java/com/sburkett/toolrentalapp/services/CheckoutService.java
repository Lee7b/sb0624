package com.sburkett.toolrentalapp.services;

import com.sburkett.toolrentalapp.db.entity.ToolEntity;
import com.sburkett.toolrentalapp.db.repository.ToolRepository;
import com.sburkett.toolrentalapp.dto.CheckoutRequest;
import com.sburkett.toolrentalapp.dto.CheckoutResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
@Slf4j
public class CheckoutService {
    private final String requestId = UUID.randomUUID().toString();
    private final Set<LocalDate> holidays = new HashSet<>();
    private final ToolRepository toolRepository;

    public CheckoutService(ToolRepository toolRepository) {
        this.toolRepository = toolRepository;
    }

    public CheckoutResponse processCheckout(CheckoutRequest request) {
        return buildCheckoutResponseAndLog(request);
    }

    private CheckoutResponse buildCheckoutResponseAndLog(CheckoutRequest checkoutRequest) {
        ToolEntity toolEntity = toolRepository.findByToolCode(checkoutRequest.getToolCode());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy");
        LocalDate checkoutDate = LocalDate.parse(checkoutRequest.getCheckoutDate(), formatter);

        holidays.add(getIndependenceDay(checkoutDate.getYear()));
        holidays.add(getLaborDay(checkoutDate.getYear()));

        LocalDate dueDate = checkoutDate.plusDays(checkoutRequest.getRentalDayCount());
        int chargeDays = getNumberOfChargeableDays(toolEntity, checkoutDate, dueDate);
        String dailyRentalCharge = toolEntity.getDailyCharge();
        BigDecimal preDiscountCharge = calculatePreDiscountCharge(dailyRentalCharge, chargeDays);
        String discountAmount = calculateDiscountAmount(checkoutRequest.getDiscountPercent(), preDiscountCharge);

        CheckoutResponse response = CheckoutResponse.builder()
                .toolCode(checkoutRequest.getToolCode())
                .toolType(toolEntity.getToolType())
                .toolBrand(toolEntity.getBrand())
                .rentalDays(String.valueOf(checkoutRequest.getRentalDayCount()))
                .checkOutDate(checkoutDate.format(formatter))
                .dueDate(dueDate.format(formatter))
                .dailyRentalCharge(NumberFormat.getCurrencyInstance().format(new BigDecimal(dailyRentalCharge)))
                .chargeDays(String.valueOf(chargeDays))
                .preDiscountCharge(NumberFormat.getCurrencyInstance().format(preDiscountCharge))
                .discountPercent(checkoutRequest.getDiscountPercent() + "%")
                .discountAmount(String.format("$%s", discountAmount))
                .finalCharge(NumberFormat.getCurrencyInstance().format(calculateFinalCharge(preDiscountCharge, discountAmount)))
                .build();

        log.info("RESPONSE: {} \n{}", requestId, response.toString());

        return response;
    }

    private int getNumberOfChargeableDays(ToolEntity toolEntity, LocalDate checkoutDate, LocalDate dueDate) {
        int chargeableDays = 0;

        // Iterate from day after checkout to the due date
        for (LocalDate date = checkoutDate.plusDays(1); !date.isAfter(dueDate); date = date.plusDays(1)) {
            boolean isWeekend = date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
            boolean isHoliday = holidays.contains(date);
            boolean weekdayCharge = toolEntity.isWeekdayCharge();
            boolean weekendCharge = toolEntity.isWeekendCharge();
            boolean holidayCharge = toolEntity.isHolidayCharge();

            // Don't increment the daily charge if no charge is applicable to that day
            if (isHoliday && !holidayCharge) continue;
            else if (!isWeekend && !weekdayCharge) continue; // checks if weekday
            else if (isWeekend && !weekendCharge) continue;

            chargeableDays++;
        }

        return chargeableDays;
    }

    private BigDecimal calculatePreDiscountCharge(String dailyRentalCharge, int chargeDays) {
        return new BigDecimal(dailyRentalCharge).multiply(BigDecimal.valueOf(chargeDays)).setScale(2, RoundingMode.HALF_UP);
    }

    private String calculateDiscountAmount(int discountPercent, BigDecimal preDiscountCharge) {
        return preDiscountCharge.multiply(BigDecimal.valueOf(discountPercent)).divide(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private BigDecimal calculateFinalCharge(BigDecimal preDiscountCharge, String discountAmount) {
        return preDiscountCharge.subtract(new BigDecimal(discountAmount)).setScale(2, RoundingMode.HALF_UP);
    }

    private LocalDate getLaborDay(int year) {
        return LocalDate.of(year, Month.SEPTEMBER, 1).with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY));
    }

    private LocalDate getIndependenceDay(int year) {
        LocalDate july4th = LocalDate.of(year, 7, 4);
        DayOfWeek dayOfWeek = july4th.getDayOfWeek();

        if (dayOfWeek.equals(DayOfWeek.SATURDAY)) {
            return july4th.minusDays(1);
        } else if (dayOfWeek.equals(DayOfWeek.SUNDAY)) {
            return july4th.plusDays(1);
        } else {
            return july4th;
        }
    }
}

package com.sburkett.toolrentalapp.services;

import com.sburkett.toolrentalapp.db.entity.ToolPricesDao;
import com.sburkett.toolrentalapp.db.entity.ToolsDao;
import com.sburkett.toolrentalapp.db.repository.ToolPricesRepository;
import com.sburkett.toolrentalapp.db.repository.ToolsRepository;
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
    private final ToolPricesRepository toolPricesRepository;
    private final ToolsRepository toolsRepository;

    public CheckoutService(ToolPricesRepository toolPricesRepository, ToolsRepository toolsRepository) {
        this.toolPricesRepository = toolPricesRepository;
        this.toolsRepository = toolsRepository;
    }

    public CheckoutResponse processCheckout(CheckoutRequest request) {
        return buildCheckoutResponseAndLog(request);
    }

    private CheckoutResponse buildCheckoutResponseAndLog(CheckoutRequest checkoutRequest) {
        ToolsDao toolsDao = toolsRepository.findByToolCode(checkoutRequest.getToolCode());
        ToolPricesDao toolPricesDao = toolPricesRepository.findByToolType(toolsDao.getToolType());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy");
        LocalDate checkoutDate = LocalDate.parse(checkoutRequest.getCheckoutDate(), formatter);

        holidays.add(getObservedIndependenceDay(checkoutDate.getYear()));
        holidays.add(getLaborDay(checkoutDate.getYear()));

        LocalDate dueDate = checkoutDate.plusDays(checkoutRequest.getRentalDayCount());
        int chargeDays = getNumberOfChargeableDays(toolPricesDao, checkoutDate, dueDate);
        String dailyRentalCharge = toolPricesDao.getDailyCharge();
        BigDecimal preDiscountCharge = calculatePreDiscountCharge(dailyRentalCharge, chargeDays);
        BigDecimal discountAmount = calculateDiscountAmount(checkoutRequest.getDiscountPercent(), preDiscountCharge);

        CheckoutResponse response = CheckoutResponse.builder()
                .toolCode(checkoutRequest.getToolCode())
                .toolType(toolsDao.getToolType())
                .toolBrand(toolsDao.getBrand())
                .rentalDays(String.valueOf(checkoutRequest.getRentalDayCount()))
                .checkOutDate(checkoutRequest.getCheckoutDate())
                .dueDate(dueDate.format(formatter))
                .dailyRentalCharge(NumberFormat.getCurrencyInstance().format(new BigDecimal(dailyRentalCharge)))
                .chargeDays(String.valueOf(chargeDays))
                .preDiscountCharge(NumberFormat.getCurrencyInstance().format(preDiscountCharge))
                .discountPercent(checkoutRequest.getDiscountPercent() + "%")
                .discountAmount(NumberFormat.getCurrencyInstance().format(discountAmount))
                .finalCharge(NumberFormat.getCurrencyInstance().format(calculateFinalCharge(preDiscountCharge, discountAmount)))
                .build();

        log.info("RESPONSE: {} \n{}", requestId, response.toString());

        return response;
    }

    private int getNumberOfChargeableDays(ToolPricesDao toolPricesDao, LocalDate checkoutDate, LocalDate dueDate) {
        int chargeableDays = 0;

        // Iterate from day after checkout to the due date
        for (LocalDate date = checkoutDate.plusDays(1); !date.isAfter(dueDate); date = date.plusDays(1)) {
            boolean isWeekend = date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
            boolean isHoliday = holidays.contains(date);
            boolean weekdayCharge = toolPricesDao.isWeekdayCharge();
            boolean weekendCharge = toolPricesDao.isWeekendCharge();
            boolean holidayCharge = toolPricesDao.isHolidayCharge();

            // Don't increment the daily charge if no charge is applicable to that day
            if (isHoliday && !holidayCharge) continue;
            else if (!isWeekend && !weekdayCharge) continue; // checks if weekday
            else if (isWeekend && !weekendCharge) continue;

            chargeableDays++;
        }

        return chargeableDays;
    }

    private BigDecimal calculatePreDiscountCharge(String dailyRentalCharge, int chargeDays) {
        return new BigDecimal(dailyRentalCharge)
                .multiply(BigDecimal.valueOf(chargeDays))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateDiscountAmount(int discountPercent, BigDecimal preDiscountCharge) {
        return preDiscountCharge
                .multiply(BigDecimal.valueOf(discountPercent))
                .divide(new BigDecimal(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateFinalCharge(BigDecimal preDiscountCharge, BigDecimal discountAmount) {
        return preDiscountCharge
                .subtract(discountAmount)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private LocalDate getLaborDay(int year) {
        return LocalDate.of(year, Month.SEPTEMBER, 1).with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY));
    }

    private LocalDate getObservedIndependenceDay(int year) {
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

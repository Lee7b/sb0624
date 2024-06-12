package com.sburkett.toolrentalapp.db.entity;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "tool_prices")
public class ToolPricesDao {
    @Id
    @Column(name = "tool_type")
    private String toolType;

    @Column(name = "daily_charge")
    private String dailyCharge;

    @Column(name = "has_weekday_charge")
    private boolean weekdayCharge;

    @Column(name = "has_weekend_charge")
    private boolean weekendCharge;

    @Column(name = "has_holiday_charge")
    private boolean holidayCharge;
}

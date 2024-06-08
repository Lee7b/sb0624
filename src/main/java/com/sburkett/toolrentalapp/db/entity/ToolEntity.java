package com.sburkett.toolrentalapp.db.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;


@Getter
@Setter
@Entity
@Table(name = "TOOL_PRICES")
@SecondaryTable(name = "TOOLS")
public class ToolEntity {
    @Id
    @Column(name = "TOOL_TYPE")
    private String toolType;

    @Column(name = "TOOL_CODE", table = "TOOLS")
    private String toolCode;

    @Column(name = "BRAND", table = "TOOLS")
    private String brand;

    @Column(name = "DAILY_CHARGE")
    private String dailyCharge;

    @Column(name = "WEEKDAY_CHARGE")
    private boolean weekdayCharge;

    @Column(name = "WEEKEND_CHARGE")
    private boolean weekendCharge;

    @Column(name = "HOLIDAY_CHARGE")
    private boolean holidayCharge;
}

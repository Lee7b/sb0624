package com.sburkett.toolrentalapp.db.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;


@Getter
@Setter
@Entity
@Table(name = "TOOLS")
@SecondaryTable(name = "TOOL_PRICES", pkJoinColumns = {@PrimaryKeyJoinColumn(referencedColumnName = "TOOL_TYPE")})
public class ToolEntity {
    @Id
    @Column(name = "TOOL_TYPE")
    private String toolType;

    @Column(name = "TOOL_CODE")
    private String toolCode;

    @Column(name = "BRAND")
    private String brand;

    @Column(name = "DAILY_CHARGE", table = "TOOL_PRICES")
    private String dailyCharge;

    @Column(name = "WEEKDAY_CHARGE", table = "TOOL_PRICES")
    private boolean weekdayCharge;

    @Column(name = "WEEKEND_CHARGE", table = "TOOL_PRICES")
    private boolean weekendCharge;

    @Column(name = "HOLIDAY_CHARGE", table = "TOOL_PRICES")
    private boolean holidayCharge;
}

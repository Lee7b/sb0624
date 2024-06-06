create table TOOLS (
    TOOL_TYPE varchar(255) NOT NULL,
    TOOL_CODE varchar(255) NOT NULL,
    BRAND varchar(255) NOT NULL
);

create table TOOL_PRICES (
    TOOL_TYPE varchar(255) NOT NULL,
    DAILY_CHARGE varchar(255) NOT NULL,
    WEEKDAY_CHARGE boolean,
    WEEKEND_CHARGE boolean,
    HOLIDAY_CHARGE boolean
);
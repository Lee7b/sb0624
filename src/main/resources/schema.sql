CREATE TABLE tool_prices (
    tool_type varchar(255) NOT NULL PRIMARY KEY,
    daily_charge decimal(19,2) NOT NULL,
    has_weekday_charge boolean,
    has_weekend_charge boolean,
    has_holiday_charge boolean
);

CREATE TABLE tools (
    tool_type varchar(255) NOT NULL,
    tool_code varchar(255) NOT NULL PRIMARY KEY,
    brand varchar(255) NOT NULL,
    CONSTRAINT FK_TOOL_TYPE FOREIGN KEY (TOOL_TYPE) REFERENCES TOOL_PRICES(TOOL_TYPE)
);


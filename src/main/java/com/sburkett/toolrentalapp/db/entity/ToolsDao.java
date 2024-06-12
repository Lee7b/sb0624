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
@Table(name = "tools")
public class ToolsDao {
    @Id
    @Column(name = "tool_code")
    private String toolCode;

    @Column(name = "tool_type")
    private String toolType;

    @Column(name = "brand")
    private String brand;
}

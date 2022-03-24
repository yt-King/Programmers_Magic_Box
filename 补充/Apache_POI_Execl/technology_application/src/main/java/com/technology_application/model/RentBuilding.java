package com.technology_application.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 租用建筑物
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RentBuilding {
    private String id;

    private String buildingName;

    private Double buildingAreas;

    private Double researchAreas;

    private String use;

    private Date rentTime;

    private BigDecimal rent;

    private Integer rentPeriod;

    private BigDecimal monthlyRent;

    private Integer useTime;

    private byte[] operationName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

}
package com.technology_application.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 自有建筑物
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SelfOwnedBuilding {
    private String id;

    private String buildingName;

    private Double buildingAreas;

    private Double researchAreas;

    private String use;

    private Date buyTime;

    private BigDecimal defaultValue;

    private Integer salvage;

    private Integer depreciationPeriod;

    private BigDecimal depreciationMoney;

    private Integer useTime;

    private String operationName;

}
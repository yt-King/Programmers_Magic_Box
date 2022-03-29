package com.technology_application.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 自有仪器设备
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SelfOwnedEquipment {
    private String id;

    private String equipmentName;

    private String equipmentType;

    private Integer power;

    private String use;

    private Date buyTime;

    private BigDecimal defaultValue;

    private Integer salvage;

    private Integer depreciationPeriod;

    private BigDecimal depreciationMoney;

    private Integer useTime;

    private String operationName;

}
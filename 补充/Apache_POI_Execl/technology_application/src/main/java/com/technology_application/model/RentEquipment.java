package com.technology_application.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 租用仪器设备
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RentEquipment {
    private String id;

    private String equipmentName;

    private String equipmentType;

    private Integer power;

    private String use;

    private Date rentTime;

    private BigDecimal rent;

    private Integer rentPeriod;

    private BigDecimal monthlyRent;

    private Integer useTime;

    private String operationName;

}
package com.technology_application.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 长期待摊
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LongTimeExpense {
    private String id;

    private String expenseName;

    private String use;

    private Date occurredTime;

    private BigDecimal defaultValue;

    private Integer depreciationPeriod;

    private BigDecimal depreciationMoney;

    private Integer useTime;

    private String operationName;


}
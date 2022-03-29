package com.technology_application.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.Date;
/**
 * 无形资产
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IntangibleAssets {
    @NotBlank(message="id不得为空")
    private String id;

    @NotBlank(message="无形资产名称不得为空")
    private String assetsName;

    @NotBlank(message="无形资产类型不得为空")
    private Double assetsType;

    @NotBlank(message="无形资产用途不得为空")
    private String use;

    @Future
    @NotBlank(message="购入时间不得为空")
    private Date buyTime;

    @NotBlank(message="摊销原值不得为空")
    private BigDecimal defaultValue;

    @NotBlank(message="摊销限制不得为空")
    private Integer depreciationPeriod;

    @NotBlank(message="每月摊销额不得为空")
    private BigDecimal depreciationMoney;

    @NotBlank(message="默认工时不得为空")
    private Integer useTime;

    @NotBlank(message="操作人不得为空")
    private String operationName;

}
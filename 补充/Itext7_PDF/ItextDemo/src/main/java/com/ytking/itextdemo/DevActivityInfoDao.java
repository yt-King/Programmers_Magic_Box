package com.ytking.itextdemo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 应涛
 * @date 2022/2/18
 * @function：
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DevActivityInfoDao {
    /**
     * 活动编号
     */
    String activityId;
    /**
     * 研发活动名称
     */
    String activityName;
    /**
     * 起止时间
     */
    String activityTime;
    /**
     * 技术领域
     */
    String techDomain;
    /**
     * 技术来源
     */
    String techFrom;
    /**
     * 知识产权（编号）
     */
    String intellectualId;
    /**
     * 研发经费总预算（万元）
     */
    String budgetAll;
    /**
     * 研发经费近三年总支出（万元）
     */
    String expenditureThree;
    /**
     * 第一年支出
     */
    String yearOne;
    /**
     * 第二年支出
     */
    String yearTwo;
    /**
     * 第三年支出
     */
    String yearThree;
    /**
     * 目的及组织实施方式(限400字)
     */
    String organizations;
    /**
     * 核心技术及创新点(限400字)
     */
    String innovations;
    /**
     * 取得的阶段性成果(限400字)
     */
    String results;


}

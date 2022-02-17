package com.ytking.itextdemo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 应涛
 * @date 2022/2/15
 * @function：
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnterpriceRegDao {
    /**
     * 企业名称
     */
    String enterpriceName;
    /**
     * 注册时间
     */
    String regTime;
    /**
     * 注册类型
     */
    String regType;
    /**
     * 外资来源地
     */
    String resourceFrom;
    /**
     * 注册资金
     */
    String regCapital;
    /**
     * 所属行业
     */
    String belongsTo;
    /**
     * 企业规模
     */
    String enterpriceScale;
    /**
     * 行政区域
     */
    String adminRegion;
    /**
     * 组织机构代码
     */
    String organizationCode;
    /**
     * 税务登记号
     */
    String taxCode;
    /**
     * 企业所得税主管税务机关
     * state / local
     */
    String directorOfTax;
    /**
     * 企业所得税征收方式
     * check / appraise
     */
    String collectMethod;
    /**
     * 通讯地址
     */
    String address;
    /**
     * 邮政编码
     */
    String postCode;
    /**
     * 企业法定代表人姓名
     */
    String enterpricerName;
    /**
     * 企业法定代表人电话
     */
    String enterpricerTel;
    /**
     * 企业法定代表人手机
     */
    String enterpricerMobile;
    /**
     * 企业法定代表人传真
     */
    String enterpricerFax;
    /**
     * 企业法定代表人身份证/护照号
     */
    String enterpricerIdCard;
    /**
     * 企业法定代表人E-mail
     */
    String enterpricerMail;
    /**
     * 联系人姓名
     */
    String contacterName;
    /**
     * 联系人电话
     */
    String contacterTel;
    /**
     * 联系人手机
     */
    String contacterMobile;
    /**
     * 联系人传真
     */
    String contacterFax;
    /**
     * 联系人E-mail
     */
    String contacterMail;
    /**
     * 企业是否上市
     * y / n
     */
    String isListed;
    /**
     * 上市时间
     */
    String listedTime;
    /**
     * 股票代码
     */
    String stockCode;
    /**
     * 上市类型
     */
    String listedType;
    /**
     * 是否属于国家级高新区内企业
     * y / n
     */
    String isHighZones;
    /**
     * 高新区名称
     */
    String highZonesName;

    public void setEnterpriceName(String enterpriceName) {
        this.enterpriceName = enterpriceName;
    }

    public void setRegTime(String regTime) {
        this.regTime = regTime;
    }

    public void setRegType(String regType) {
        this.regType = regType;
    }

    public void setResourceFrom(String resourceFrom) {
        this.resourceFrom = resourceFrom;
    }

    public void setRegCapital(String regCapital) {
        this.regCapital = regCapital;
    }

    public void setBelongsTo(String belongsTo) {
        this.belongsTo = belongsTo;
    }

    public void setEnterpriceScale(String enterpriceScale) {
        this.enterpriceScale = enterpriceScale;
    }

    public void setAdminRegion(String adminRegion) {
        this.adminRegion = adminRegion;
    }

    public void setOrganizationCode(String organizationCode) {
        this.organizationCode = organizationCode;
    }

    public void setTaxCode(String taxCode) {
        this.taxCode = taxCode;
    }

    public void setDirectorOfTax(String directorOfTax) {
        this.directorOfTax = directorOfTax;
    }

    public void setCollectMethod(String collectMethod) {
        this.collectMethod = collectMethod;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public void setEnterpricerName(String enterpricerName) {
        this.enterpricerName = enterpricerName;
    }

    public void setEnterpricerTel(String enterpricerTel) {
        this.enterpricerTel = enterpricerTel;
    }

    public void setEnterpricerMobile(String enterpricerMobile) {
        this.enterpricerMobile = enterpricerMobile;
    }

    public void setEnterpricerFax(String enterpricerFax) {
        this.enterpricerFax = enterpricerFax;
    }

    public void setEnterpricerIdCard(String enterpricerIdCard) {
        this.enterpricerIdCard = enterpricerIdCard;
    }

    public void setEnterpricerMail(String enterpricerMail) {
        this.enterpricerMail = enterpricerMail;
    }

    public void setContacterName(String contacterName) {
        this.contacterName = contacterName;
    }

    public void setContacterTel(String contacterTel) {
        this.contacterTel = contacterTel;
    }

    public void setContacterMobile(String contacterMobile) {
        this.contacterMobile = contacterMobile;
    }

    public void setContacterFax(String contacterFax) {
        this.contacterFax = contacterFax;
    }

    public void setContacterMail(String contacterMail) {
        this.contacterMail = contacterMail;
    }

    public void setIsListed(String isListed) {
        this.isListed = isListed;
    }

    public void setListedTime(String listedTime) {
        this.listedTime = listedTime;
    }

    public void setStockCode(String stockCode) {
        this.stockCode = stockCode;
    }

    public void setListedType(String listedType) {
        this.listedType = listedType;
    }

    public void setIsHighZones(String isHighZones) {
        this.isHighZones = isHighZones;
    }

    public void setHighZonesName(String highZonesName) {
        this.highZonesName = highZonesName;
    }

    public String getEnterpriceName() {
        return enterpriceName;
    }

    public String getRegTime() {
        return regTime;
    }

    public String getRegType() {
        return regType;
    }

    public String getResourceFrom() {
        return resourceFrom;
    }

    public String getRegCapital() {
        return regCapital;
    }

    public String getBelongsTo() {
        return belongsTo;
    }

    public String getEnterpriceScale() {
        return enterpriceScale;
    }

    public String getAdminRegion() {
        return adminRegion;
    }

    public String getOrganizationCode() {
        return organizationCode;
    }

    public String getTaxCode() {
        return taxCode;
    }

    public String getDirectorOfTax() {
        return directorOfTax;
    }

    public String getCollectMethod() {
        return collectMethod;
    }

    public String getAddress() {
        return address;
    }

    public String getPostCode() {
        return postCode;
    }

    public String getEnterpricerName() {
        return enterpricerName;
    }

    public String getEnterpricerTel() {
        return enterpricerTel;
    }

    public String getEnterpricerMobile() {
        return enterpricerMobile;
    }

    public String getEnterpricerFax() {
        return enterpricerFax;
    }

    public String getEnterpricerIdCard() {
        return enterpricerIdCard;
    }

    public String getEnterpricerMail() {
        return enterpricerMail;
    }

    public String getContacterName() {
        return contacterName;
    }

    public String getContacterTel() {
        return contacterTel;
    }

    public String getContacterMobile() {
        return contacterMobile;
    }

    public String getContacterFax() {
        return contacterFax;
    }

    public String getContacterMail() {
        return contacterMail;
    }

    public String getIsListed() {
        return isListed;
    }

    public String getListedTime() {
        return listedTime;
    }

    public String getStockCode() {
        return stockCode;
    }

    public String getListedType() {
        return listedType;
    }

    public String getIsHighZones() {
        return isHighZones;
    }

    public String getHighZonesName() {
        return highZonesName;
    }
}

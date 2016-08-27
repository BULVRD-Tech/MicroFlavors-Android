package com.zeen.reststopper.models.ip;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by davidhodge on 11/28/15.
 */
public class IpResponse {
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("country")
    @Expose
    private String country;
    @SerializedName("countryCode")
    @Expose
    private String countryCode;
    @SerializedName("region")
    @Expose
    private String region;
    @SerializedName("regionName")
    @Expose
    private String regionName;
    @SerializedName("city")
    @Expose
    private String city;
    @SerializedName("zip")
    @Expose
    private String zip;
    @SerializedName("lat")
    @Expose
    private String lat;
    @SerializedName("lon")
    @Expose
    private String lon;
    @SerializedName("timezone")
    @Expose
    private String timezone;
    @SerializedName("isp")
    @Expose
    private String isp;
    @SerializedName("org")
    @Expose
    private String org;
    @SerializedName("as")
    @Expose
    private String as;
    @SerializedName("query")
    @Expose
    private String query;

    /**
     *
     * @return
     * The status
     */
    public String getStatus() {
        return status;
    }

    /**
     *
     * @param status
     * The status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     *
     * @return
     * The country
     */
    public String getCountry() {
        return country;
    }

    /**
     *
     * @param country
     * The country
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     *
     * @return
     * The countryCode
     */
    public String getCountryCode() {
        return countryCode;
    }

    /**
     *
     * @param countryCode
     * The countryCode
     */
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    /**
     *
     * @return
     * The region
     */
    public String getRegion() {
        return region;
    }

    /**
     *
     * @param region
     * The region
     */
    public void setRegion(String region) {
        this.region = region;
    }

    /**
     *
     * @return
     * The regionName
     */
    public String getRegionName() {
        return regionName;
    }

    /**
     *
     * @param regionName
     * The regionName
     */
    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    /**
     *
     * @return
     * The city
     */
    public String getCity() {
        return city;
    }

    /**
     *
     * @param city
     * The city
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     *
     * @return
     * The zip
     */
    public String getZip() {
        return zip;
    }

    /**
     *
     * @param zip
     * The zip
     */
    public void setZip(String zip) {
        this.zip = zip;
    }

    /**
     *
     * @return
     * The lat
     */
    public String getLat() {
        return lat;
    }

    /**
     *
     * @param lat
     * The lat
     */
    public void setLat(String lat) {
        this.lat = lat;
    }

    /**
     *
     * @return
     * The lon
     */
    public String getLon() {
        return lon;
    }

    /**
     *
     * @param lon
     * The lon
     */
    public void setLon(String lon) {
        this.lon = lon;
    }

    /**
     *
     * @return
     * The timezone
     */
    public String getTimezone() {
        return timezone;
    }

    /**
     *
     * @param timezone
     * The timezone
     */
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    /**
     *
     * @return
     * The isp
     */
    public String getIsp() {
        return isp;
    }

    /**
     *
     * @param isp
     * The isp
     */
    public void setIsp(String isp) {
        this.isp = isp;
    }

    /**
     *
     * @return
     * The org
     */
    public String getOrg() {
        return org;
    }

    /**
     *
     * @param org
     * The org
     */
    public void setOrg(String org) {
        this.org = org;
    }

    /**
     *
     * @return
     * The as
     */
    public String getAs() {
        return as;
    }

    /**
     *
     * @param as
     * The as
     */
    public void setAs(String as) {
        this.as = as;
    }

    /**
     *
     * @return
     * The query
     */
    public String getQuery() {
        return query;
    }

    /**
     *
     * @param query
     * The query
     */
    public void setQuery(String query) {
        this.query = query;
    }
}

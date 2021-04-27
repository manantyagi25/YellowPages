package com.aventum.yellowpages;

public class pInfo {
    String name;
    String unit;
    String dept;

    public pInfo() {
    }

    public pInfo(String name, String unit, String dept) {
        this.name = name;
        this.unit = unit;
        this.dept = dept;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getDept() {
        return dept;
    }

    public void setDept(String dept) {
        this.dept = dept;
    }

    @Override
    public String toString() {
        return "pInfo{" +
                "name='" + name + '\'' +
                ", unit='" + unit + '\'' +
                ", dept='" + dept + '\'' +
                '}';
    }
}

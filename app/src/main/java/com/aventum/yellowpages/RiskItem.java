package com.aventum.yellowpages;

public class RiskItem {
    String concern;
    String loc;

    public RiskItem(){

    }

    public RiskItem(String concern, String loc) {
        this.concern = concern;
        this.loc = loc;
    }

    /*public RiskItem(String concern, String loc) {
        this.concern = concern;
        this.loc = loc;
    }*/

    public String getConcern() {
        return concern;
    }

    public void setConcern(String concern) {
        this.concern = concern;
    }

    public String getLoc() {
        return loc;
    }

    public void setLoc(String loc) {
        this.loc = loc;
    }


    @Override
    public String toString() {
        return "RiskItem{" +
                "concern='" + concern + '\'' +
                ", loc='" + loc + '\'' +
                '}';
    }
}

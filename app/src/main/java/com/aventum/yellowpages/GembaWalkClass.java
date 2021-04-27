package com.aventum.yellowpages;

import com.google.firebase.database.ServerValue;

public class GembaWalkClass {
    String type;
    String repBy;
    String repByDept;
    String teamName;
    String concern;
    String loc;
    String deptTo;
    String cPhotoURL;
    String tPhotoURL;
    String priority;
    Object repTime = ServerValue.TIMESTAMP;

    public GembaWalkClass(){

    }

    public GembaWalkClass(String type, String repBy, String repByDept, String teamName, String concern, String loc, String deptTo, String cPhotoURL, String tPhotoURL, String priority) {
        this.type = type;
        this.repBy = repBy;
        this.repByDept = repByDept;
        this.teamName = teamName;
        this.concern = concern;
        this.loc = loc;
        this.deptTo = deptTo;
        this.cPhotoURL = cPhotoURL;
        this.tPhotoURL = tPhotoURL;
        this.priority = priority;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRepBy() {
        return repBy;
    }

    public void setRepBy(String repBy) {
        this.repBy = repBy;
    }

    public String getRepByDept() {
        return repByDept;
    }

    public void setRepByDept(String repByDept) {
        this.repByDept = repByDept;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

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

    public String getDeptTo() {
        return deptTo;
    }

    public void setDeptTo(String deptTo) {
        this.deptTo = deptTo;
    }

    public String getcPhotoURL() {
        return cPhotoURL;
    }

    public void setcPhotoURL(String cPhotoURL) {
        this.cPhotoURL = cPhotoURL;
    }

    public String gettPhotoURL() {
        return tPhotoURL;
    }

    public void settPhotoURL(String tPhotoURL) {
        this.tPhotoURL = tPhotoURL;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Object getRepTime() {
        return repTime;
    }

    public void setRepTime(Object repTime) {
        this.repTime = repTime;
    }

    @Override
    public String toString() {
        return "GembaWalkConcern{" +
                "type='" + type + '\'' +
                ", repBy='" + repBy + '\'' +
                ", repByDept='" + repByDept + '\'' +
                ", teamName='" + teamName + '\'' +
                ", concern='" + concern + '\'' +
                ", loc='" + loc + '\'' +
                ", deptTo='" + deptTo + '\'' +
                ", cPhotoURL='" + cPhotoURL + '\'' +
                ", tPhotoURL='" + tPhotoURL + '\'' +
                ", priority='" + priority + '\'' +
                ", repTime=" + repTime +
                '}';
    }
}

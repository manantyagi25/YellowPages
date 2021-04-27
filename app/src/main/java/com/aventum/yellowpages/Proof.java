package com.aventum.yellowpages;

import com.google.firebase.database.ServerValue;

public class Proof {
    String closedBy;
    String closeByDept;
    String cause;
    String immAction;
    String permAction;
    String pPhotoURL;
    Object repTime = ServerValue.TIMESTAMP;

    public Proof(){

    }

    public Proof(String closedBy, String closeByDept, String cause, String immAction, String permAction, String pPhotoURL) {
        this.closedBy = closedBy;
        this.closeByDept = closeByDept;
        this.cause = cause;
        this.immAction = immAction;
        this.permAction = permAction;
        this.pPhotoURL = pPhotoURL;
    }

    public String getClosedBy() {
        return closedBy;
    }

    public void setClosedBy(String closedBy) {
        this.closedBy = closedBy;
    }

    public String getCloseByDept() {
        return closeByDept;
    }

    public void setCloseByDept(String closeByDept) {
        this.closeByDept = closeByDept;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public String getImmAction() {
        return immAction;
    }

    public void setImmAction(String immAction) {
        this.immAction = immAction;
    }

    public String getPermAction() {
        return permAction;
    }

    public void setPermAction(String permAction) {
        this.permAction = permAction;
    }

    public String getpPhotoURL() {
        return pPhotoURL;
    }

    public void setpPhotoURL(String pPhotoURL) {
        this.pPhotoURL = pPhotoURL;
    }

    public Object getRepTime() {
        return repTime;
    }

    public void setRepTime(Object repTime) {
        this.repTime = repTime;
    }

    @Override
    public String toString() {
        return "Proof{" +
                "closedBy='" + closedBy + '\'' +
                ", closeByDept='" + closeByDept + '\'' +
                ", cause='" + cause + '\'' +
                ", immAction='" + immAction + '\'' +
                ", permAction='" + permAction + '\'' +
                ", pPhotoURL='" + pPhotoURL + '\'' +
                ", repTime=" + repTime +
                '}';
    }
}

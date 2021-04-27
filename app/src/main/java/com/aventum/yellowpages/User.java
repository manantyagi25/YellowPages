package com.aventum.yellowpages;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {

    public com.aventum.yellowpages.pInfo pInfo;
    public com.aventum.yellowpages.accInfo accInfo;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(com.aventum.yellowpages.pInfo pInfo, com.aventum.yellowpages.accInfo accInfo) {
        this.pInfo = pInfo;
        this.accInfo = accInfo;
    }
}

package com.chaoyang805.cloudnote.model;

/**
 * Created by chaoyang805 on 2015/10/19.
 */
public class User {

    private String mName;
    private String mPasswordMd5;
    private String mEmail;
    private int mUserId;

    public User(int id, String name, String passwordMd5, String email) {
        mUserId = id;
        mName = name;
        mPasswordMd5 = passwordMd5;
        mEmail = email;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getPasswordMd5() {
        return mPasswordMd5;
    }

    public void setPasswordMd5(String passwordMd5) {
        mPasswordMd5 = passwordMd5;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        mEmail = email;
    }

    public int getUserId() {
        return mUserId;
    }

    public void setUserId(int userId) {
        mUserId = userId;
    }
}

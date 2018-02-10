package com.unic_1.hereim.Model;

/**
 * Created by unic-1 on 28/11/17.
 */

public class ContactModel {
    private String name;
    private String number;

    public ContactModel(String name, String number) {
        this.name = name;
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }
}

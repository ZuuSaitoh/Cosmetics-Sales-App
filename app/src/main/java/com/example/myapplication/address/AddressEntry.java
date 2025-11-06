package com.example.myapplication.address;

public class AddressEntry {
    public Long id; // server userAddressId (nullable for local only)
    public String name;
    public String phone;
    public String address;
    public boolean isDefault;

    public AddressEntry(String name, String phone, String address, boolean isDefault) {
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.isDefault = isDefault;
    }

    public AddressEntry(Long id, String name, String phone, String address, boolean isDefault) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.isDefault = isDefault;
    }
}



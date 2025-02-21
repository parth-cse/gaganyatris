package com.gaganyatris.gaganyatri.models;

public class Users {
    private String name;
    private String email;
    private String phone;
    private String dateOfBirth;
    private String gender;
    private String country;
    private String state;
    private String city;

    public Users(String name, String email, String phone, String dob, String gender, String country, String state, String city) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.dateOfBirth = dob;
        this.gender = gender;
        this.country = country;
        this.state = state;
        this.city = city;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDOB() {
        return dateOfBirth;
    }

    public void setDOB(String dob) {
        this.dateOfBirth = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}

package com.gaganyatris.gaganyatri.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;

public class CoTraveller implements Parcelable {

    private String coTraveller_id;
    private String user_uid;
    private String name;
    private String email;
    private String phoneNo;
    private Timestamp dateOfBirth;
    private String gender;
    private String country;
    private String state;
    private String city;
    private int avatarIndex;

    public CoTraveller() {
        // Default constructor required for Firebase
    }

    public CoTraveller(String coTraveller_id, String user_uid, String name, String email, String phoneNo,
                       Timestamp dateOfBirth, String gender, String country, String state, String city, int avatarIndex) {
        this.coTraveller_id = coTraveller_id;
        this.user_uid = user_uid;
        this.name = name;
        this.email = email;
        this.phoneNo = phoneNo;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.country = country;
        this.state = state;
        this.city = city;
        this.avatarIndex = avatarIndex;
    }

    // Parcelable Implementation
    protected CoTraveller(Parcel in) {
        coTraveller_id = in.readString();
        user_uid = in.readString();
        name = in.readString();
        email = in.readString();
        phoneNo = in.readString();
        gender = in.readString();
        country = in.readString();
        state = in.readString();
        city = in.readString();
        avatarIndex = in.readInt();

        // Convert long back to Timestamp
        long dateOfBirthMillis = in.readLong();
        dateOfBirth = dateOfBirthMillis == -1 ? null : new Timestamp(new java.util.Date(dateOfBirthMillis));
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(coTraveller_id);
        dest.writeString(user_uid);
        dest.writeString(name);
        dest.writeString(email);
        dest.writeString(phoneNo);
        dest.writeString(gender);
        dest.writeString(country);
        dest.writeString(state);
        dest.writeString(city);
        dest.writeInt(avatarIndex);

        // Store Timestamp as long
        dest.writeLong(dateOfBirth != null ? dateOfBirth.toDate().getTime() : -1);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CoTraveller> CREATOR = new Creator<CoTraveller>() {
        @Override
        public CoTraveller createFromParcel(Parcel in) {
            return new CoTraveller(in);
        }

        @Override
        public CoTraveller[] newArray(int size) {
            return new CoTraveller[size];
        }
    };

    // Getters and Setters
    public String getCoTraveller_id() {
        return coTraveller_id;
    }

    public void setCoTraveller_id(String coTraveller_id) {
        this.coTraveller_id = coTraveller_id;
    }

    public String getUser_uid() {
        return user_uid;
    }

    public void setUser_uid(String user_uid) {
        this.user_uid = user_uid;
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

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public Timestamp getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Timestamp dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
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

    public int getAvatarIndex() {
        return avatarIndex;
    }

    public void setAvatarIndex(int avatarIndex) {
        this.avatarIndex = avatarIndex;
    }
}

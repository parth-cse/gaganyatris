package com.gaganyatris.gaganyatri.models;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Map;

public class Trip {
    private String tripId;
    private String userUid;

    public CabDetails getCabDetailsTo() {
        return cabDetailsTo;
    }

    public void setCabDetailsTo(CabDetails cabDetailsTo) {
        this.cabDetailsTo = cabDetailsTo;
    }

    private Timestamp tripStartDate;

    public Timestamp getTripStartDate() {
        return tripStartDate;
    }

    public void setTripStartDate(Timestamp tripStartDate) {
        this.tripStartDate = tripStartDate;
    }

    public Trip(String tripId, String userUid, Timestamp tripStartDate, ArrayList<String> coTravellers, Map<String, Object> tripDetails, String tripPlan, TrainDetails trainDetailsTo, TrainDetails trainDetailsFro, CabDetails cabDetailsTo, CabDetails cabDetailsFro) {
        this.tripId = tripId;
        this.userUid = userUid;
        this.tripStartDate = tripStartDate;
        this.coTravellers = coTravellers;
        this.tripDetails = tripDetails;
        this.tripPlan = tripPlan;
        this.trainDetailsTo = trainDetailsTo;
        this.trainDetailsFro = trainDetailsFro;
        this.cabDetailsTo = cabDetailsTo;
        this.cabDetailsFro = cabDetailsFro;
    }

    public CabDetails getCabDetailsFro() {
        return cabDetailsFro;
    }

    public void setCabDetailsFro(CabDetails cabDetailsFro) {
        this.cabDetailsFro = cabDetailsFro;
    }

    private ArrayList<String> coTravellers;
    private Map<String, Object> tripDetails; // Using Map to store various detail types
    private String tripPlan; // Could be a Map, a JSON string, or another object

    private TrainDetails trainDetailsTo, trainDetailsFro;
    private CabDetails cabDetailsTo, cabDetailsFro;

    public Trip(String tripId, String userUid, ArrayList<String> coTravellers, Map<String, Object> tripDetails, String tripPlan, TrainDetails trainDetailsTo, TrainDetails trainDetailsFro, CabDetails cabDetailsTo, CabDetails cabDetailsFro) {
        this.tripId = tripId;
        this.userUid = userUid;
        this.coTravellers = coTravellers;
        this.tripDetails = tripDetails;
        this.tripPlan = tripPlan;
        this.trainDetailsTo = trainDetailsTo;
        this.trainDetailsFro = trainDetailsFro;
        this.cabDetailsTo = cabDetailsTo;
        this.cabDetailsFro = cabDetailsFro;
    }

    // Default Constructor (required for Firebase)
    public Trip() {
    }

    public Trip(String tripId, String userUid, ArrayList<String> coTravellers, Map<String, Object> tripDetails) {
        this.tripId = tripId;
        this.userUid = userUid;
        this.coTravellers = coTravellers;
        this.tripDetails = tripDetails;
    }

    // Constructor
    public Trip(String userUid, ArrayList<String> coTravellers, Map<String, Object> tripDetails, String tripPlan) {
        this.userUid = userUid;
        this.coTravellers = coTravellers;
        this.tripDetails = tripDetails;
        this.tripPlan = tripPlan;
    }

    // Getters and Setters

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    public ArrayList<String> getCoTravellers() {
        return coTravellers;
    }

    public void setCoTravellers(ArrayList<String> coTravellers) {
        this.coTravellers = coTravellers;
    }

    public Map<String, Object> getTripDetails() {
        return tripDetails;
    }

    public void setTripDetails(Map<String, Object> tripDetails) {
        this.tripDetails = tripDetails;
    }

    public String getTripPlan() {
        return tripPlan;
    }

    public void setTripPlan(String tripPlan) {
        this.tripPlan = tripPlan;
    }

    public TrainDetails getTrainDetailsTo() {
        return trainDetailsTo;
    }

    public void setTrainDetailsTo(TrainDetails trainDetailsTo) {
        this.trainDetailsTo = trainDetailsTo;
    }

    public TrainDetails getTrainDetailsFro() {
        return trainDetailsFro;
    }

    public void setTrainDetailsFro(TrainDetails trainDetailsFro) {
        this.trainDetailsFro = trainDetailsFro;
    }
}
package com.gaganyatris.gaganyatri.models;

public class TrainDetails {
    private String pnrNumber;
    private String trainNumber;
    private String trainName;
    private String travelClass;
    private String fromStation;
    private String toStation;
    private String departureTime;
    private String arrivalTime;
    private String source;
    private String destination;
    private String travelStartDate;
    private String travelDestDate;
    private String duration;

    public TrainDetails() {
    }

    public TrainDetails(String pnrNumber, String trainNumber, String trainName, String travelClass, String fromStation, String toStation, String departureTime, String arrivalTime, String source, String destination, String travelStartDate, String travelDestDate, String duration) {
        this.pnrNumber = pnrNumber;
        this.trainNumber = trainNumber;
        this.trainName = trainName;
        this.travelClass = travelClass;
        this.fromStation = fromStation;
        this.toStation = toStation;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.source = source;
        this.destination = destination;
        this.travelStartDate = travelStartDate;
        this.travelDestDate = travelDestDate;
        this.duration = duration;
    }

    // Getters and Setters
    public String getPnrNumber() {
        return pnrNumber;
    }

    public void setPnrNumber(String pnrNumber) {
        this.pnrNumber = pnrNumber;
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(String trainNumber) {
        this.trainNumber = trainNumber;
    }

    public String getTrainName() {
        return trainName;
    }

    public void setTrainName(String trainName) {
        this.trainName = trainName;
    }

    public String getTravelClass() {
        return travelClass;
    }

    public void setTravelClass(String travelClass) {
        this.travelClass = travelClass;
    }

    public String getFromStation() {
        return fromStation;
    }

    public void setFromStation(String fromStation) {
        this.fromStation = fromStation;
    }

    public String getToStation() {
        return toStation;
    }

    public void setToStation(String toStation) {
        this.toStation = toStation;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getTravelStartDate() {
        return travelStartDate;
    }

    public void setTravelStartDate(String travelStartDate) {
        this.travelStartDate = travelStartDate;
    }

    public String getTravelDestDate() {
        return travelDestDate;
    }

    public void setTravelDestDate(String travelDestDate) {
        this.travelDestDate = travelDestDate;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
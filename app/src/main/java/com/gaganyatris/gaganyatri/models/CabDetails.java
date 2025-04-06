package com.gaganyatris.gaganyatri.models;

public class CabDetails {

    private String cabNumber;
    private String startTime;
    private String tripDate;
    private String endTime;
    private String endDate;
    private String source;
    private String destination;
    private String duration;

    public CabDetails() {
    }

    public CabDetails(String cabNumber, String startTime, String tripDate, String endTime, String endDate, String source, String destination, String duration) {
        this.cabNumber = cabNumber;
        this.startTime = startTime;
        this.tripDate = tripDate;
        this.endTime = endTime;
        this.endDate = endDate;
        this.source = source;
        this.destination = destination;
        this.duration = duration;
    }


    public String getCabNumber() {
        return cabNumber;
    }

    public void setCabNumber(String cabNumber) {
        this.cabNumber = cabNumber;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getTripDate() {
        return tripDate;
    }

    public void setTripDate(String tripDate) {
        this.tripDate = tripDate;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
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

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
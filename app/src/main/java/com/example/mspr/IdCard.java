package com.example.mspr;

public class IdCard {
    private String FirstName;
    private String LastName;
    private String NoIdCard;

    public IdCard() {

    }

    public IdCard(String FirstName, String LastName, String NoIdCard) {
        this.FirstName = FirstName;
        this.LastName = LastName;
        this.NoIdCard = NoIdCard;
    }

    public String getFirstName() {
        return FirstName;
    }

    public void setFirstName(String firstName) {
        FirstName = firstName;
    }

    public String getLastName() {
        return LastName;
    }

    public void setLastName(String lastName) {
        LastName = lastName;
    }
    public String getNoIdCard() {
        return NoIdCard;
    }

    public void setNoIdCard(String noIdCard) {
        NoIdCard = noIdCard;
    }
}

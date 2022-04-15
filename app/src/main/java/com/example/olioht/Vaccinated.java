package com.example.olioht;

public class Vaccinated {
    private String index;
    private String date;
    private Integer vaxAmount;

    public Vaccinated(String index, String date, Integer vaxAmount) {
        this.index = index;
        this.date = date;
        this.vaxAmount = vaxAmount;
    }

    public String getIndex() {
        return index;
    }

    public String getDate() {
        return date;
    }

    public Integer getVaxAmount() {
        return vaxAmount;
    }

    @Override
    public String toString() {
        return "Vaccinated{" +
                "index='" + index + '\'' +
                ", date='" + date + '\'' +
                ", vaxAmount=" + vaxAmount +
                '}';
    }
}

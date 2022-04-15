package com.example.olioht;

public class CoronaCase {
    private Integer amount;
    private String index;
    private String date;

    public CoronaCase(Integer amount, String index, String date) {
        this.amount = amount;
        this.index = index;
        this.date = date;
    }

    public Integer getAmount() {
        return amount;
    }

    public String getIndex() {
        return index;
    }

    public String getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "CoronaCase{" +
                "amount=" + amount +
                ", index='" + index + '\'' +
                ", date='" + date + '\'' +
                '}';
    }
}

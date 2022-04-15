package com.example.olioht;

public class HealthCareDistrict {
    private String id;
    private Integer index;
    private String name;
    public HealthCareDistrict(String id, Integer index, String name) {
        this.id = id;
        this.index = index;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public Integer getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "HealthCareDistrict{" +
                "id='" + id + '\'' +
                ", index=" + index +
                ", name='" + name + '\'' +
                '}';
    }
}

package com.example.olioht;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class Area {

    // *** SINGLETON ***

    private static Area area = new Area();
    private Area() { }
    public static Area getInstance() {
        return area;
    }

    // ***

    public ArrayList<City> getCityList(String url) {
        ArrayList<City> CITYList = new ArrayList<>();
        JsonDataParser jdp = new JsonDataParser();
        String json = jdp.getJson(url);

        if (json != null) {
            Gson cgson = new GsonBuilder().create();
            JsonObject cjob = cgson.fromJson(json, JsonObject.class);
            JsonObject index = cjob.getAsJsonObject("dataset").getAsJsonObject("dimension").getAsJsonObject("hcdmunicipality2020").getAsJsonObject("category").getAsJsonObject("index");
            JsonObject label = cjob.getAsJsonObject("dataset").getAsJsonObject("dimension").getAsJsonObject("hcdmunicipality2020").getAsJsonObject("category").getAsJsonObject("label");
            String inStr = index.toString();
            LinkedHashMap<String, Double> indexMap = new Gson().fromJson(inStr, LinkedHashMap.class);

            String laStr = label.toString();
            LinkedHashMap<String, String> labelMap = new Gson().fromJson(laStr,LinkedHashMap.class);
            for (Map.Entry<String, Double> entry : indexMap.entrySet()) {
                String key = entry.getKey();
                int value = entry.getValue().intValue();
                CITYList.add(new City(key, value, labelMap.get(key)));
            }
        }
        return CITYList;
    }

    public ArrayList<HealthCareDistrict> getHCDList(String url) {
        ArrayList<HealthCareDistrict> HCDList = new ArrayList<>();
        JsonDataParser jdp = new JsonDataParser();
        String json = jdp.getJson(url);

        if (json != null) {
            JsonObject job = JsonParser.parseString(json).getAsJsonObject();
            JsonObject index = job.getAsJsonObject("dataset").getAsJsonObject("dimension").getAsJsonObject("hcdmunicipality2020").getAsJsonObject("category").getAsJsonObject("index");
            JsonObject label = job.getAsJsonObject("dataset").getAsJsonObject("dimension").getAsJsonObject("hcdmunicipality2020").getAsJsonObject("category").getAsJsonObject("label");

            String inStr = index.toString();
            LinkedHashMap<String, Double> indexMap = new Gson().fromJson(inStr, LinkedHashMap.class);

            String laStr = label.toString();
            LinkedHashMap<String, String> labelMap = new Gson().fromJson(laStr, LinkedHashMap.class);

            for (Map.Entry<String, Double> entry : indexMap.entrySet()) {
                String key = entry.getKey();
                int value = entry.getValue().intValue();
                HCDList.add(new HealthCareDistrict(key, value, labelMap.get(key)));
            }
        }
        return HCDList;
    }

    public ArrayList<HealthCareDistrict> getVaxHCDList(String url) {
        ArrayList<HealthCareDistrict> VaxHCDList = new ArrayList<>();

        JsonDataParser jdp = new JsonDataParser();
        String json = jdp.getJson(url);

        if (json != null) {
            Gson gson = new GsonBuilder().create();
            JsonObject job = gson.fromJson(json, JsonObject.class);
            JsonObject index = job.getAsJsonObject("dataset").getAsJsonObject("dimension").getAsJsonObject("area").getAsJsonObject("category").getAsJsonObject("index");
            JsonObject label = job.getAsJsonObject("dataset").getAsJsonObject("dimension").getAsJsonObject("area").getAsJsonObject("category").getAsJsonObject("label");
            String indexStr = index.toString();
            String labelStr = label.toString();

            LinkedHashMap<String, Double> indexMap = new Gson().fromJson(indexStr, LinkedHashMap.class);
            LinkedHashMap<String, String> labelMap = new Gson().fromJson(labelStr, LinkedHashMap.class);

            for (Map.Entry<String, Double> entry : indexMap.entrySet()) {
                String key = entry.getKey();
                int value = entry.getValue().intValue();
                VaxHCDList.add(new HealthCareDistrict(key, value, labelMap.get(key)));
            }
        }
        return VaxHCDList;
    }
}

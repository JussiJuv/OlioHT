package com.example.olioht;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class JsonDataParser {

    /* Method reads JSON database site and returns JSON string. */
    public String getJson(String urlString) {
        String response = null;
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            InputStream in = new BufferedInputStream(conn.getInputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            response = sb.toString();
            in.close();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    /* Method gets current week id and returns it. */
    public String getWeekId(String json, int week, int year) {
        String id = "";
        if (json != null) {
            Gson gson = new GsonBuilder().create();
            JsonObject job = gson.fromJson(json, JsonObject.class);
            JsonObject label = job.getAsJsonObject("dataset").getAsJsonObject("dimension").getAsJsonObject("dateweek20200101").getAsJsonObject("category").getAsJsonObject("label");
            String labelStr = label.toString();
            LinkedHashMap<String, String> labelMap = new Gson().fromJson(labelStr, LinkedHashMap.class);
            for (Map.Entry<String, String> entry : labelMap.entrySet()) {
                String yearWeek = entry.getValue();
                String[] dateList = yearWeek.split(" ");
                try {
                    if (Integer.parseInt(dateList[3]) == week && Integer.parseInt(dateList[1]) == year) {
                        id = entry.getKey();
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("Error: " + e);
                }
            }
        } else {
            System.out.println("Input string is null");
        }
        return id;
    }

    /* Method gets JSON string and parses useful data from the string and creates CoronaCase list
       and returns the list. */
    public ArrayList<CoronaCase> jsonToCoronaCaseList(String url) {
        ArrayList<CoronaCase> caseList = new ArrayList<>();
        String json = getJson(url);

        if (json != null) {
            Gson gson = new GsonBuilder().create();
            JsonObject job = gson.fromJson(json, JsonObject.class);
            JsonObject index = job.getAsJsonObject("dataset").getAsJsonObject("dimension").getAsJsonObject("dateweek20200101").getAsJsonObject("category").getAsJsonObject("index");
            JsonObject label = job.getAsJsonObject("dataset").getAsJsonObject("dimension").getAsJsonObject("dateweek20200101").getAsJsonObject("category").getAsJsonObject("label");
            JsonObject amount = job.getAsJsonObject("dataset").getAsJsonObject("value");

            String indexStr = index.toString();
            String labelStr = label.toString();

            LinkedHashMap<String, Double> indexMap = new Gson().fromJson(indexStr, LinkedHashMap.class);
            LinkedHashMap<String, String> labelMap = new Gson().fromJson(labelStr, LinkedHashMap.class);
            LinkedHashMap<String, String> caseMap = new Gson().fromJson(amount, LinkedHashMap.class);

            Integer i = 0;
            for (Map.Entry<String, Double> entry : indexMap.entrySet()) {
                String key = entry.getKey();
                String yearWeek = labelMap.get(key);
                String caseAmount = caseMap.get(String.valueOf(i));

                yearWeek = yearWeek.replaceAll("Vuosi ", "");
                yearWeek = yearWeek.replaceAll(" Viikko", ""); // yyyy w

                String[] dList = yearWeek.split(" ");
                if (dList[1].length() == 1) {
                    dList[1] = "0" + dList[1];
                }
                yearWeek = dList[0] + " " + dList[1];
                CoronaCase coronaCase;
                try {
                    coronaCase = new CoronaCase(Integer.parseInt(caseAmount), key, yearWeek);
                } catch (NumberFormatException e) {
                    coronaCase = new CoronaCase(0, key, yearWeek);
                }
                caseList.add(coronaCase);
                i++;
            }
        }
        return caseList;
    }

    /* Method gets JSON string and parses useful data from the string, creates Vaccinated list
       and returns the list. */
    public ArrayList<Vaccinated> jsonToVaxList(String url) {
        ArrayList<Vaccinated> vaxList = new ArrayList<>();
        JsonDataParser jdp = new JsonDataParser();
        String json = jdp.getJson(url);

        if (json != null) {
            Gson gson = new GsonBuilder().create();
            JsonObject job = gson.fromJson(json, JsonObject.class);
            JsonObject index = job.getAsJsonObject("dataset").getAsJsonObject("dimension").getAsJsonObject("dateweek20201226").getAsJsonObject("category").getAsJsonObject("index");
            JsonObject label = job.getAsJsonObject("dataset").getAsJsonObject("dimension").getAsJsonObject("dateweek20201226").getAsJsonObject("category").getAsJsonObject("label");
            JsonObject value = job.getAsJsonObject("dataset").getAsJsonObject("value");
            String indexStr = index.toString();
            String labelStr = label.toString();

            LinkedHashMap<String, Double> indexMap = new Gson().fromJson(indexStr, LinkedHashMap.class);
            LinkedHashMap<String, String> labelMap = new Gson().fromJson(labelStr, LinkedHashMap.class);
            LinkedHashMap<String, String> vaxMap = new Gson().fromJson(value, LinkedHashMap.class);

            Integer i = 0;
            for (Map.Entry<String, Double> entry : indexMap.entrySet()) {
                String key = entry.getKey();
                String yearWeek = labelMap.get(key);
                String vaxAmount = vaxMap.get(String.valueOf(i));

                yearWeek = yearWeek.replaceAll("Vuosi ", "");
                yearWeek = yearWeek.replaceAll(" Viikko", ""); // yyyy w

                String[] dList = yearWeek.split(" ");
                if (dList[1].length() == 1) {
                    dList[1] = "0" + dList[1];
                }
                yearWeek = dList[0] + " " + dList[1];
                Vaccinated vaccinated;
                try {
                    vaccinated = new Vaccinated(key, yearWeek, Integer.parseInt(vaxAmount));
                } catch (NumberFormatException e) {
                    vaccinated = new Vaccinated(key, yearWeek, 0);
                }
                vaxList.add(vaccinated);
                i++;
            }
        }
        return vaxList;
    }
}

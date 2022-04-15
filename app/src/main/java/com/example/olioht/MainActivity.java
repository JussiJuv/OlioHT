package com.example.olioht;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileNotFoundException;

import java.io.IOException;
import java.io.OutputStreamWriter;

import java.util.ArrayList;
import java.util.Calendar;

import java.util.LinkedHashMap;

import java.util.Map;
import java.util.Scanner;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {


    Spinner spin;
    Spinner citySpinner;
    Spinner yearSpinner;
    Spinner weekSpinner;

    Button searchBtnTop;
    Button searchBtnBot;
    Button saveBtn;

    TextView resultText;
    TextView listViewText;
    ListView listView;

    Context context = null;

    Area area = Area.getInstance();

    ArrayList<String>  yearList = new ArrayList<>();
    ArrayList<String> weekList = new ArrayList<>();
    ArrayList<String> HCDNameList = new ArrayList<>();
    ArrayList<String> CityNameList = new ArrayList<>();

    ArrayList<CoronaCase> caseList = new ArrayList<>();
    ArrayList<Vaccinated> vaxList = new ArrayList<>();
    ArrayList<City> saveList = new ArrayList<>();
    ArrayList<String> cityCaseAmount = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = MainActivity.this;
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        spin = (Spinner) findViewById(R.id.HCDspinner);
        citySpinner = (Spinner) findViewById(R.id.CITYspinner);
        yearSpinner = (Spinner) findViewById(R.id.yearSpinner);
        weekSpinner = (Spinner) findViewById(R.id.weekSpinner);
        searchBtnTop = (Button) findViewById(R.id.searchBtnTop);
        searchBtnBot = (Button) findViewById(R.id.searchBtnBot);
        saveBtn = (Button) findViewById(R.id.saveBtn);
        resultText = (TextView) findViewById(R.id.resultText);
        listViewText = (TextView) findViewById(R.id.listViewText);
        listView = (ListView) findViewById(R.id.listView);

        yearList.add("2020");
        yearList.add("2021");
        yearList.add("2022");
        yearList.add("All of the above");

        String HCDUrl = "https://sampo.thl.fi/pivot/prod/fi/epirapo/covid19case/fact_epirapo_covid19case.json?row=hcdmunicipality2020-445222&column=dateweek20200101-509030&filter=measure-444833";
        ArrayList<HealthCareDistrict> HCDList = area.getHCDList(HCDUrl);
        for (int i=0; i<HCDList.size(); i++) {
            HCDNameList.add(HCDList.get(i).getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, HCDNameList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(adapter);

        ArrayAdapter<String> yAdapter = new ArrayAdapter<String>(
                getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, yearList);
        yAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(yAdapter);
        yearSpinner.setSelection(2);
        yearSpinner.setOnItemSelectedListener(this);

        initWeekList();
        displaySavedData();

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, cityCaseAmount);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Save button clicked!");
                saveData();
                arrayAdapter.notifyDataSetChanged();
            }
        };
        saveBtn.setOnClickListener(onClickListener);
        listView.setAdapter(arrayAdapter);
    }

    /* Method gets current week id to show realtime data on listView on startup
       and when user saves a city to file. */
    public String getCurrentWeekId() {
        Calendar calendar = Calendar.getInstance();
        int weekNumber = calendar.get(Calendar.WEEK_OF_YEAR) - 1;
        int year = calendar.get(Calendar.YEAR);

        String weekURL = "https://sampo.thl.fi/pivot/prod/fi/epirapo/covid19case/fact_epirapo_covid19case.json?column=dateweek20200101-509030";
        JsonDataParser jdp = new JsonDataParser();
        String json = jdp.getJson(weekURL);
        String id = jdp.getWeekId(json, weekNumber, year);
        return id;
    }

    /* Method displays user saved data on app launch. */
    public void displaySavedData() {
        getSavedData();
        String id = getCurrentWeekId();
        Calendar calendar = Calendar.getInstance();
        int weekNumber = calendar.get(Calendar.WEEK_OF_YEAR) - 1;

        /* Searching corona case amounts on current week for each city user has saved. */
        for(City city : saveList) {
            String areaID = city.getId();
            String cityName = city.getName();

            if (!areaID.isEmpty() && !id.isEmpty()) {
                String url = "https://sampo.thl.fi/pivot/prod/fi/epirapo/covid19case/fact_epirapo_covid19case.json?row=hcdmunicipality2020-"+
                        areaID +".&column=dateweek20200101-" + id + ".&filter=measure-444833";
                JsonDataParser jdp = new JsonDataParser();
                String cityJson = jdp.getJson(url);

                String amount = getAmount(cityJson);
                addToList(cityName, amount);
            }
        }
        String textHint = "Cases on week " + weekNumber;
        listViewText.setText(textHint);
    }


    public void addToList(String name, String amount) {
        cityCaseAmount.add(name + " " + amount);
    }

    public String getAmount(String json) {
        String amount = "";
        if (json != null) {
            Gson cgson = new GsonBuilder().create();
            JsonObject cjob = cgson.fromJson(json, JsonObject.class);
            JsonObject value = cjob.getAsJsonObject("dataset").getAsJsonObject("value");

            LinkedHashMap<String, String> amountMap = new Gson().fromJson(value, LinkedHashMap.class);
            for (Map.Entry<String, String> entry : amountMap.entrySet()) {
                amount = entry.getValue();
            }
        }
        return amount;
    }

    /* Method reads saved file data if it exist on app start up. */
    public void getSavedData() {
        File saveFile = new File(getApplicationContext().getFilesDir(),  "coronaSave.txt");

        if (saveFile.exists()) {
            System.out.println("Save file exist, loading data");
            try {
                Scanner scanner = new Scanner(saveFile);
                scanner.nextLine();
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    String[] arrLine = line.split(";");
                    String id = arrLine[0];
                    Integer index = Integer.parseInt(arrLine[1]);
                    String name = arrLine[2];

                    City city = new City(id, index, name);
                    saveList.add(city);
                }
            } catch (FileNotFoundException e) {
                System.out.println("Error: " + e);
            }
        }
    }

    /* Method initializes week list and week spinner. */
    public void initWeekList() {
        weekList.clear();
        int choice = yearSpinner.getSelectedItemPosition();
        if (choice == 0) {                                      // 2020
            for (int i = 0; i<53; i++) {
                weekList.add(String.valueOf(i+1));
            }
        } else if (choice == 1 || choice == 2) {                // 2021 | 2022
            for (int i = 0; i<52; i++) {
                weekList.add(String.valueOf(i+1));
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, weekList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        weekSpinner.setAdapter(adapter);
    }

    /* Method gets called from top right button. Method gets user selected healthcare district from
       spinner and displays cities within selected district. */
    public void districtSearch(View v) {
        CityNameList.clear();
        citySpinner.setAdapter(null);
        int choice = spin.getSelectedItemPosition();
        String HCDUrl = "https://sampo.thl.fi/pivot/prod/fi/epirapo/covid19case/fact_epirapo_covid19case.json?row=hcdmunicipality2020-445222&column=dateweek20200101-509030&filter=measure-444833";
        ArrayList<HealthCareDistrict> HCDList = area.getHCDList(HCDUrl);
        String id = HCDList.get(choice).getId();


        if (choice != 21) {
            String cityUrl = "https://sampo.thl.fi/pivot/prod/fi/epirapo/covid19case/fact_epirapo_covid19case.json?" +
                    "row=hcdmunicipality2020-" + id + "&column=dateweek20200101-509030&filter=measure-444833";

            ArrayList<City> CITYList = area.getCityList(cityUrl);
            for (int i = 0; i < CITYList.size(); i++) {
                CityNameList.add(CITYList.get(i).getName());
            }
            /* Setting spinner to show cities in a selected district. */
            ArrayAdapter<String> cadapter = new ArrayAdapter<String>(
                    getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, CityNameList);
            cadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            citySpinner.setAdapter(cadapter);
        } else if (choice == 21) {
            String url = "https://sampo.thl.fi/pivot/prod/fi/epirapo/covid19case/fact_epirapo_covid19case.json?" +
                    "row=hcdmunicipality2020-" + id + ".&column=dateweek20200101-509030&filter=measure-444833";
            JsonDataParser jdp = new JsonDataParser();
            caseList = jdp.jsonToCoronaCaseList(url);
        }
    }

    /* Method gets called from the button below top right. Method gets corona case amount in
     selected city. */
    public void citySearch(View v) {
        try {
            int choice = citySpinner.getSelectedItemPosition();

            String HCDUrl = "https://sampo.thl.fi/pivot/prod/fi/epirapo/covid19case/fact_epirapo_covid19case.json?row=hcdmunicipality2020-445222&column=dateweek20200101-509030&filter=measure-444833";
            ArrayList<HealthCareDistrict> HCDList = area.getHCDList(HCDUrl);
            String idC = HCDList.get(spin.getSelectedItemPosition()).getId();
            String cityUrl = "https://sampo.thl.fi/pivot/prod/fi/epirapo/covid19case/fact_epirapo_covid19case.json?" +
                    "row=hcdmunicipality2020-" + idC + "&column=dateweek20200101-509030&filter=measure-444833";
            ArrayList<City> CITYList = area.getCityList(cityUrl);

            String id = CITYList.get(choice).getId();
            String url = "https://sampo.thl.fi/pivot/prod/fi/epirapo/covid19case/fact_epirapo_covid19case.json?" +
                    "row=hcdmunicipality2020-" + id + ".&column=dateweek20200101-509030&filter=measure-444833";
            JsonDataParser jdp = new JsonDataParser();
            caseList = jdp.jsonToCoronaCaseList(url);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Error city spinner does not have any items:" + e);
        }

    }
    /* This method gets user selected filters from spinners and displays
       corona case amount on screen. */
    public void analyse(View v) {
        getVax();
        String result = "";
        String vaxResult = "";

        String HCDUrl = "https://sampo.thl.fi/pivot/prod/fi/epirapo/covid19case/fact_epirapo_covid19case.json?row=hcdmunicipality2020-445222&column=dateweek20200101-509030&filter=measure-444833";
        ArrayList<HealthCareDistrict> HCDList = area.getHCDList(HCDUrl);
        String id = HCDList.get(spin.getSelectedItemPosition()).getId();
        String cityUrl = "https://sampo.thl.fi/pivot/prod/fi/epirapo/covid19case/fact_epirapo_covid19case.json?" +
                "row=hcdmunicipality2020-" + id + "&column=dateweek20200101-509030&filter=measure-444833";
        ArrayList<City> CITYList = area.getCityList(cityUrl);

        if (yearSpinner.getSelectedItemPosition() == 3) {                                           // All years
            int amount = caseList.get(caseList.size() - 1).getAmount();
            int vaxAmount = vaxList.get(vaxList.size()-1).getVaxAmount();
            if (spin.getSelectedItemPosition() == 21) {                                             // All years and all districts
                result = String.format("Cases in Finland: %,d\nVaccinated: %,d", amount, vaxAmount).// Formatting numbers for easier reading
                        replaceAll(",", " ");
                resultText.setText(result);
            } else {                                                                                // All years 1 district
                String districtName = HCDList.get(spin.getSelectedItemPosition()).getName();
                if (citySpinner.getSelectedItemPosition() == CityNameList.size() - 1) {             // All years entire district
                    result = String.format("Cases in " + districtName + ": %,d" +
                            "\nVaccinated: %,d", amount, vaxAmount).replaceAll(
                                    ",", " ");
                    resultText.setText(result);
                } else {                                                                            // All years 1 city
                    String cityName = CITYList.get(citySpinner.getSelectedItemPosition()).getName();
                    result = String.format("Cases in " + cityName + ": %,d" +
                            "\nVaccinated in " + districtName + ": %,d", amount,
                            vaxAmount).replaceAll(",", " ");
                    resultText.setText(result);
                }
            }
        } else {                                                                                    // 1 week
            String year = yearSpinner.getSelectedItem().toString();
            String week = weekSpinner.getSelectedItem().toString();
            if (week.length() == 1) {
                week = "0" + week;
            }
            String date = year + " " + week;
            if (spin.getSelectedItemPosition() == 21) {                                             //  1 week all districts
                for (CoronaCase coronaCase : caseList) {
                    if (coronaCase.getDate().contains(date)) {
                        result = String.format("Cases in Finland: %,d", coronaCase.getAmount()).
                                replace(",", " ");
                        for (Vaccinated vaccinated : vaxList) {
                            if (vaccinated.getDate().contains(date)) {
                                vaxResult = String.format("\nVaccinated: %,d", vaccinated.
                                        getVaxAmount()).replace(",", " ");
                            }
                        }
                        resultText.setText(result + vaxResult);
                    }
                }
            } else {                                                                                //  1 week 1 district
                if (citySpinner.getSelectedItemPosition() == CityNameList.size() - 1) {             // 1 week entire district
                    String districtName = spin.getSelectedItem().toString();
                    for (CoronaCase coronaCase : caseList) {
                        if (coronaCase.getDate().contains(date)) {
                            result = String.format("Cases in " + districtName + ": %,d",
                                    coronaCase.getAmount()).replace(",", " ");
                            for (Vaccinated vaccinated : vaxList) {
                                if (vaccinated.getDate().contains(date)) {
                                    vaxResult = String.format("\nVaccinated: %,d",
                                            vaccinated.getVaxAmount()).
                                            replace(",", " ");
                                }
                            }
                            resultText.setText(result + vaxResult);
                        }
                    }
                } else {                                                                            // 1 week 1 city
                    String cityName = citySpinner.getSelectedItem().toString();
                    String districtName = HCDList.get(spin.getSelectedItemPosition()).getName();
                    for (CoronaCase coronaCase : caseList) {
                        if (coronaCase.getDate().contains(date)) {
                            result = String.format("Cases in " + cityName + ": %,d",
                                    coronaCase.getAmount()).replace(",", " ");
                        }
                    }
                    for (Vaccinated vaccinated : vaxList) {
                        if (vaccinated.getDate().contains(date)) {
                            vaxResult = String.format("\nVaccinated in " + districtName + ": %,d",
                                    vaccinated.getVaxAmount()). replace(",", " ");
                        }
                    }
                    resultText.setText(result + vaxResult);
                }
            }
        }
    }

    public void getVax() {
        vaxList.clear();
        String url = "https://sampo.thl.fi/pivot/prod/fi/vaccreg/cov19cov/fact_cov19cov.json?";
        ArrayList<HealthCareDistrict> VaxHCDList = area.getVaxHCDList(url);

        String selectedHCD = spin.getSelectedItem().toString().toLowerCase();
        for (int i = 0; i<VaxHCDList.size(); i++) {
            if (selectedHCD.equals(VaxHCDList.get(i).getName().toLowerCase())) {
                getVaxData(VaxHCDList.get(i).getId());
            }
        }
    }

    /* Method gets a list Vaccine objects. */
    public void getVaxData(String id) {
        String url = "https://sampo.thl.fi/pivot/prod/fi/vaccreg/cov19cov/fact_cov19cov.json?row=hcdmunicipality2020-"
                + id + ".&column=dateweek20201226-525425";
        JsonDataParser jdp = new JsonDataParser();
        vaxList = jdp.jsonToVaxList(url);
    }

    /* Method Saves user selected city name, index, and id to a csv file. */
    public void saveData() {
        if(citySpinner.getSelectedItemPosition() != -1) {                                            // Checking if user has selected a city.
            int contains;
            String name = citySpinner.getSelectedItem().toString();
            Integer index = citySpinner.getSelectedItemPosition();

            String HCDUrl = "https://sampo.thl.fi/pivot/prod/fi/epirapo/covid19case/fact_epirapo_covid19case.json?row=hcdmunicipality2020-445222&column=dateweek20200101-509030&filter=measure-444833";
            ArrayList<HealthCareDistrict> HCDList = area.getHCDList(HCDUrl);
            String idC = HCDList.get(spin.getSelectedItemPosition()).getId();
            String cityUrl = "https://sampo.thl.fi/pivot/prod/fi/epirapo/covid19case/fact_epirapo_covid19case.json?" +
                    "row=hcdmunicipality2020-" + idC + "&column=dateweek20200101-509030&filter=measure-444833";
            ArrayList<City> CITYList = area.getCityList(cityUrl);

            String id = CITYList.get(index).getId();
            String output = id + ";" + index + ";" + name + "\n";
            File f = new File(getApplicationContext().getFilesDir(),"coronaSave.txt");

            /* Adding user saved city to a list for updating listView. */
            String weekId = getCurrentWeekId();
            String url = "https://sampo.thl.fi/pivot/prod/fi/epirapo/covid19case/fact_epirapo_covid19case.json?row=hcdmunicipality2020-"+
                    id +".&column=dateweek20200101-" + weekId + ".&filter=measure-444833";
            JsonDataParser jdp = new JsonDataParser();
            String cityJson = jdp.getJson(url);
            String amount = getAmount(cityJson);

            if (f.exists()) {                                                                        // Checking if file exists, if not create a headline for csv file.
                System.out.println("File is not empty");
                contains = checkFileForString(name);                                                 // Checking if saved file already contains selected city.
                if (contains == 0) {
                    try {
                        System.out.println("Adding " + name + " to file");
                        OutputStreamWriter osw2 = new OutputStreamWriter(
                                context.openFileOutput("coronaSave.txt", Context.MODE_APPEND));
                        osw2.append(output);
                        osw2.close();
                        addToList(name, amount);
                    } catch (IOException e) {
                        System.out.println("Exception: " + e);
                    }
                } else {
                    System.out.println(name + " already in file");
                }
            } else {
                try {
                    System.out.println("File does not exist, creating file");
                    OutputStreamWriter osw1 = new OutputStreamWriter(
                            context.openFileOutput("coronaSave.txt", Context.MODE_APPEND));
                    osw1.append("CityId;CityIndex;CityName\n");
                    osw1.append(output);
                    osw1.close();
                    addToList(name, amount);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    /* Method checks if coronaSave.txt contains input string. Returns 1 if file contains input string
       and returns 0 if not. */
    public int checkFileForString(String str) {
        int contains = -1;
        File file = new File(getApplicationContext().getFilesDir(),"coronaSave.txt");
        try {
            Scanner scanner = new Scanner(file);
            int i = 0;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                i++;
                if(line.contains(str.trim())) {
                    contains = 1;
                    System.out.println("String: " + str + " found on line: " + i);
                } else {
                    contains = 0;
                }
            }
        } catch(FileNotFoundException e) {
            System.out.println("Error: " + e);
        }
        return contains;
    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        initWeekList();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
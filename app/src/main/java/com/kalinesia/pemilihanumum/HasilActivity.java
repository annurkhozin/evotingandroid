package com.kalinesia.pemilihanumum;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HasilActivity extends AppCompatActivity {

    private  String base_url;
    DatabaseHelper dbHelper;
    protected Cursor cursor;
    SharedPreferences pref;    //Declaration SharedPreferences
    String kode_pemilih, pin, id_campaign;
    ProgressDialog progressDialog;
    private BarChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hasil);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading..."); // Setting Message
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
        progressDialog.show(); // Display Progress Dialog
        progressDialog.setCancelable(false);

        pref = getSharedPreferences("SharedPref", Context.MODE_PRIVATE);
        kode_pemilih =  pref.getString("kode_pemilih",null);
        pin =  pref.getString("pin",null);
        id_campaign =  pref.getString("id_campaign",null);
        if((kode_pemilih==null) || (pin==null) || (id_campaign==null)){
            Toast.makeText(getApplicationContext(),  "Sesi anda telah habis, mohon login kembali", Toast.LENGTH_LONG).show();
            //User Logged in Successfully Launch You home screen activity
            Intent intent=new Intent(HasilActivity.this,ScanCodeActivity.class);
            startActivity(intent);
            finish();
        }

        dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        cursor = db.rawQuery("SELECT * FROM tbl_ip", null);
        cursor.moveToFirst();
        if (cursor.getCount()>0) {
            cursor.moveToPosition(0);
            base_url = cursor.getString(1).toString();
        }
        getDataHasil();

    }

    public void getDataHasil(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Initialize a new JsonObjectRequest instance
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                base_url +"/hasilPoling?id_campaign="+id_campaign+"&tahun=2018",
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        progressDialog.dismiss();
                        // Process the JSON
                        try{
                            // Get the JSON array
                            String code = response.getString("code");
                            if(code.toString().equals("200".toString())) {
                                JSONArray dataArray = response.getJSONArray("calon");
                                generateChart(dataArray);
                            }else {
                                String message = response.getString("message");

                                Toast.makeText(getApplicationContext(),  message, Toast.LENGTH_LONG).show();
                                builder.setTitle("GAGAL!");
                                builder.setPositiveButton("oke", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                                builder.setMessage(message);
                                AlertDialog alert1 = builder.create();
                                alert1.show();
                            }

                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }


                    private void generateChart(JSONArray dataCalon) throws JSONException {
                        chart = (BarChart) findViewById(R.id.chart);
                        ArrayList<String> xAxis = new ArrayList<>();
                        ArrayList<IBarDataSet> dataSets = null;
                        ArrayList<BarEntry> valueSet = new ArrayList<>();

                        //now looping through all the elements of the json array
                        for (int i = 0; i < dataCalon.length(); i++) {
                            //getting the json object of the particular index inside the array
                            JSONObject calonObject = dataCalon.getJSONObject(i);

                            //adding the hero to herolist
                            xAxis.add(calonObject.getString("nama_calon"));
                            valueSet.add(new BarEntry(Integer.parseInt(calonObject.getString("poin")),i));
                        }

                        BarDataSet barDataSet = new BarDataSet(valueSet,"Calon");
                        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);

                        dataSets = new ArrayList<>();
                        dataSets.add(barDataSet);

                        YAxis yAxisRight = chart.getAxisRight();
                        yAxisRight.setEnabled(false);

                        BarData data = new BarData(xAxis,dataSets);
                        chart.setExtraLeftOffset(0);
                        chart.setData(data);
                        chart.animateXY(2000,2000);
                        chart.invalidate();

                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        // Do something when error occurred
                        Toast.makeText(getApplicationContext(),  "GAGAL koneksi ke server, mohon cek hostname atau IP Server atau pengiriman data tidak sesuai", Toast.LENGTH_LONG).show();
                        builder.setTitle("GAGAL!");
                        builder.setPositiveButton("oke", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(getIntent());
                                finish();
                            }
                        });
                        builder.setMessage("Terjadi kesalahan, coba lagi");
                        AlertDialog alert1 = builder.create();
                        alert1.show();
                    }
                }
        );
        // Add JsonObjectRequest to the RequestQueue
        requestQueue.add(jsonObjectRequest);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { // method membuat menu
        getMenuInflater().inflate(R.menu.menu_layout, menu); // create menu dari layout xml
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) { // method ketika menu di klik
        Intent mIntent;
        switch (item.getItemId()) {
            case R.id.menuServer: // ketika menu server diklik
                mIntent = new Intent(this, Server.class); // inisialisasi intent class Server
                startActivity(mIntent); // strat intent
                return true;
            case R.id.menuPemilihan:  // ketika menu pemilihan diklik
                mIntent = new Intent(this, ScanCodeActivity.class); // inisialisasi intent class ScanCodeActivity
                startActivity(mIntent); // start intent
                return true;
            case R.id.menuLocation:  // ketika menu location diklik
                mIntent = new Intent(this, MapsActivity.class); // inisialisasi intent class MapsActivity
                startActivity(mIntent); // start intent
                return true;
            case R.id.menuRefresh:  // ketika menu refresh diklik
                finish();
                startActivity(getIntent()); // refesh this intent
                return true;
            default:
                return super.onOptionsItemSelected(item); // item selected
        }
    }

    @Override
    public void onBackPressed() { // ketika back
        super.onBackPressed(); 
        Intent intent = new Intent(getApplicationContext(),MainActivity.class); // inisialisasi intent MainActivity
        startActivity(intent); // start activity intent
        finish();
    }
}

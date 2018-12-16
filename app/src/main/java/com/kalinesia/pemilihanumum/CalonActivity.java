package com.kalinesia.pemilihanumum;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalonActivity extends AppCompatActivity {

    // Deklarasi object
    private  String base_url; // Deklarasi object base_url
    DatabaseHelper dbHelper; // Deklarasi object dbHelper
    protected Cursor cursor; // Deklarasi object cursor
    SharedPreferences pref;    //Deklarasi SharedPreferences
    String kode_pemilih, pin, id_campaign; // Deklarasi object kode_pemilih, pin, id_campaign
    ListView listView; // Deklarasi object listView
    List<Calon> calonList; // Deklarasi object calonList
    ProgressDialog progressDialog; // Deklarasi object progressDialog


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calon); // set content xml layout

        progressDialog = new ProgressDialog(this); // inisialisasi class ProgressDialog
        progressDialog.setMessage("Loading..."); // Set Pesan
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style icon Spinner
        progressDialog.show(); // Display Progress Dialog

        dbHelper = new DatabaseHelper(this); // inisialisasi class DatabaseHelper
        SQLiteDatabase db = dbHelper.getReadableDatabase(); //get method getReadableDatabase
        cursor = db.rawQuery("SELECT * FROM tbl_ip", null); // query get ip server
        cursor.moveToFirst();
        if (cursor.getCount()>0) { // count result
            cursor.moveToPosition(0);
            base_url = cursor.getString(1).toString(); // get data array ke 1 (ip)
        }

        pref = getSharedPreferences("SharedPref", Context.MODE_PRIVATE);
        kode_pemilih =  pref.getString("kode_pemilih",null); // getSharedPreferences kode_pemilih
        pin =  pref.getString("pin",null); // getSharedPreferences pin
        id_campaign =  pref.getString("id_campaign",null); // getSharedPreferences id_campaign
        if((kode_pemilih==null) || (pin==null) || (id_campaign==null)){ // pengecekan value kode pemilih / pin / id_campaign jika kosong
            Toast.makeText(getApplicationContext(),  "Sesi anda telah habis, mohon login kembali", Toast.LENGTH_LONG).show(); // pesan
            Intent intent=new Intent(CalonActivity.this,ScanCodeActivity.class); // Inisialisasi intent ScanCodeActivity
            startActivity(intent); // start intent
            finish();
        }
        //initializing listview and hero list
        listView = (ListView) findViewById(R.id.listCalon); //inisialisasi listView
        calonList = new ArrayList<>(); // inisialisasi
        initviews(); // memanggil method initview
    }

    private void initviews() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this); // insialisasi class AlertDialog
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext()); // get method volley newRequestQueue
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, // metode request
                base_url +"/auth?id="+kode_pemilih+"&pin="+pin, // url request
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) { // respone
                        progressDialog.dismiss(); //  dismiss progess dialog
                        // Process the JSON
                        try{
                            // Get the JSON array
                            String code = response.getString("code"); // get respone json object "code"
                            if(code.toString().equals("200".toString())) { // jika respone code == 200

                                JSONArray calonArray = response.getJSONArray("calon"); // menyimpan respone json object pada variable calonArray
                                //now looping through all the elements of the json array
                                for (int i = 0; i < calonArray.length(); i++) { // looping semua data jsonArray calonArry

                                    JSONObject calonObject = calonArray.getJSONObject(i); //getting the json object of the particular index inside the array

                                    //creating memnaggil class calon dengan mengirim data untuk setter getter
                                    Calon calon = new Calon(base_url, calonObject.getString("no_urut"), calonObject.getString("nama_calon"),calonObject.getString("gambar"),calonObject.getString("visimisi"));

                                    //menambah ke list adapter calon
                                    calonList.add(calon);
                                }

                                //creating custom adapter object
                                final CalonViewAdapter adapter = new CalonViewAdapter(calonList, getApplicationContext());

                                //adding the adapter to listview
                                listView.setAdapter(adapter);

                                listView.setOnItemClickListener(new AdapterView.OnItemClickListener (){
                                    @Override
                                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                                        ViewGroup vg=(ViewGroup)view;

                                        TextView nomor=(TextView)vg.findViewById(R.id.textViewNomor); // inisialisasi textview nomor
                                        TextView nama=(TextView)vg.findViewById(R.id.textViewName); // inisialisasi textview nama
                                        TextView visiMisi=(TextView)vg.findViewById(R.id.textViewVisiMisi); // inisialisasi textview visiMisi

                                        final String nomorSelected = nomor.getText().toString(); // get text nomor
                                        String namaSelected = nama.getText().toString();  // get text nama
                                        String visiMisiSelected = visiMisi.getText().toString();  // get text visiMisi

                                        builder.setTitle("("+nomorSelected+") "+namaSelected); // set title dialog box
                                        builder.setMessage(visiMisiSelected); // set message dialog box
                                        builder.setPositiveButton("Pilih", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) { // membuat button pilih
                                                postRequest(nomorSelected, kode_pemilih, id_campaign); // ketika pilih di klik memanggil method postRequest
                                            }
                                        });

                                        builder.setNeutralButton("Kembali", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) { // membuat button pilih
                                                dialog.dismiss(); // dismiss dialog
                                            }
                                        });
                                        AlertDialog alert1 = builder.create(); // membuat dialog
                                        alert1.show(); // show dialog
                                    }

                                });

                            }else if(code.toString().equals("501".toString())){ // ketika respone 501
                                Toast.makeText(getApplicationContext(),
                                        response.getString("message"),Toast.LENGTH_LONG).show(); // menampilkan Toast pesan
                                Intent intent = new Intent(getApplicationContext(), HasilActivity.class); // inisialisasi intent class HasilActivity
                                startActivity(intent); // start intent
                                finish();
                            }else {
                                String status = response.getString("status"); // get string array status
                                String message = response.getString("message");  // get string array message
                                Toast.makeText(getApplicationContext(),
                                        message,Toast.LENGTH_LONG).show();  // menampilkan toast response message
                                builder.setTitle(status);  // set title dialog
                                builder.setMessage(message); //set message dialog
                                builder.setPositiveButton("Login", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) { // membuat button Login
                                        Intent intent = new Intent(getApplicationContext(), ScanCodeActivity.class); // inisialisasi intent class ScanCodeActivity
                                        startActivity(intent); // start intent
                                        finish();
                                    }
                                });

                                builder.setNeutralButton("Kembali", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) { // membuat button kembali
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);  // inisialisasi intent class MainActivity
                                        startActivity(intent); // start intent
                                        finish();
                                    }
                                });
                                AlertDialog alert1 = builder.create(); // membuat dialog
                                alert1.show(); // menampilkan dialog
                            }

                        }catch (JSONException e){ // jika terjadi kesalahan
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        // jika terjadi error saat request
                        Toast.makeText(getApplicationContext(),  "GAGAL koneksi ke server, mohon cek hostname atau IP Server atau pengiriman data tidak sesuai", Toast.LENGTH_LONG).show();
                        builder.setTitle("GAGAL!"); // start title message
                        builder.setPositiveButton("Oke", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) { // create button "OKE"
                                Intent intent = new Intent(getApplicationContext(), ScanCodeActivity.class); // Inialisasi intent class
                                startActivity(intent); // start intent
                                finish();
                            }
                        });
                        builder.setMessage("Terjadi kesalahan, coba lagi"); // set message dialog
                        AlertDialog alert1 = builder.create(); // create dialog
                        alert1.show(); // menampilkan dialog
                    }
                }
        );
        // Add JsonObjectRequest to the RequestQueue
        requestQueue.add(jsonObjectRequest); // mengirim request
    }

    public void postRequest(final String no_urut, final String id_pemilih, final String camp){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this); // deklarasi object alertdialog

        progressDialog = new ProgressDialog(this); // inisialisasi proges bar
        progressDialog.setMessage("Loading..."); // Setting Message
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
        progressDialog.show(); // Display Progress Dialog


        Map<String, String> params =  new HashMap<>(); //inisialisasi hashmap pengiriman data
        params.put("no_urut", no_urut); // put no_urut
        params.put("id_pemilih", id_pemilih); // put id_pemilih
        params.put("id_campaign", camp); // put id_campaign
        JSONObject jsonObj = new JSONObject(params); // inisialisasi json onject
        // Initialize a new JsonObjectRequest instance
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST, // request method post
                base_url +"/prosesPoling", // url
                jsonObj, // mengirim data json
                new Response.Listener<JSONObject>() { // response
                    @Override
                    public void onResponse(JSONObject response) { // response json object
                        progressDialog.dismiss(); // dismiss progress bar
                        // Process the JSON
                        try{ // memcoba membaca json objecr
                            // Get the JSON array
                            String code = response.getString("code"); // get string code dalam json
                            if(code.toString().equals("200".toString())) { // jika data code == 200
                                Toast.makeText(getApplicationContext(),  "Sukses melakukan pemilihan, Terimakasih telah ikut serta berpartisipasi", Toast.LENGTH_LONG).show(); // menampilkan toast
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class); // inisialisasi intent MainActivity
                                startActivity(intent); // startintent
                                finish();
                            }else {
                                Toast.makeText(getApplicationContext(),  "Terjadi Kesalahan", Toast.LENGTH_LONG).show(); // menampilkan pesan toast
                                builder.setTitle("GAGAL!"); // set title dialog
                                builder.setPositiveButton("oke", new DialogInterface.OnClickListener() { // set button posotive "oke"
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) { // ketika button oke event klik
                                        dialog.dismiss(); // dismiss dialog
                                    }
                                });
                                builder.setMessage("Terjadi kesalahan, coba lagi"+response.toString()); // set pesan pada dialog
                                AlertDialog alert1 = builder.create(); // membuat dialog
                                alert1.show(); // menampilkan dialog
                            }

                        }catch (JSONException e){ // ketika ada kesalahan
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener(){ // ketika ada respon error dari server
                    @Override
                    public void onErrorResponse(VolleyError error){
                        // Do something when error occurred
                  Toast.makeText(getApplicationContext(),  "GAGAL koneksi ke server, mohon cek hostname atau IP Server atau pengiriman data tidak sesuai", Toast.LENGTH_LONG).show(); // menampilkan toast
                    builder.setTitle("GAGAL!"); // set title dialog
                    builder.setPositiveButton("oke", new DialogInterface.OnClickListener() { // set/create button "oke"
                        @Override
                        public void onClick(DialogInterface dialog, int which) { // ketika ada event klik
                            dialog.dismiss(); // dismiss dialog
                        }
                    });
                    builder.setMessage("Terjadi kesalahan, coba lagi"); // set pesan pada dialog
                    AlertDialog alert1 = builder.create(); // membuat dialog
                    alert1.show(); // menampilkan dialog
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

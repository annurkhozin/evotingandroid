package com.kalinesia.pemilihanumum;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    EditText edt_kode_pemilih, edt_pin; //Deklarasi EditText
    TextInputLayout txtKodePemilih, txtPin; //Deklarasi TextInputLayout
    Button buttonLogin; // Deklarasi Button
    DatabaseHelper dbHelper; // Deklarasi databasehelper
    protected Cursor cursor; // Deklarasi cursor
    SharedPreferences pref,sharedpreferences; // Deklarasi SharedPreferences
    String base_url, kode_pemilih, pin, id_campaign; // deklarasi object string
    ProgressDialog progressDialog; // deklarasi progress dialog

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // set content xml layout

        progressDialog = new ProgressDialog(this); // inisialisasi class progress dialog
        final AlertDialog.Builder builder = new AlertDialog.Builder(this); // inisialisasi class alert dialog
        dbHelper = new DatabaseHelper(this); // inisialisasi class database helper
        SQLiteDatabase db = dbHelper.getReadableDatabase(); // get method getreadabledatabase
        cursor = db.rawQuery("SELECT * FROM tbl_ip", null); // query get ip
        cursor.moveToFirst(); 
        if (cursor.getCount()>0) { // ketika terdapat result
            cursor.moveToPosition(0);
            base_url = cursor.getString(1).toString(); // get string dengan object array ke 1
        }

        pref = getSharedPreferences("SharedPref", Context.MODE_PRIVATE); // get class getSharedPreferences
        kode_pemilih =  pref.getString("kode_pemilih",null); // get string kode_pemilih
        pin =  pref.getString("pin",null); // get string pin
        id_campaign =  pref.getString("id_campaign",null); // get string id_campaign
        if((kode_pemilih!=null) && (pin!=null) && (id_campaign!=null)){ // ketika data tidak kosong / masih mempunyai sesi
            Toast.makeText(getApplicationContext(),  "Mengecek status akun anda", Toast.LENGTH_LONG).show(); // menampilkan pesan toast

            Intent intent=new Intent(LoginActivity.this,CalonActivity.class); // inisialisasi intent CalonActivity
            startActivity(intent); // start intent
            finish();
        }
        
        // inisialisasi object
        edt_kode_pemilih = (EditText) findViewById(R.id.kode_pemilih); // inisialisasi EditText kode pemilih
        edt_pin = (EditText) findViewById(R.id.pin);  // inisialisasi EditText pin
        txtKodePemilih = (TextInputLayout) findViewById(R.id.txtKodePemilih);  // inisialisasi TextInputLayout txtKodePemilih
        txtPin = (TextInputLayout) findViewById(R.id.txtPin); // inisialisasi TextInputLayout txtPin
        buttonLogin = (Button) findViewById(R.id.buttonLogin); // inisialisasi button login

        buttonLogin.setOnClickListener(new View.OnClickListener() { // ketika ada event klik
            @Override
            public void onClick(View view) { //set click event of login button

                //Check user input is correct or not
                if (validate()) {
                    //Get values from EditText fields
                    String Kode_pemilih = edt_kode_pemilih.getText().toString();
                    String Pin = edt_pin.getText().toString();

                    progressDialog.setMessage("Loading..."); // Setting Message
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
                    progressDialog.show(); // Display Progress Dialog

                    // Initialize a new RequestQueue instance
                    RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                    // Initialize a new JsonObjectRequest instance
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                            Request.Method.GET, // request method
                            base_url +"/auth?id="+Kode_pemilih+"&pin="+Pin, // url
                            null,
                            new Response.Listener<JSONObject>() { // respon json object
                                @Override
                                public void onResponse(JSONObject response) { // ketika terdapat respone
                                    progressDialog.dismiss(); // dismiss progress dialog
                                    // Process the JSON
                                    try{ // memcoba membaca json
                                        // Get the JSON array
                                        String code = response.getString("code"); // get respon code
                                        if(code.toString().equals("200".toString())) { // jika respon code = 200
                                            String kode_pemilih = response.getString("kode_pemilih"); // get respon kode_pemilih
                                            String pin = response.getString("pin"); // get respon pin
                                            String id_campaign = response.getString("id_campaign"); // get respon id_campaign
                                            String nama_pemilih = response.getString("nama_pemilih"); // get respon nama_pemilih

                                            Toast.makeText(getApplicationContext(),
                                                    "Selamat datang "+nama_pemilih,Toast.LENGTH_LONG).show(); // menampilkan toast
                                            sharedpreferences = getSharedPreferences("SharedPref", Context.MODE_PRIVATE); // call & get getSharedPreferences
                                            SharedPreferences.Editor editor = sharedpreferences.edit(); // mengedit data SharedPreferences
                                            editor.putString("kode_pemilih", kode_pemilih); // put data kode pemilih
                                            editor.putString("pin", pin); // put data pin
                                            editor.putString("id_campaign", id_campaign); // put data id_campaign
                                            editor.putString("nama_pemilih", nama_pemilih); // put data nama_pemilih
                                            editor.commit(); // commit data
                                            Intent hal = new Intent(getApplicationContext(),CalonActivity.class); // inisialisasi intent CalonActivity
                                            startActivity(hal); // start intent
                                            finish();
                                        }else if(code.toString().equals("501".toString())){ // ketika respon code == 501

                                            String kode_pemilih = response.getString("kode_pemilih"); // get respon kode_pemilih
                                            String pin = response.getString("pin"); // get respon pin
                                            String id_campaign = response.getString("id_campaign"); // get respon id_campaign
                                            String nama_pemilih = response.getString("nama_pemilih"); // get respon nama_pemilih

                                            sharedpreferences = getSharedPreferences("SharedPref", Context.MODE_PRIVATE);
                                            SharedPreferences.Editor editor = sharedpreferences.edit(); // mengedit data SharedPreferences
                                            editor.putString("kode_pemilih", kode_pemilih); // put data kode pemilih
                                            editor.putString("pin", pin); // put data pin
                                            editor.putString("id_campaign", id_campaign); // put data id_campaign
                                            editor.putString("nama_pemilih", nama_pemilih); // put data nama_pemilih
                                            editor.commit(); // commit data
                                            Toast.makeText(getApplicationContext(),nama_pemilih+", "+
                                                    response.getString("message"),Toast.LENGTH_LONG).show(); // menampilkan pesan toast
                                            Intent intent = new Intent(getApplicationContext(), HasilActivity.class); // inisialisasi intent HasilActivity
                                            startActivity(intent); // start intent
                                            finish();
                                        }else { // ketika respon selai yang diatas
                                            String status = response.getString("status"); // get status respon
                                            String message = response.getString("message"); // get pesan respon
                                            Toast.makeText(getApplicationContext(),
                                                    message,Toast.LENGTH_LONG).show(); // menampilkan pesan toast
                                            builder.setTitle(status); // set title dialog
                                            builder.setMessage(message); // set pesan dialog
                                            builder.setPositiveButton("Coba Lagi", new DialogInterface.OnClickListener() { // crete positive button "coab lagi"
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) { // ketika event klik
                                                    dialog.dismiss(); // dismiss dialog
                                                }
                                            });
                                            AlertDialog alert1 = builder.create(); // membuat dialog
                                            alert1.show(); // menampilkan dialog
                                        }

                                    }catch (JSONException e){ // ketika terdapat error dalam server
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
                                builder.setPositiveButton("oke", new DialogInterface.OnClickListener() { // set atau create button "oke"
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
            }
        });

    }

    //This method is used to validate input given by user
    public boolean validate() {
        boolean valid = false;

        //Get values from EditText fields
        String Kode_pemilih = edt_kode_pemilih.getText().toString();
        String Pin = edt_pin.getText().toString();

        //Handling validation for Password field
        if(Kode_pemilih.isEmpty()) {
            valid = false;
            txtKodePemilih.setError("Kode pememilih kosong!");
        }else {
            valid = true;
            txtKodePemilih.setError(null);
        }

        //Handling validation for Password field
        if (Pin.isEmpty()) {
            valid = false;
            txtPin.setError("PIN kosong!");
        } else {
            valid = true;
        }
        return valid;
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

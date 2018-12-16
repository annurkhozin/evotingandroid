package com.kalinesia.pemilihanumum;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.Result;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScanCodeActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView scannerView; // deklarasi object ZXingScannerView
    DatabaseHelper dbHelper; // deklarasi DatabaseHelper
    protected Cursor cursor; // deklarasi cursor
    SharedPreferences pref,sharedpreferences;    //Declaration SharedPreferences
    String base_url, kode_pemilih, pin, id_campaign; // deklarasi string
    ProgressDialog progressDialog; //deklarasi ProgressDialog

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_code); // set content layout xml

        permissionCheck(); // get method permissionCheck

        scannerView = new ZXingScannerView(this); // inisialisasi clas ZXingScannerView
        setContentView(scannerView);
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

            Intent intent=new Intent(ScanCodeActivity.this,CalonActivity.class); // inisialisasi intent CalonActivity
            startActivity(intent); // start intent
            finish();
        }
    }

    @Override
    public void onResume() { // onresume
        super.onResume();
        if(scannerView == null) {
            scannerView = new ZXingScannerView(this); // inisialisasi class ZXingScannerView
            setContentView(scannerView); // set content
        }
        scannerView.setResultHandler(this); 
        scannerView.startCamera(); // start camera ulang
        scannerView.setAutoFocus(true); // auto focus true
    }

    @Override
    public void onDestroy() { // ondestroy
        super.onDestroy(); 
        scannerView.stopCamera();// stop camera
    }

    @Override
    public void handleResult(final Result result) { // menghandle hasil scan
        final AlertDialog.Builder builder = new AlertDialog.Builder(this); // inisialisasi class object alrt dialog
        MediaPlayer.create(this, R.raw.sound).start(); // play media player mp3

        final String[] splitLable = result.getText().split("_"); // split hasil scan dengan _
        if(splitLable.length!=2){ // jika panjang != 2
            Toast.makeText(getApplicationContext(),
                    "Barcode tidak sesuai",Toast.LENGTH_LONG).show(); // menampilkan toast
            scannerView.resumeCameraPreview(ScanCodeActivity.this); // resume camera
        }else{
            progressDialog = new ProgressDialog(this); //inisialisasi class ProgressDialog
            progressDialog.setMessage("Loading..."); // Setting Message
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
            progressDialog.show(); // Display Progress Dialog

            // Initialize a new RequestQueue instance
            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            // Initialize a new JsonObjectRequest instance
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.GET, // request method GET
                    base_url +"/barcodeAuth?id="+splitLable[0]+"&pin="+splitLable[1], // url
                    null,
                    new Response.Listener<JSONObject>() { // respon
                        @Override
                        public void onResponse(JSONObject response) { //
                            progressDialog.dismiss(); // dismiss progressDialog

                            try{ // mencoba membaca respon json
                                // Get the JSON array
                                String code = response.getString("code");
                                if(code.toString().equals("200".toString())) { // jika respon code == 200
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
                                }else if(code.toString().equals("501".toString())){ // jika respone code == 501

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
                                    builder.setPositiveButton("Coba Lagi", new DialogInterface.OnClickListener() {  //create button coba lagi
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) { // ketika event klik
                                            scannerView.resumeCameraPreview(ScanCodeActivity.this); // resume camera
                                        }
                                    });

                                    builder.setNeutralButton("Kembali", new DialogInterface.OnClickListener() { // create button kembali
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) { // ketika event onclick
                                            Intent intent = new Intent(getApplicationContext(), MainActivity.class); // inisialisasi intent MainActivity
                                            startActivity(intent); // start intent
                                            finish();
                                        }
                                    });
                                    AlertDialog alert1 = builder.create(); // membuat dialog
                                    alert1.show(); // menmapilkan dialog
                                }

                            }catch (JSONException e){ // jika terdapat error pada json
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

    private boolean permissionCheck() {
        String[] PERMISSIONS = new String[]{"android.permission.CAMERA"}; // string permission camera
        if (!hasPermissions(this, PERMISSIONS)) { // get method hasPermissions jika return false
            ActivityCompat.requestPermissions(this, PERMISSIONS, 1); 
        }
        return hasPermissions(this, PERMISSIONS); 
    }

    public boolean hasPermissions(Context context, String[] permissions) {
        if (!(Build.VERSION.SDK_INT <  Build.VERSION_CODES.M || context == null || permissions == null)) { // jika versi sdk < versi bulid
            for (String permission : permissions) { // for permission apa saya yang ingin di cek
                if (ContextCompat.checkSelfPermission(context, permission) != 0) { // jika permissin false
                    return false; // return false
                }
            }
        }
        return true;
    }
}

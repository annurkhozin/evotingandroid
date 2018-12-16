package com.kalinesia.pemilihanumum;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    CardView cardViewlogin, cardViewscan, cardViewhasil, cardViewserver; // deklarasi CardView
    //Declaration SharedPreferences
    SharedPreferences pref;
    Button buttonLogout; //Declaration Button
    String kode_pemilih, pin, id_campaign, user_pemilih; // deklarasi string

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permissionCheck();

        // inisialisasi object 
        cardViewlogin = (CardView) findViewById(R.id.cardviewlogin); // inisialisasi object cardViewlogin
        cardViewscan = (CardView) findViewById(R.id.cardviewscan); // inisialisasi object cardViewscan
        cardViewhasil = (CardView) findViewById(R.id.cardviewhasil); // inisialisasi object cardViewhasil
        cardViewserver = (CardView) findViewById(R.id.cardviewserver); // inisialisasi object cardViewserver

        cardViewlogin.setOnClickListener(this); // ketika event onclik
        cardViewscan.setOnClickListener(this);  // ketika event onclik
        cardViewhasil.setOnClickListener(this);  // ketika event onclik
        cardViewserver.setOnClickListener(this);  // ketika event onclik

        LinearLayout login = (LinearLayout) findViewById(R.id.login); // inisialisasi object LinearLayout login
        LinearLayout logedin = (LinearLayout) findViewById(R.id.logedin);  // inisialisasi object LinearLayout logedin
        TextView lblPemilih = (TextView) findViewById(R.id.fullName);  // inisialisasi object TextView lblPemilih
        buttonLogout = (Button) findViewById(R.id.buttonLogout);  // inisialisasi object Button buttonLogout
        buttonLogout.setOnClickListener(new View.OnClickListener() { // ketika event onclik
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = pref.edit(); // get method  SharedPreferences edit
                editor.clear(); // menghapus data pada SharedPreferences
                editor.commit(); // commit data SharedPreferences
                //User Logged in Successfully Launch You home screen activity
                Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent); // start intent
                finish();
            }
        });
        pref = getSharedPreferences("SharedPref", Context.MODE_PRIVATE); // get getSharedPreferences
        kode_pemilih =  pref.getString("kode_pemilih",null); // get data kode pemilih
        pin =  pref.getString("pin",null); // get data pin
        id_campaign =  pref.getString("id_campaign",null); // get data id_campaign
        user_pemilih =  pref.getString("nama_pemilih",null); // get data nama_pemilih
        lblPemilih.setText(user_pemilih); // set text lblPemilih dengan user_pemilih
        if((kode_pemilih!=null) && (pin!=null) && (id_campaign!=null)){ // jika masih terdapat sesi
            buttonLogout.setVisibility(View.VISIBLE); //visible button logout true / visible
            login.setVisibility(View.GONE);  //visible button login false / gone
            logedin.setVisibility(View.VISIBLE);  //visible button logedin true / visible
            lblPemilih.setVisibility(View.VISIBLE);  //visible button lblPemilih true / visble
        }

    }

    @Override
    public void onClick(View view) { // onclick menu card
        Intent intent; // deklarasi object intent

        switch (view.getId()){
            case R.id.cardviewlogin: intent = new Intent(this,LoginActivity.class); // jika yg dipilih menu cardviewlogin, inisialisasi intent LoginActivity
                startActivity(intent); break; // start intent
            case R.id.cardviewscan: intent = new Intent(this,ScanCodeActivity.class);  // jika yg dipilih menu ScanCodeActivity, inisialisasi intent LoginActivity
                startActivity(intent); break;// start intent
            case R.id.cardviewhasil: intent = new Intent(this,HasilActivity.class);  // jika yg dipilih menu HasilActivity, inisialisasi intent LoginActivity
                startActivity(intent); break;// start intent
            case R.id.cardviewserver: intent = new Intent(this,ScanCodeActivity.class);  // jika yg dipilih menu ScanCodeActivity, inisialisasi intent LoginActivity
                startActivity(intent); break;// start intent
            default:break;
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

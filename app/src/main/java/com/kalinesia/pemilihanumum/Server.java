package com.kalinesia.pemilihanumum;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Server extends AppCompatActivity implements View.OnClickListener {

    DatabaseHelper dbHelper; // deklarasi DatabaseHelper
    protected Cursor cursor; // deklarasi Cursor
    TextView txtIp; // deklarasi txtIp
    EditText edtIp; // deklarasi edtIp
    Button btnSave; // deklarasi btnSave

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server); // set content layout xml

        btnSave = (Button) findViewById(R.id.btnSave); // inisialisasi button 
        btnSave.setOnClickListener(this); // ketika event onclik
        txtIp = (TextView) findViewById(R.id.txtIp); // inisialisasi TextView 
        edtIp = (EditText) findViewById(R.id.edtIp); // inisialisasi EditText 

        dbHelper = new DatabaseHelper(this); // inisialisasi DatabaseHelper 
        SQLiteDatabase db = dbHelper.getReadableDatabase(); // get method getReadableDatabase pada class DatabaseHelper
        cursor = db.rawQuery("SELECT * FROM tbl_ip", null); // query get ip
        cursor.moveToFirst();

        if (cursor.getCount()>0) { // jika terdapat result
            cursor.moveToPosition(0);
            txtIp.setText(cursor.getString(1).toString()); // get & set txtIp dengan array ke 1
            edtIp.setText(cursor.getString(1).toString()); // get & set edtIp dengan array ke 1
        }
    }

    @Override
    public void onClick(View view) {
        SQLiteDatabase db = dbHelper.getWritableDatabase(); // open connecton get method getWritableDatabase
        db.execSQL("update tbl_ip set ip='"+ edtIp.getText().toString()+"'"); // query updatedata ip
        Toast.makeText(getApplicationContext(), "Berhasil Update", Toast.LENGTH_LONG).show(); // menampilkan toast berhasil update
        onBackPressed(); // back
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

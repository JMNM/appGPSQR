/*
    Copyright (C) 2016  José Miguel Navarro Moreno and José Antonio Larrubia García

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.example.appgpsqr;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Button;
import android.content.Intent;
import java.util.ArrayList;
import java.util.Locale;
import android.net.Uri;
import android.widget.Toast;

/**
 * Aplicación que lee unas coordenadas de un código QR y mustra la ruta en google maps,
 * una vez terminada la ruta muestra el recorrido realizado.
 */
public class MainActivity extends AppCompatActivity implements LocationListener {
    private TextView x;
    private TextView y;
    private Button bRuta;
    private Button mRecorrido;
    private double latitud;
    private double longitud;
    private ArrayList<Location> recorrido;
    Intent intent;
    LocationManager lm;
    String proveedor;

    /**
     * Crea la aplicación
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new IntentIntegrator(MainActivity.this).initiateScan();
            }
        });
        y = (TextView) findViewById(R.id.textView);
        x = (TextView) findViewById(R.id.textView2);
        bRuta= (Button) findViewById(R.id.button);
        bRuta.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickButton();
            }
        });
        mRecorrido =(Button) findViewById(R.id.buttonRecorrido);
        mRecorrido.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarRecorrido();
            }
        });
        recorrido=new ArrayList<Location>();

        lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        criteria.setAccuracy(criteria.ACCURACY_FINE);

        proveedor = lm.getBestProvider(criteria, true);
    }

    /**
     * Detruye la aplicación
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * Reanuda la aplicación
     */
    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * Pausa la aplicación
     */
    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Una vez leido el código QR procesa los resultados.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case IntentIntegrator.REQUEST_CODE: {
                if (resultCode == RESULT_CANCELED) {
                } else {
                    //Recogemos los datos que nos envio el código Qr
                    IntentResult scanResult = IntentIntegrator.parseActivityResult(
                            requestCode, resultCode, data);
                    String coordenadas = scanResult.getContents();
                    //partimos el string del código QR por "_" y nos quedamos con el segundo elemento
                    //y el último que seran los números de las coordenadas.
                    String[] res = coordenadas.split("_");
                    if (res.length >= 4) {
                        latitud = Double.parseDouble(res[1]);
                        longitud = Double.parseDouble(res[3]);
                    }
                    x.setText(latitud + "");
                    y.setText(longitud + "");

                }
                break;
            }
        }
    }

    /**
     * Captura los eventos de cambio de localización y guarda los puntos en un array,
     * para mostrarlos al final.
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(this, getString(R.string.geo_desactivada), Toast.LENGTH_LONG).show();
            return;
        }
        Location pos = lm.getLastKnownLocation(proveedor);
        recorrido.add(pos);

        String uri = String.format(Locale.ENGLISH, "https://www.google.com/maps/dir/%f,%f/%f,%f/", pos.getLatitude(), pos.getLongitude(), latitud, longitud);

        intent.setData(Uri.parse(uri));

        Log.d("LOGTAG", "Lista: " + pos.getLatitude() + " " + pos.getLongitude());
    }

    /**
     * Método que se ejecuta al pulsar el botón de mostrar ruta, que se encarga de mostrar el mapa con la ruta.
     */
    private void clickButton(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(this, getString(R.string.geo_desactivada), Toast.LENGTH_LONG).show();
            return;
        }
        Location pos = lm.getLastKnownLocation(proveedor);
        recorrido.clear();
        recorrido.add(pos);
        //String uri = String.format(Locale.ENGLISH, "geo:%f,%f", latitud, longitud);

        //String uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?saddr=%f,%f&daddr=%f,%f", pos.getLatitude(), pos.getLongitude(),  latitud, longitud);
        String uri = String.format(Locale.ENGLISH, "https://www.google.com/maps/dir/%f,%f/%f,%f/", pos.getLatitude(), pos.getLongitude(), latitud, longitud);

        intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
        this.startActivity(intent);

    }

    /**
     * Metodo para mostrar el recorrido realizado.
     */
    private void mostrarRecorrido(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            Toast.makeText(this, getString(R.string.geo_desactivada), Toast.LENGTH_LONG).show();
            return;
        }
        Location pos = lm.getLastKnownLocation(proveedor);
        recorrido.add(pos);
        String direccion="https://www.google.com/maps/dir/";
        for(Location l:recorrido){
            direccion += l.getLatitude()+","+l.getLongitude()+"/";

        }
        String uri = String.format(Locale.ENGLISH, direccion);
        intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
        this.startActivity(intent);

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}

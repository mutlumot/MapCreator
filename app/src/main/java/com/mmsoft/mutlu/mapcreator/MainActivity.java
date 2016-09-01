package com.mmsoft.mutlu.mapcreator;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.annotations.Sprite;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.annotations.SpriteFactory;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private MapView mapView = null;

    GPSTracker gps;
    private int son_sefer = 0;
    private int sefer = 0;

   //private Database dtbs = new Database(this);
    private DatabaseHelper dtbs1 = new DatabaseHelper(this);
    Handler handler = new Handler();
    final ArrayList<LatLng> noktalar = new ArrayList<LatLng>();


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button basla = (Button) findViewById(R.id.button);
        final Button dur = (Button) findViewById(R.id.button2);


        //Tabani kaldirmak için
        // mapView.setStyleUrl("mapbox://styles/mutlumotugan/cih6njwea00feaxm4k4jlyobh");
        mapView = (MapView) findViewById(R.id.mapboxMapView);
        mapView.setZoomLevel(17);
        mapView.onCreate(savedInstanceState);

        mapView.setStyleUrl(Style.MAPBOX_STREETS);


        gps = new GPSTracker(MainActivity.this);
        final ArrayList<LatLng> dizi = new ArrayList<LatLng>();

        gps.getLocation();
        //Double mlat = gps.getLatitude();
        //Double mlon = gps.getLongitude();
       // mapView.setCenterCoordinate(new LatLng(mlat,mlon));
        mapView.setCenterCoordinate(new LatLng(40.77529308420188, 29.417492151260376));


        GetAllTables();
        FindCommonPoints();



        basla.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                 son_sefer = getMaxColumnData();
                 startRepeatingTask();

            }
        });


        dur.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gps.stopUsingGPS();
                stopRepeatingTask();
                //dtbs1.closeCon();

                //MainActivity.this.finish();
            }
        });


    }

    public int getMaxColumnData() {
        SQLiteDatabase Db1 = dtbs1.getReadableDatabase();
        final SQLiteStatement stmt = Db1.compileStatement("SELECT max(seferno) from line");

        return (int) stmt.simpleQueryForLong();
    }

    public  int getSeferPointCount(int i){
        SQLiteDatabase db = dtbs1.getReadableDatabase();
        final SQLiteStatement stmt1 = db.compileStatement("Select count(*) from line where seferno="+i);

        return (int) stmt1.simpleQueryForLong();

    }

    public Runnable Updater = new Runnable() {
        double latitude ;
        double longitude ;

        @Override
        public void run() {
            sefer = son_sefer+1;
            gps.getLocation();
            if (gps.canGetLocation()) {
                latitude = gps.getLatitude();
                longitude = gps.getLongitude();
                //pop-up bildirim
                Toast.makeText(getApplicationContext(), "Konumunuz (sefer "+sefer+") - \nEnlem: " + latitude + "\nBoylam: " + longitude, Toast.LENGTH_LONG).show();
                LatLng latLng2 = new LatLng(latitude, longitude);
                noktalar.add(latLng2);
                mapView.setCenterCoordinate(new LatLng(latitude, longitude));

                dtbs1.insertPoint("line",sefer,latitude,longitude);

            } else {
                // Konuma ulasilamiyor..!
                // GPS veya Network kullanilabilir degil..!
                // Ayarlari aktiflestirmesi için kullanici ile iletisime geç.
                gps.showSettingsAlert();
            }
            new DrawGeoJSON().onPostExecute(noktalar);
            handler.postDelayed(Updater, 5000); // 5 seconds
        }

    };

    public void GetAllTables(){
        SQLiteDatabase db = dtbs1.getReadableDatabase();
        String[] SELECT = {"id","seferno", "latitude","longitude","kavsakno"};
        ArrayList<LatLng> allpoints = new ArrayList<LatLng>();


        for(int sayi=1 ; sayi <= getMaxColumnData(); sayi++) {
            Cursor c = db.rawQuery("SELECT * FROM line WHERE seferno =" + sayi, null);
            if (c.moveToFirst()) {
                while (!c.isAfterLast()) {
                    //Toast.makeText(MainActivity.this, " seferno = " + c.getInt(1) + "=> latitude " + c.getDouble(2) +" - longitude "+c.getDouble(3), Toast.LENGTH_LONG).show();
                    LatLng locats = new LatLng(c.getDouble(2), c.getDouble(3));
                    allpoints.add(locats);
                    c.moveToNext();
                }
            }
            new DrawGeoJSON().onPostExecute(allpoints);
            allpoints.clear();
        }
        /*
        int tablo_sayi = tables.size();

        for (int s=3; s<tablo_sayi; s++) {
            String s2 = tables.get(s).toString();

            Cursor c1 = db.query(s2, SELECT, null, null, null, null, null);
            if (c1.moveToFirst()) {
                while (!c1.isAfterLast()) {

                    LatLng locats = new LatLng(c1.getDouble(2), c1.getDouble(3));
                    allpoints.add(locats);

                    c1.moveToNext();
                }
            }
            new DrawGeoJSON().onPostExecute(allpoints);
            allpoints.clear();
         }
        */

    }


    public void FindCommonPoints(){
        Double xa=0.0;Double xb=0.0;Double xc=0.0;Double xd=0.0;
        Double ya=0.0;Double yb=0.0;Double yc=0.0;Double yd=0.0;
        SQLiteDatabase dat = dtbs1.getReadableDatabase();
        String[] SELECT = {"id","seferno", "latitude","longitude","kavsakno"};

        final ArrayList<LatLng> noktalar2 = new ArrayList<LatLng>();

            for(int i=1; i<=getMaxColumnData(); i++) {
                Cursor c = dat.rawQuery("Select * from line where seferno=" +i, null);
                c.moveToFirst();
                    for (int j = 1; j <= getMaxColumnData(); j++) {
                        if (i != j) {

                         for (int p=0; p < getSeferPointCount(i)-1; p++) {
                             Cursor c1 = dat.rawQuery("Select * from line where seferno=" + j, null);
                             c1.moveToFirst();
                             c.moveToPosition(p);

                             while (!c.isLast()) {
                                 xa = c.getDouble(2);
                                 ya = c.getDouble(3);
                                 c.moveToNext();
                                 xb = c.getDouble(2);
                                 yb = c.getDouble(3);

                                 while (!c1.isLast()) {
                                     xc = c1.getDouble(2);
                                     yc = c1.getDouble(3);
                                     c1.moveToNext();
                                     xd = c1.getDouble(2);
                                     yd = c1.getDouble(3);

                                     Double xk = 0.0;
                                     Double yk = 0.0;

                                     xk = ((yd * xc - xd * yc) / (xc - xd) - (yb * xa - xb * ya) / (xa - xb)) / ((ya - yb) / (xa - xb) - (yc - yd) / (xc - xd));
                                     yk = (((yd * xc - xd * yc) / (xc - xd) - (yb * xa - xb * ya) / (xa - xb)) / ((ya - yb) / (xa - xb) - (yc - yd) / (xc - xd))) * ((ya - yb) / (xa - xb)) + ((yb * xa - xb * ya) / (xa - xb));

                                     // Toast.makeText(MainActivity.this, " : : : : \n " + xa + "   " + ya + " : : : : \n " + xb + "   " + yb + " : : : : \n " + xc + "   " + yc + " : : : : \n " + xd + "   " + yd + "   ", Toast.LENGTH_LONG).show();

                                     try {

                                         FileOutputStream fileout = openFileOutput("mytextfile.txt", MODE_APPEND);
                                         OutputStreamWriter outputWriter = new OutputStreamWriter(fileout);

                                         outputWriter.write(" : : : : \n " + xa + "   " + ya + " : : : : \n " + xb + "   " + yb + " : : : : \n " + xc + "   " + yc + " : : : : \n " + xd + "   " + yd + "   ");

                                         outputWriter.close();

                                         //display file saved message

                                     } catch (Exception e) {
                                         e.printStackTrace();
                                     }

                                 }

                             }
                         }
                    }

                }


        }
        Toast.makeText(getBaseContext(), "File saved successfully!",
                Toast.LENGTH_SHORT).show();






/*
        ArrayList<LatLng> road1 = new ArrayList<>();
        ArrayList<LatLng> road2 = new ArrayList<>();
        ArrayList<LatLng> road3 = new ArrayList<>();

        LatLng point1 = new LatLng(40.775321520639196,29.41682159900665);
        road1.add(point1);

        LatLng point3 = new LatLng(40.77530527124792,29.417288303375248);
        road1.add(point3);

        LatLng point5 = new LatLng(40.77528902185268,29.417647719383236);
        road1.add(point5);

        LatLng point7 = new LatLng(40.77530120889949,29.417985677719116);
        road1.add(point7);

        LatLng point11 = new LatLng(40.77478935100893,29.41758871078491);
        road2.add(point11);

        LatLng point33 = new LatLng(40.775102153521765,29.417583346366882);
        road2.add(point33);

        LatLng point55 = new LatLng(40.77545557796549,29.417551159858704);
        road2.add(point55);

        LatLng point77 = new LatLng(40.77573994109547,29.417486786842343);
        road2.add(point77);

        LatLng point88 = new LatLng(40.77681238537704,29.417213201522827);
        road2.add(point88);
        LatLng point99 = new LatLng(40.77768170250654,29.416944980621334);
        road2.add(point99);
        LatLng point00 = new LatLng(40.77808792156432,29.416730403900146);
        road2.add(point00);

        LatLng point111 = new LatLng(40.77768982691204,29.41663920879364);
        road3.add(point111);

        LatLng point333 = new LatLng(40.77774669772271,29.41685914993286);
        road3.add(point333);

        LatLng point555 = new LatLng(40.77790106110624,29.41731512546539);
        road3.add(point555);

       // new DrawGeoJSON().onPostExecute(road1);
       // new DrawGeoJSON().onPostExecute(road2);
       // new DrawGeoJSON().onPostExecute(road3);



        DİZİ İÇİNDE DİZİ TANIMLAMA



        ArrayList<ArrayList<LatLng>> roads = new ArrayList<>();

        roads.add(road1);
        roads.add(road2);
        roads.add(road3);

        double s = roads.get(1).get(2).getLatitude();



        Double t = 0.0;
        Double z = 0.0;
        Double sapma = 0.00018;

            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    t = Math.abs(road2.get(j).getLatitude() - road1.get(i).getLatitude());
                    z = Math.abs(road2.get(j).getLongitude() - road1.get(i).getLongitude());
                    if (t <= sapma && z <= sapma) {
                        Toast.makeText(MainActivity.this, "eşit nokta bulundu marker şu noktalar arasında " + road1.get(i) + road2.get(j), Toast.LENGTH_LONG).show();
                        double d = road1.get(i).getLatitude() - t / 2;
                        double e = road1.get(i).getLongitude() - z / 2;
                        mapView.addMarker(new MarkerOptions()
                                .position(new LatLng(d, e)));
                    }

                }

            }
*/

    /*    Double xa = 40.775321520639196;
        Double ya = 29.41682159900665;

        Double xb = 40.77530120889949;
        Double yb = 29.417985677719116;

        Double xc = 40.77478935100893;
        Double yc = 29.41758871078491;

        Double xd = 40.77573994109547;
        Double yd = 29.417486786842343;

        Double xk = 0.0;
        Double yk = 0.0;

        xk = ( (yd*xc-xd*yc) / (xc-xd) - (yb*xa-xb*ya) / (xa-xb) ) / ( (ya-yb) / (xa-xb) - (yc-yd) / (xc-xd) );
        yk = ( ((yd*xc-xd*yc) / (xc-xd) - (yb*xa-xb*ya) / (xa-xb)) / ((ya-yb) / (xa-xb) - (yc-yd) / (xc-xd)) ) * ((ya-yb) / (xa-xb)) + ((yb*xa-xb*ya) / (xa-xb));

        Toast.makeText(MainActivity.this, "kav;ak nokta bulundu marker şu noktalar arasında " +xk+"   "+yk, Toast.LENGTH_LONG).show();

        mapView.addMarker(new MarkerOptions()
                .position(new LatLng(xk, yk)));

*/



    }


    void startRepeatingTask()
    {
        Updater.run();
    }

    void stopRepeatingTask()
    {
        handler.removeCallbacks(Updater);
        noktalar.clear();


       // dtbs.close();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause()  {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
    private class DrawGeoJSON extends AsyncTask<Void, Void, List<LatLng>> {
        @Override
        protected List<LatLng> doInBackground(Void... voids) {

            ArrayList<LatLng> points = new ArrayList<LatLng>();

            try {
                // Load GeoJSON file
                InputStream inputStream = getAssets().open("map.geojson");
                BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
                StringBuilder sb = new StringBuilder();
                int cp;
                while ((cp = rd.read()) != -1) {
                    sb.append((char) cp);
                }

                inputStream.close();

                // Parse JSON
                JSONObject json = new JSONObject(sb.toString());
                JSONArray features = json.getJSONArray("features");
                JSONObject feature = features.getJSONObject(0);
                JSONObject geometry = feature.getJSONObject("geometry");
                if (geometry != null) {
                    String type = geometry.getString("type");

                    // Our GeoJSON only has one feature: a line string
                    if (!TextUtils.isEmpty(type) && type.equalsIgnoreCase("LineString")) {

                        // Get the Coordinates
                        JSONArray coords = geometry.getJSONArray("coordinates");
                        for (int lc = 0; lc < coords.length(); lc++) {
                            JSONArray coord = coords.getJSONArray(lc);
                            LatLng latLng = new LatLng(coord.getDouble(1), coord.getDouble(0));
                            points.add(latLng);
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception Loading GeoJSON: " + e.toString());
            }

            return points;
        }

        @Override
        protected void onPostExecute(List<LatLng> noktalar) {
            super.onPostExecute(noktalar);

            if (noktalar.size() > 0) {
                LatLng[] pointsArray = noktalar.toArray(new LatLng[noktalar.size()]);

                // Draw Points on MapView
                mapView.addPolyline(new PolylineOptions()
                        .add(pointsArray)
                        .color(Color.parseColor("#3bb2d0"))
                        .width(3));
            }
        }


    }

}
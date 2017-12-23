package com.ksubbu199.niwe.niwe;

import android.Manifest;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_SATELLITE;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,LocationListener {

    private static final int PERMISSION_ACCESS_FINE_LOCATION = 0,PERMISSION_ACCESS_COARSE_LOCATION = 1;

    private GoogleMap mMap;
    private  Dialog gpsDialog,internetDialog;
    private static final String TAG = "MapActivity";
    private Marker marker;
    private boolean locationPermissions=false;
    private LocationManager locationManager;
    private Location location;
    ZoomControls zoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        zoom = (ZoomControls) findViewById(R.id.simpleZoomControl);
        zoom.setOnZoomOutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.animateCamera(CameraUpdateFactory.zoomOut());

            }
        });
        zoom.setOnZoomInClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.animateCamera(CameraUpdateFactory.zoomIn());

            }
        });



        //TextView verdictV = findViewById(R.id.text_view_verdict);
        TextView addrV = findViewById(R.id.text_view_address);
        addrV.setText("Select a region");
       // verdictV.setText("Status");
        //verdictV.setTextColor(Color.BLUE);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            if (doWeHaveLocationPerm()==false) {
                getLocPerm();
            }


        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.getView().setBackgroundColor(Color.WHITE);

        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_NONE).setCountry("IN")
                .build();

        autocompleteFragment.setFilter(typeFilter);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.

                setMap(place.getLatLng());
                Log.i(TAG, "Place: " + place.getName()+place.getLatLng());//get place details here
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status.toString());
            }
        });


        ImageView imageView = findViewById(R.id.gps_icon);
        View.OnClickListener clickListener = new View.OnClickListener() {
            public void onClick(View v) {
                    if(doWeHaveLocationPerm())
                    {
                        Location loc=getLocation();
                        if(loc!=null)
                            setMap(new LatLng(loc.getLatitude(),loc.getLongitude()));
                        else{
                            Toast.makeText(MapsActivity.this, "Unable to get your location!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else
                    {
                        getLocPerm();
                    }
                }

        };
        imageView.setOnClickListener(clickListener);

        final ImageView imageViewT = findViewById(R.id.view_switch);
        View.OnClickListener clickListenerT = new View.OnClickListener() {
            public void onClick(View v) {
                if(mMap.getMapType()==GoogleMap.MAP_TYPE_NORMAL)
                {
                    mMap.setMapType(MAP_TYPE_SATELLITE);
                    //imageViewT.setBackgroundResource(R.drawable.normal);
                    imageViewT.setImageResource(R.drawable.normal);
                }
                else
                {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    imageViewT.setImageResource(R.drawable.sat);
                }
                // Write your awesome code here
            }

        };
        imageViewT.setOnClickListener(clickListenerT);
        gpsDialog = new Dialog(this);

        gpsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        gpsDialog.setContentView(R.layout.dialog);


//        final EditText editText = (EditText) dialog.findViewById(R.id.editText);
        Button btnOn  = gpsDialog.findViewById(R.id.save);
        Button btnCancel = gpsDialog.findViewById(R.id.cancel);

        btnOn.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent gpsOptionsIntent = new Intent(
                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(gpsOptionsIntent);
            }
        });

        btnCancel.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                gpsDialog.cancel();
                Toast.makeText(MapsActivity.this, "GPS action unavailable!", Toast.LENGTH_SHORT).show();
            }
        });

        internetDialog = new Dialog(this);

        internetDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        internetDialog.setContentView(R.layout.internet_dialog);

        internetDialog.setCancelable(false);

//        final EditText editText = (EditText) dialog.findViewById(R.id.editText);
        Button btnOnInternet  = internetDialog.findViewById(R.id.save);
        Button btnCancelInternet = internetDialog.findViewById(R.id.cancel);

        btnOnInternet.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                internetDialog.dismiss();
                startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
            }
        });

        btnCancelInternet.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                try{
                    MapsActivity.this.finishAffinity();
                }
                catch(Exception e){
                    try{
                        finish();
                    }
                    catch(Exception f)
                    {
                        Toast.makeText(MapsActivity.this, "Soemthing went Wrong!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        if(isNetworkConnected()==false)
        {
            enableInernet();
        }

        final RadioGroup radioGroup = findViewById(R.id.select_search);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the RadioButton selected

                View radioButton = radioGroup.findViewById(checkedId);
                int index = radioGroup.indexOfChild(radioButton);

                // Add logic here

                //Fragment mapsFragment = getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
                LinearLayout maps = findViewById(R.id.place_autocomplete_layout);
                LinearLayout latlong = findViewById(R.id.input_layout);
                switch (index) {
                    case 0: // first button
                        //if(maps != null && maps.getVisibility() == LinearLayout.GONE)
                        //{
                        latlong.setVisibility(LinearLayout.GONE);
                        maps.setVisibility(LinearLayout.VISIBLE);
                        //}
                        Toast.makeText(getApplicationContext(), "Selected button number " + index, 500).show();
                        break;
                    case 1: // secondbutton
                        //if(latlong != null && latlong.getVisibility() == LinearLayout.GONE)
                        //{


                        maps.setVisibility(LinearLayout.GONE);
                        latlong.setVisibility(LinearLayout.VISIBLE);
                        //}
                        Toast.makeText(getApplicationContext(), "Selected button number " + index, 500).show();
                        break;
                }

            }
        });

        Button lat_long_form_btn = findViewById(R.id.lat_long_btn);

        lat_long_form_btn.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                EditText lat = findViewById(R.id.input_lat);
                EditText lng = findViewById(R.id.input_long);

                try{
                    double lat_v = Double.parseDouble(lat.getText().toString());
                    double lng_v = Double.parseDouble(lng.getText().toString());
                    if(isValidLatLng(lat_v,lng_v)==false){
                        throw new Exception();
                    }
                    setMap(new LatLng(lat_v,lng_v));
                }
                catch (Exception e){
                    Toast.makeText(getApplicationContext(),"Invalid Lattitude and Longitude",Toast.LENGTH_SHORT).show();
                    //What should happen when the input string is no double?
                }

            }
        });

    }

    public boolean isValidLatLng(double lat, double lng){
        if(lat < -90 || lat > 90)
        {
            return false;
        }
        else if(lng < -180 || lng > 180)
        {
            return false;
        }
        return true;
    }



    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    private void getLocPerm()
    {
        Toast.makeText(this, "Please provide location access!", Toast.LENGTH_SHORT).show();
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_FINE_LOCATION);
    }

    private void setMap(LatLng latLng){

        if(isNetworkConnected()==false)
        {
            enableInernet();
            return;
        }
        if(latLng==null) return;
        EditText lat = findViewById(R.id.input_lat);
        EditText lng = findViewById(R.id.input_long);
        lat.setText(String.valueOf(latLng.latitude));
        lng.setText(String.valueOf(latLng.longitude));
        getData(latLng);
        String addr=getCompleteAddressString(latLng.latitude,latLng.longitude);
        Log.d(TAG, "addr:"+addr);
        marker.setPosition(latLng);
        Log.d(TAG, "latitude : "+ marker.getPosition().latitude);
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        moveToCurrentLocation(latLng);
        TextView addrV = findViewById(R.id.text_view_address);
        if(addr.isEmpty())
            addrV.setText("Unable to fetch address!");
        else
            addrV.setText(addr);
    }

    public void onProviderDisabled(String string)
    {
     //   location=getLocation();
    }

    public void onProviderEnabled(String string)
    {
        location=getLocation();
    }

    public void onStatusChanged(String string, int i,Bundle b)
    {
        //location=getLocation();
    }

    public void onLocationChanged(Location loc)
    {
        location=loc;
    }


    private void moveToCurrentLocation(LatLng currentLocation)
    {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,15));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(13), 2000, null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    location=getLocation();
                    if(location!=null)
                    {
                        LatLng latLng=new LatLng(location.getLatitude(),location.getLongitude());
                        setMap(latLng);
                    }

                } else {
                    Toast.makeText(this, "Need your location to continue!", Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_FINE_LOCATION);
                }

                break;

        }
    }


    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    Log.w(TAG, "Got for this i:"+i);
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append(" ");
                }
                strAdd = strReturnedAddress.toString();
                Log.w(TAG, strReturnedAddress.toString());
            } else {
                Log.w(TAG, "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(TAG, "Canont get Address!");
        }
        return strAdd;
    }

    private boolean doWeHaveLocationPerm(){
        //return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        String permission = "android.permission.ACCESS_FINE_LOCATION";
        int res = this.checkCallingOrSelfPermission(permission);
        Log.w(TAG, "Location permission: !"+(res == PackageManager.PERMISSION_GRANTED));
        return (res == PackageManager.PERMISSION_GRANTED);

    }

    private boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        }else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }


    }

    public void enableLocation(){
        if(gpsDialog.isShowing()==false)
            gpsDialog.show();
    }

    public void enableInernet(){
        if(internetDialog.isShowing()==false)
            internetDialog.show();
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(isLocationEnabled(this)==true&&gpsDialog.isShowing()==true)
        {
            gpsDialog.dismiss();
        }
        Log.i("test", "onResume");
        if(isNetworkConnected()==false){
            enableInernet();
        }

    }


    private Location getLastKnownLocation() {
        LocationManager mLocationManager;
        mLocationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    public Location getLocation() {
        if (!doWeHaveLocationPerm()) {
                return null;
        }
        if (isLocationEnabled(this)) {
            Location location = getLastKnownLocation();
            if (location != null) {
                Log.e(TAG, "GPS is on");
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                moveToCurrentLocation(new LatLng(latitude,longitude));
                return location;
            }
            else{
                Log.e(TAG, "Else brah");
                //enableLocation();
                return null;
            }
        }
        else
        {
            Log.e(TAG, "Else fucked up");
            enableLocation();
            return null;
        }
    }

    private void getData(LatLng ll)
    {
        if(isNetworkConnected()==false)
        {
            enableInernet();
        }
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://14.139.172.6:8080/getLatInfo?lat="+ll.latitude+"&long="+ll.longitude+"&area="+900;
        Log.d(TAG, url);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, response);
                        try{
                            JSONObject obj = new JSONObject(response);
                            if(obj.getString("error")!=null)
                            {
                                Log.d(TAG, obj.getString("CUF"));
                                String snip= obj.toString();
                                marker.setSnippet(snip);
                                marker.showInfoWindow();
                            }
                            else
                            {
                                marker.setSnippet("");
                            }
                            marker.setTitle("Poweredby NIWE");
                        }
                        catch(JSONException e)
                        {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "API Server Error");
                marker.setSnippet("");
                return;

            }
        });
        queue.add(stringRequest);

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker arg0) {

                Log.d(TAG, "ohh"+arg0.getSnippet());
                View v = getLayoutInflater().inflate(R.layout.windowlayout, null);
                TextView titleV = v.findViewById(R.id.tv_title);
                titleV.setText("Powered by NIWE");

                TextView aep = v.findViewById(R.id.tv_aep);
                TextView cuf = v.findViewById(R.id.tv_cuf);
                TextView ghi = v.findViewById(R.id.tv_ghi);
                TextView dni = v.findViewById(R.id.tv_dni);
                TextView dhi = v.findViewById(R.id.tv_dhi);
                TextView lat = v.findViewById(R.id.tv_lat);
                TextView lng = v.findViewById(R.id.tv_lng);

                TextView aep_value = v.findViewById(R.id.tv_aep_value);
                TextView cuf_value = v.findViewById(R.id.tv_cuf_value);
                TextView ghi_value = v.findViewById(R.id.tv_ghi_value);
                TextView dni_value = v.findViewById(R.id.tv_dni_value);
                TextView dhi_value = v.findViewById(R.id.tv_dhi_value);
                TextView lat_value = v.findViewById(R.id.tv_lat_value);
                TextView lng_value = v.findViewById(R.id.tv_lng_value);

                lat.setText("Latitude");
                lng.setText("Longitude");


               // TextView verdictV = findViewById(R.id.text_view_verdict);

                if(arg0.getSnippet()!=null)
                {
                    try{
                        JSONObject obj = new JSONObject(arg0.getSnippet());
                        LatLng latLng = arg0.getPosition();
                        aep.setText(obj.getJSONObject("AEP").getString("units")+"(kWh)");
                        cuf.setText(obj.getJSONObject("CUF").getString("units"));
                        ghi.setText(obj.getJSONObject("GHI").getString("units"));
                        dhi.setText(obj.getJSONObject("DHI").getString("units"));
                        dni.setText(obj.getJSONObject("DNI").getString("units"));

                        aep_value.setText(obj.getJSONObject("AEP").getString("value"));
                        cuf_value.setText(obj.getJSONObject("CUF").getString("value"));
                        ghi_value.setText(obj.getJSONObject("GHI").getString("value"));
                        dhi_value.setText(obj.getJSONObject("DHI").getString("value"));
                        dni_value.setText(obj.getJSONObject("DNI").getString("value"));
                        lat_value.setText(String.valueOf(latLng.latitude));
                        lng_value.setText(String.valueOf(latLng.longitude));

                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                        cuf.setText("Something went wrong!");
                        ghi.setText("");
                        dni.setText("");
                        dhi.setText("");
                        aep.setText("");
                    }
                }
                else
                {
                    cuf.setText("We got no data here!");
                    ghi.setText("");
                    dni.setText("");
                    dhi.setText("");
                    aep.setText("");
                    //verdictV.setText("Status");
                    //verdictV.setTextColor(Color.BLUE);
                }
                return v;
            }
        });

        location=getLocation();

        double lat,lng;
        if(location!=null)
        {
            lat=location.getLatitude();
            lng=location.getLongitude();
        }
        else
        {
            lat=28.7041;
            lng=77.216721;
        }

        LatLng ll = new LatLng(lat, lng);
        marker = mMap.addMarker(new MarkerOptions().position(ll).title("Marked Location").draggable(true));
        setMap(ll);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                setMap(latLng);
            }
        });

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {

            @Override
            public void onMarkerDragStart(Marker marker) {
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                Log.d(TAG, "latitude : "+ marker.getPosition().latitude);
                mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));

            }

            @Override
            public void onMarkerDrag(Marker marker) {
            }

        });

    }
}

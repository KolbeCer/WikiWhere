package com.example.wikiwhere.NearbyPlaces;

import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wikiwhere.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;
import com.skyfishjy.library.RippleBackground;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.jetbrains.anko.ToastsKt.toast;

public class MapActivity<prviate> extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,  GoogleMap.OnInfoWindowClickListener {

    private GoogleMap mMap;
    private GoogleApiClient client;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlacesClient placesClient;
    private List<AutocompletePrediction> predictionList;

    private Location mLastKnownLocation;
    private LocationCallback locationCallback;

    //Variables for UI
    private MaterialSearchBar materialSearchBar;
    private View mapView;
    private Button btnFind;
    private RippleBackground rippleBg;

    private List<String> listOfUrls = new ArrayList<>();
    private List<String> listOfTitles = new ArrayList<>();

    //Variables for getting nearby locations
    private double latitude, longitude;
    private int ProximityRadius = 50000;

    private final float DEFAULT_ZOOM = 18;
    public List<HashMap<String, String>> nearbyPlacesList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nb_activity_map);

        //Create components of map
        materialSearchBar = findViewById(R.id.searchBar);
        btnFind = findViewById(R.id.btn_find);
        rippleBg = findViewById(R.id.ripple_bg);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById((R.id.map));
        mapFragment.getMapAsync(this);
        mapView = mapFragment.getView();

        //Create map using API key
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapActivity.this);
        Places.initialize(MapActivity.this, "AIzaSyCVcaWYjLUyzeHQtMBdcZ2MRyZ2tODl1wY");
        placesClient = Places.createClient(this);
        final AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {

            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                startSearch(text.toString(), true, null, true);

            }

            @Override
            public void onButtonClicked(int buttonCode) {
                if(buttonCode == MaterialSearchBar.BUTTON_NAVIGATION){
                    //opening or closing a navigation drawer
                } else if(buttonCode == MaterialSearchBar.BUTTON_BACK){
                    materialSearchBar.disableSearch();
                }

            }
        });

        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                //Set autocomplete to find
                FindAutocompletePredictionsRequest predictionsRequest = FindAutocompletePredictionsRequest.builder()
                        .setCountry("us")
                        .setTypeFilter(TypeFilter.ADDRESS)
                        .setSessionToken(token)
                        .setQuery(s.toString())
                        .build();
                placesClient.findAutocompletePredictions(predictionsRequest).addOnCompleteListener(new OnCompleteListener<FindAutocompletePredictionsResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {
                        if(task.isSuccessful()){
                            FindAutocompletePredictionsResponse predictionsResponse = task.getResult();
                            if(predictionsResponse != null) {
                                predictionList = predictionsResponse.getAutocompletePredictions();
                                List<String> suggestionsList = new ArrayList<>();
                                for(int i=0; i< predictionList.size(); i++){
                                    AutocompletePrediction prediction = predictionList.get(i);
                                    suggestionsList.add(prediction.getFullText(null).toString());
                                }
                                materialSearchBar.updateLastSuggestions(suggestionsList);
                                if(!materialSearchBar.isSuggestionsVisible()){
                                    materialSearchBar.showSuggestionsList();
                                }
                            }
                        } else {
                            Log.i("mytag", "Prediction fetching task unsuccessful");
                        }
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        materialSearchBar.setSuggestionsClickListener(new SuggestionsAdapter.OnItemViewClickListener() {
            @Override
            public void OnItemClickListener(int position, View v) {
                if(position >= predictionList.size()){
                    return;
                }
                AutocompletePrediction selectedPrediction = predictionList.get(position);
                String suggestion = materialSearchBar.getLastSuggestions().get(position).toString();
                materialSearchBar.setText(suggestion);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        materialSearchBar.clearSuggestions();
                    }
                }, 1000);

                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if(imm != null)
                    imm.hideSoftInputFromWindow(materialSearchBar.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
                final String placeId = selectedPrediction.getPlaceId();
                List<Place.Field> placeFields = Arrays.asList(Place.Field.LAT_LNG);

                FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build();
                placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                    @Override
                    public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                        Place place = fetchPlaceResponse.getPlace();
                        Log.i("mytag", "Place found: " + place.getName());
                        LatLng latLngOfPlace = place.getLatLng();
                        if(latLngOfPlace != null){
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngOfPlace, DEFAULT_ZOOM));
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if(e instanceof ApiException){
                            ApiException apiException = (ApiException) e;
                            apiException.printStackTrace();
                            int statusCode = apiException.getStatusCode();
                            Log.i("mytag", "Place not found: " + e.getMessage());
                            Log.i("mytag", "Status code: " + statusCode);
                        }
                    }
                });
            }

            @Override
            public void OnItemDeleteListener(int position, View v) {

            }
        });
        btnFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng currentMarkerLocation = mMap.getCameraPosition().target;

                Object[] transferData = new Object[2];
                GetNearbyPlaces getNearbyPlaces = new GetNearbyPlaces();
                rippleBg.startRippleAnimation();
                String url = getUrl(latitude, longitude, "restaurant");
                transferData[0] = mMap;
                transferData[1] = url;

                getNearbyPlaces.execute(transferData);

                rippleBg.stopRippleAnimation();

            }
        });
    }

    private String getUrl(double latitude, double longitude, String type){

        StringBuilder googleURL = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googleURL.append("location=" + 28.5383+ "," + -81.3792);
        googleURL.append("&radius=" + ProximityRadius);
        googleURL.append("&type=" + "restaurant");
        googleURL.append("&sensor=true");
        googleURL.append("&key=AIzaSyCVcaWYjLUyzeHQtMBdcZ2MRyZ2tODl1wY");
        Log.d("MapsActivity", "url = " + googleURL.toString());

        return googleURL.toString();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        mMap.setOnInfoWindowClickListener(this);

        if(mapView != null && mapView.findViewById(Integer.parseInt("1")) != null) {
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 40, 180);

        }

        //Check if GPS is enabled or not and then request user to enable it
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

        SettingsClient settingsClient = LocationServices.getSettingsClient(MapActivity.this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

        task.addOnSuccessListener(MapActivity.this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                getDeviceLocation();
            }
        });

        task.addOnFailureListener(MapActivity.this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if(e instanceof ResolvableApiException){
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    try {
                        resolvable.startResolutionForResult(MapActivity.this, 51);
                    } catch (IntentSender.SendIntentException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                if(materialSearchBar.isSuggestionsVisible())
                    materialSearchBar.clearSuggestions();
                if(materialSearchBar.isSearchEnabled())
                    materialSearchBar.disableSearch();
                return false;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 51){
            if(resultCode == RESULT_OK){
                getDeviceLocation();
            }
        }
    }

    private void getDeviceLocation(){
        mFusedLocationProviderClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            mLastKnownLocation = task.getResult();
                            if (mLastKnownLocation != null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            } else {
                                final LocationRequest locationRequest = LocationRequest.create();
                                locationRequest.setInterval(10000);
                                locationRequest.setFastestInterval(5000);
                                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                                locationCallback = new LocationCallback() {
                                    @Override
                                    public void onLocationResult(LocationResult locationResult) {
                                        super.onLocationResult(locationResult);
                                        if (locationResult == null) {
                                            return;
                                        }
                                        mLastKnownLocation = locationResult.getLastLocation();
                                        longitude = mLastKnownLocation.getLongitude();
                                        latitude = mLastKnownLocation.getLatitude();
                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                        mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
                                    }
                                };
                                mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);

                            }
                        } else {
//                            Toast.makeText(MapActivity.this, "Cannot get last location", Toast.LENGTH_SHORT).show();
                        }
                    }
        });
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onInfoWindowClick(Marker marker) {

            String title = marker.getTitle();
            String url = "https://www.google.com/";
            for ( int i = 0; i < listOfTitles.size();i++){
                Log.d("Kolbe-title",title);
                Log.d("Kolbe-list",listOfTitles.get(i));
                if(listOfTitles.get(i).equalsIgnoreCase(title))
                    url = listOfUrls.get(i);
            }


            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);

    }

    private class GetNearbyPlaces extends AsyncTask<Object, String, String> {
        private String googleplaceData, url;
        private GoogleMap mMap;

        @Override
        protected String doInBackground(Object... objects) {

            mMap = (GoogleMap) objects[0];
            url = (String) objects[1];

            Log.d("Mason", "url in background:" + url);

            DownloadUrl downloadUrl = new DownloadUrl();
            try {
                googleplaceData = downloadUrl.ReadTheURL(url);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.d("Mason", "Google place data: " + googleplaceData);

            return googleplaceData;
        }

        @Override
        protected void onPostExecute(String s) {

            DataParser dataParser = new DataParser();
            Log.d("Mason", "Before parse");
            try {
                Thread.sleep(10000);
                nearbyPlacesList = dataParser.parse(s);

                Log.d("Mason", "Nearest places is: " + nearbyPlacesList);

                for (int i = 0; i < nearbyPlacesList.size(); i++)
                {
                    Log.d("Mason", "Place name is: " + nearbyPlacesList.get(i).get("place_name"));
                    String keyword = nearbyPlacesList.get(i).get("place_name");

                    String WIKIPEDIA_URL = "https://en.wikipedia.org/w/api.php?action=query&list=search&srsearch=" +
                    keyword + "%22" + keyword +
                                "%22&format=json&srlimit=1";

                    // Start AsyncTask
                    FetchWikiDataAsync fetchWikiDataAsync = new FetchWikiDataAsync();
                    fetchWikiDataAsync.execute(WIKIPEDIA_URL);
                }

                Log.d("Mason", "URL List is: " + listOfUrls);
                Log.d("Mason", "Title List is: " + listOfTitles);

                DisplayNearbyPlaces(nearbyPlacesList);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        private void DisplayNearbyPlaces(List<HashMap<String, String>> nearbyPlacesList){
            for (int i=0; i<nearbyPlacesList.size(); i++){
                MarkerOptions markerOptions = new MarkerOptions();

                HashMap<String, String> googleNearbyPlace = nearbyPlacesList.get(i);
                String nameOfPlace = googleNearbyPlace.get("place_name");
                String vicinity = googleNearbyPlace.get("vicinity");
                double lat = Double.parseDouble(googleNearbyPlace.get("lat"));
                double lng = Double.parseDouble(googleNearbyPlace.get("lng"));

                LatLng latLng = new LatLng(lat, lng);
                markerOptions.position(latLng);
                markerOptions.title(nameOfPlace);
                markerOptions.snippet(vicinity);
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
                mMap.addMarker(markerOptions);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
            }
        }
    }


    private class FetchWikiDataAsync extends AsyncTask<String, Void, String> {

        private TextView txtWikiData;

        @Override
        protected String doInBackground(String[] params) {
            String sURL = params[0];
            try {
                URL url = new URL(sURL);        // Convert String URL to java.net.URL
                // Connection: to Wikipedia API
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder stringBuilder = new StringBuilder();
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }

                String wikiData = stringBuilder.toString();

//                Log.d("Mason", "Wiki data in background: " + wikiData);

                // Parse JSON Data
                String formattedData = parseJSONData(wikiData);

                return formattedData;

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String formattedData) {
            super.onPostExecute(formattedData);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // HTML Data
//                txtWikiData.setText(Html.fromHtml
//                        (formattedData,Html.FROM_HTML_MODE_LEGACY));
            } else {
                // HTML Data
//                txtWikiData.setText(Html.fromHtml(formattedData));
            }
        }

        private String parseJSONData(String wikiData) throws JSONException {
            JSONObject jsonObject;
            JSONArray search = null;

            try {
                jsonObject = new JSONObject(wikiData);
//                Log.d("Mason", "wikiData JSON is: " + wikiData);
                JSONObject query = jsonObject.getJSONObject("query");
//                Log.d("Mason", "query JSON is: " + query);
                search = query.getJSONArray("search");

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return getWikiUrl(search);
        }

        private String getWikiUrl(JSONArray data) throws JSONException {

            String pageid = "";

            if (data != null) {
                if (data.get(0) != null) {
                    pageid = getPageId((JSONObject) data.get(0));
                }
            }

            String WIKI_URL = "https://en.wikipedia.org/w/api.php?action=query&prop=info&pageids="
                    + pageid + "&inprop=url&format=json";

            readWikiUrl(pageid, WIKI_URL);

            return pageid;
        }

        private String getPageId(JSONObject search) {

            String pageid = "";

            try {
                if (!search.isNull("pageid")) {
                    pageid = search.getString("pageid");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return pageid;
        }

        protected void readWikiUrl(String pageid, String params) {
            String sURL = params;

            try {
                URL url = new URL(sURL);        // Convert String URL to java.net.URL
                // Connection: to Wikipedia API
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder stringBuilder = new StringBuilder();
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }

                String wikiData = stringBuilder.toString();

//                Log.d("Mason", "Wiki data in background: " + wikiData);

                getRealUrl(pageid, wikiData);

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void getRealUrl(String pageid, String pageInfo){
            JSONObject init;
            String wikiUrl = "";
            String title = "";

            try {
                init = new JSONObject(pageInfo);

                JSONObject query = init.getJSONObject("query");

                JSONObject pages = query.getJSONObject("pages");

                JSONObject page_num = pages.getJSONObject(pageid);

                wikiUrl = page_num.getString("fullurl");
                title = page_num.getString("title");

            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (wikiUrl != "") {
                listOfUrls.add(wikiUrl);
                listOfTitles.add(title);
            }
        }
    }


            @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }




}

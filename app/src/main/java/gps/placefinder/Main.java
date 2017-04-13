package gps.placefinder;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class Main extends FragmentActivity implements OnMapReadyCallback{

    private ArrayList<PlaceDetail> place_details;
    private Place place; private GoogleMap cMap;
    private ArrayAdapter<PlaceDetail> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Button find = (Button) findViewById(R.id.find_again);
        find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAutocompleteActivity();
            }
        });

        ListView list = (ListView) findViewById(R.id.place_details);
        place_details=new ArrayList<>();

        adapter = new ArrayAdapter<PlaceDetail>(this,R.layout.list_item,place_details)
        {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if(convertView == null){
                    convertView = getLayoutInflater().inflate(R.layout.list_item, null);
                }

                ImageView icon =(ImageView)convertView.findViewById(R.id.place_detail_icon);
                icon.setImageResource(place_details.get(position).icon);

                TextView val = (TextView)convertView.findViewById(R.id.place_detail_value);
                val.setText(place_details.get(position).value);

                return convertView;
            }
        };

        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setPrimaryClip(ClipData.newPlainText("",place_details.get(i).value));
                Toast.makeText(getApplicationContext(),"Copied to clipboard", Toast.LENGTH_LONG).show();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        openAutocompleteActivity();
    }

    private void openAutocompleteActivity() {
        try {
            AutocompleteFilter typeFilter = new AutocompleteFilter.Builder().setCountry("IN").build();
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).setFilter(typeFilter).build(this);
            startActivityForResult(intent, 1);
        }
        catch (GooglePlayServicesRepairableException e) {
            GoogleApiAvailability.getInstance().getErrorDialog(this, e.getConnectionStatusCode(),0).show();
        }
        catch (GooglePlayServicesNotAvailableException e) {
            String message = "Google Play Services is not available: " + GoogleApiAvailability.getInstance().getErrorString(e.errorCode);
            Log.e(TAG, message); Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {

                place = PlaceAutocomplete.getPlace(this, data);
                Log.i(TAG, "Place Selected: " + place.getName());

                cMap.clear();
                cMap.addMarker(new MarkerOptions().position(place.getLatLng()).title(place.getName().toString()));
                cMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(),15));
                populateList();

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Log.e(TAG, "Error: Status = " + status.toString());
            } else if (resultCode == RESULT_CANCELED) {}
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        cMap=map;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setZoomGesturesEnabled(true);
    }

    protected void populateList()
    {
        place_details.clear();

        if(place.getAddress()!=null)
            place_details.add(new PlaceDetail(R.drawable.address,place.getAddress()));

        if(place.getPhoneNumber().length()>0)
            place_details.add(new PlaceDetail(R.drawable.phone,place.getPhoneNumber()));

        if(place.getWebsiteUri()!=null)
            place_details.add(new PlaceDetail(R.drawable.url,place.getWebsiteUri().toString()));

        adapter.notifyDataSetChanged();
    }
}
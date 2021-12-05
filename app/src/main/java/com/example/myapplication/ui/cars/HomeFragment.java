package com.example.myapplication.ui.cars;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.myapplication.Downloaders.JSONArrayDownloader;
//import com.example.myapplication.JSONArrayDownloader;
import com.example.myapplication.MapsActivity;
import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentHomeBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class HomeFragment extends Fragment {
    Button btnGps,btnRepairs;
    ListView repairs;
    Spinner car;


    LocationManager locationManager;
    LocationListener locationListener;
    ArrayList<String> typeRepairId;
    ArrayList<String> typeRepairNames;

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;
    public double latitude,longitude;
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        repairs= binding.listRepairs;
        btnRepairs=binding.btnSearchRepairs;
        btnGps = binding.btnGps;
        car = binding.carSelector;

        btnRepairs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view)
                        .navigate(R.id.action_navigation_home_to_TypeRepairListView);
            }
        });

        btnGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("CENAS", "CENAS");
                getLocation();
                Intent intent=new Intent(getContext(), MapsActivity.class);

                intent.putExtra("Latitude", Double.toString(latitude));
                intent.putExtra("Longitude", Double.toString(longitude));

                startActivity(intent);
            }
        });

        JSONArrayDownloader task = new JSONArrayDownloader();
        JSONArray objTypeRepair;
        try {
            objTypeRepair = task.execute("https://mechanic-on-the-go.herokuapp.com/api/typeRepair").get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            objTypeRepair = null;
        }

        JSONObject obj;
        typeRepairId = new ArrayList<>();
        typeRepairNames = new ArrayList<>();
        if(objTypeRepair != null) {
            for(int i = 0; i < objTypeRepair.length(); i++) {
                try {
                    obj = objTypeRepair.getJSONObject(i);
                    typeRepairId.add(obj.getString("id"));
                    typeRepairNames.add(obj.getString("typeRepairName"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        Log.i("1",typeRepairNames.toString());

        initializeMyLListView();

        return root;
    }

    private void initializeMyLListView(){
        ArrayAdapter<String> myListAdapter=new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,typeRepairNames);
        repairs.setAdapter(myListAdapter);
    }


    public void getLocation() {
        locationManager = (LocationManager) this.getContext().getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updateLocationInfo(location);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) { }

            @Override
            public void onProviderEnabled(String s) { }

            @Override
            public void onProviderDisabled(String s) { }
        };

        if (ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this.getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        } else if(ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this.getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }else {

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (location != null) {
                updateLocationInfo(location);
            }
        }
    }

    public void updateLocationInfo(Location location) {

        Log.i("LocationInfo", location.toString());

        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
        latitude=location.getLatitude();
        longitude=location.getLongitude();
        try {

            String address = "Could not find address";
            List<Address> listAddresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

            if (listAddresses != null && listAddresses.size() > 0 )
            {
                Log.i("PlaceInfo", listAddresses.get(0).toString());

                address = "Address: \n";

                if (listAddresses.get(0).getSubThoroughfare() != null) {
                    address += listAddresses.get(0).getSubThoroughfare() + " ";
                }

                if (listAddresses.get(0).getThoroughfare() != null) {
                    address += listAddresses.get(0).getThoroughfare() + "\n";
                }

                if (listAddresses.get(0).getLocality() != null) {
                    address += listAddresses.get(0).getLocality() + "\n";
                }

                if (listAddresses.get(0).getPostalCode() != null) {
                    address += listAddresses.get(0).getPostalCode() + "\n";
                }

                if (listAddresses.get(0).getCountryName() != null) {
                    address += listAddresses.get(0).getCountryName() + "\n";
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startListening()
    {
        if (ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager = (LocationManager) this.getContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startListening();
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}
package edu.gatech.wang.hedi.classthirteen.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import edu.gatech.wang.hedi.classthirteen.R;

public class MapFragment extends Fragment {

    ArrayList<MarkerOptions> markers;
    private static View view;
    private GoogleMap map;

    public static MapFragment newInstance() {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public MapFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
        }
        try {
            view = inflater.inflate(R.layout.fragment_map, container, false);
            SupportMapFragment fragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.main_map);
            fragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    googleMap.setMyLocationEnabled(true);
                    map = googleMap;
                    LatLng latLng = new LatLng(33.773669, -84.397748);
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 2));
                    if (markers != null) {
                        for (MarkerOptions markerOptions : markers) {
                            googleMap.addMarker(markerOptions);
                        }
                    }
                }
            });
        } catch (Exception e) {

        }

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public ArrayList<MarkerOptions> getMarkers() {
        return markers;
    }

    public void setMarkers(ArrayList<MarkerOptions> markers) {
        this.markers = markers;
        if (map != null) {
            map.clear();
            LatLng latLng = new LatLng(33.773669, -84.397748);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 2));
            if (markers != null) {
                for (MarkerOptions markerOptions : markers) {
                    map.addMarker(markerOptions);
                }
            }
        }
    }
}

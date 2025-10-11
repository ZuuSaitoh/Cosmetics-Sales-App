package com.example.myapplication;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import vn.vietmap.vietmapsdk.Vietmap;
import vn.vietmap.vietmapsdk.annotations.Polygon;
import vn.vietmap.vietmapsdk.annotations.Polyline;
import vn.vietmap.vietmapsdk.annotations.PolylineOptions;
import vn.vietmap.vietmapsdk.geometry.LatLng;
import vn.vietmap.vietmapsdk.maps.MapView;
import vn.vietmap.vietmapsdk.maps.Style;
import vn.vietmap.vietmapsdk.maps.VietMapGL;

public class MapActivity extends AppCompatActivity {

    private MapView mapView;
    private VietMapGL vietMapGL;

    private List<Polyline> polylines = null;
    private ArrayList<PolylineOptions> polylineOptions = new ArrayList<>();
    private Polygon polygon = null;

    private static final String STATE_POLYLINE_OPTIONS = "polylineOptions";

    private static final LatLng HOCHIMINH = new LatLng(10.791257, 106.669189);
    private static final LatLng NINHTHUAN = new LatLng(11.550254, 108.960579);
    private static final LatLng DANANG = new LatLng(16.045746, 108.202241);
    private static final LatLng HUE = new LatLng(16.469602, 107.577462);
    private static final LatLng NGHEAN = new LatLng(18.932151, 105.577207);
    private static final LatLng HANOI = new LatLng(21.024696, 105.833099);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize Vietmap SDK
        Vietmap.getInstance(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vietmapview);

        mapView = findViewById(R.id.vmMapView);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(new VietMapGL.OnMapReadyCallback() {
            @Override
            public void onMapReady(VietMapGL vietMapGL) {
                MapActivity.this.vietMapGL = vietMapGL;

                vietMapGL.setStyle(new Style.Builder()
                        .fromUri("https://maps.vietmap.vn/api/maps/light/styles.json?apikey=YOUR_API_KEY_HERE"));

                vietMapGL.setOnPolylineClickListener(new VietMapGL.OnPolylineClickListener() {
                    @Override
                    public void onPolylineClick(Polyline polyline) {
                        Toast.makeText(
                                MapActivity.this,
                                "You clicked on polyline with id = " + polyline.getId(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
            }
        });
    }
}

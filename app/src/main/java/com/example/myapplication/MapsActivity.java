package com.example.myapplication;

import static com.example.myapplication.BuildConfig.VIETMAP_API_KEY;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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
import vn.vietmap.vietmapsdk.maps.OnMapReadyCallback;


public class MapsActivity extends AppCompatActivity implements VietMapGL.OnMapClickListener {

    private MapView mapView;
    private VietMapGL vietMapGL;
    private MapsManager mapsManager;

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

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(VietMapGL vietMapGL) {
                MapsActivity.this.vietMapGL = vietMapGL;

                vietMapGL.setStyle(
                        new Style.Builder()
                                .fromUri("https://maps.vietmap.vn/api/maps/light/styles.json?apikey=" + VIETMAP_API_KEY),
                        new Style.OnStyleLoaded() {
                            @Override
                            public void onStyleLoaded(Style style) {
                                // ✅ Khởi tạo MapsManager khi style đã load xong
                                mapsManager = new MapsManager(MapsActivity.this, vietMapGL);

                                // Gọi các hàm khởi tạo trong MapsManager
                                mapsManager.initLocationEngine();
                                mapsManager.enableLocationComponent(style);
                                vietMapGL.moveCamera(
                                        vn.vietmap.vietmapsdk.camera.CameraUpdateFactory.newLatLngZoom(HOCHIMINH, 13.0)
                                );

                                // Đăng ký sự kiện click bản đồ
                                vietMapGL.addOnMapClickListener(MapsActivity.this);

                                // Đăng ký sự kiện click polyline
                                vietMapGL.setOnPolylineClickListener(new VietMapGL.OnPolylineClickListener() {
                                    @Override
                                    public void onPolylineClick(Polyline polyline) {
                                        Toast.makeText(
                                                MapsActivity.this,
                                                "You clicked on polyline with id = " + polyline.getId(),
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    }
                                });
                            }
                        }
                );
            }
        });
    }

    // ✅ Xử lý khi người dùng click vào bản đồ
    @Override
    public boolean onMapClick(LatLng latLng) {
        if (mapsManager != null) {
            mapsManager.addMarker(latLng);
        }
        return false;
    }

    // ⚠️ Check quyền truy cập vị trí
    private boolean checkPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    // ✅ Lifecycle MapView
    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
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
}

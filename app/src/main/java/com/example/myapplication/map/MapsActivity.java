package com.example.myapplication.map;

import static com.example.myapplication.BuildConfig.VIETMAP_API_KEY;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.myapplication.R;
import com.mapbox.geojson.Point;

import java.util.List;

import vn.vietmap.vietmapsdk.Vietmap;
import vn.vietmap.vietmapsdk.annotations.Polygon;
import vn.vietmap.vietmapsdk.annotations.Polyline;
import vn.vietmap.vietmapsdk.annotations.PolylineOptions;
import vn.vietmap.vietmapsdk.geometry.LatLng;
import vn.vietmap.vietmapsdk.location.permissions.PermissionsManager;
import vn.vietmap.vietmapsdk.maps.MapView;
import vn.vietmap.vietmapsdk.maps.OnMapReadyCallback;
import vn.vietmap.vietmapsdk.maps.Style;
import vn.vietmap.vietmapsdk.maps.VietMapGL;

public class MapsActivity extends AppCompatActivity {

    private MapView mapView;
    private VietMapGL vietMapGL;
    private MapsManager mapsManager;
    private MapsNavigationManager mapsNavigationManager;

    private PermissionsManager permissionsManager;
    private TextToSpeech textToSpeech;
    private List<Polyline> polylines = null;
    private Polygon polygon = null;

    private LatLng userLatLng; // ⚠️ Lưu vị trí người dùng để dùng khi nhấn nút
    private static final LatLng STORE_LOCATION = new LatLng(10.976238345142892, 106.61804099635049);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Vietmap.getInstance(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vietmapview);

        Button button = findViewById(R.id.pushToNavigationScreen);
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

                                // ✅ Khởi tạo các manager
                                mapsManager = new MapsManager(MapsActivity.this, vietMapGL);
                                mapsNavigationManager = new MapsNavigationManager(MapsActivity.this, vietMapGL);

                                // ⚠️ Kiểm tra quyền vị trí
                                if (!checkPermission()) {
                                    ActivityCompat.requestPermissions(
                                            MapsActivity.this,
                                            new String[]{
                                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                            },
                                            1
                                    );
                                    return;
                                }

                                // ✅ Bật vị trí
                                mapsManager.initLocationEngine();
                                mapsManager.enableLocationComponent(style);

                                // 📍 Thêm marker cửa hàng
                                mapsManager.addMarker(STORE_LOCATION);

                                // ✅ Lấy vị trí người dùng và hiển thị camera
                                mapsManager.getUserLocation(location -> {
                                    if (location != null) {
                                        userLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                                        // Hiển thị vùng chứa user + store
                                        vn.vietmap.vietmapsdk.geometry.LatLngBounds bounds =
                                                new vn.vietmap.vietmapsdk.geometry.LatLngBounds.Builder()
                                                        .include(userLatLng)
                                                        .include(STORE_LOCATION)
                                                        .build();

                                        vietMapGL.animateCamera(
                                                vn.vietmap.vietmapsdk.camera.CameraUpdateFactory.newLatLngBounds(bounds, 100)
                                        );
                                    } else {
                                        vietMapGL.moveCamera(
                                                vn.vietmap.vietmapsdk.camera.CameraUpdateFactory.newLatLngZoom(STORE_LOCATION, 13.0)
                                        );
                                        Toast.makeText(MapsActivity.this, "Không thể lấy vị trí người dùng", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                // ✅ Khi người dùng nhấn nút Navigation
                                button.setOnClickListener(v -> {
                                    if (userLatLng == null) {
                                        Toast.makeText(MapsActivity.this, "Chưa có vị trí người dùng!", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    mapsNavigationManager.drawShortestRoute(
                                            mapView,
                                            Point.fromLngLat(STORE_LOCATION.getLongitude(), STORE_LOCATION.getLatitude()),
                                            Point.fromLngLat(userLatLng.getLongitude(), userLatLng.getLatitude())
                                    );
                                });
                            }
                        }
                );
            }
        });
    }

    // ⚠️ Check quyền truy cập vị trí
    private boolean checkPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (vietMapGL != null) {
                vietMapGL.getStyle(style -> {
                    mapsManager.initLocationEngine();
                    mapsManager.enableLocationComponent(style);
                });
            }
        } else {
            Toast.makeText(this, "Cần cấp quyền vị trí để hiển thị vị trí của bạn", Toast.LENGTH_SHORT).show();
        }
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

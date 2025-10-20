package com.example.myapplication.map;

import static com.example.myapplication.BuildConfig.VIETMAP_API_KEY;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.mapbox.geojson.Point;

import java.util.List;

import vn.vietmap.vietmapsdk.Vietmap;
import vn.vietmap.vietmapsdk.annotations.Marker;
import vn.vietmap.vietmapsdk.annotations.Polygon;
import vn.vietmap.vietmapsdk.annotations.Polyline;
import vn.vietmap.vietmapsdk.annotations.PolylineOptions;
import vn.vietmap.vietmapsdk.geometry.LatLng;
import vn.vietmap.vietmapsdk.location.permissions.PermissionsManager;
import vn.vietmap.vietmapsdk.maps.MapView;
import vn.vietmap.vietmapsdk.maps.OnMapReadyCallback;
import vn.vietmap.vietmapsdk.maps.Style;
import vn.vietmap.vietmapsdk.maps.VietMapGL;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.example.myapplication.map.MapsManager;

public class MapsActivity extends AppCompatActivity {

    private MapView mapView;
    private VietMapGL vietMapGL;
    private MapsManager mapsManager;
    private MapsNavigationManager mapsNavigationManager;

    private PermissionsManager permissionsManager;
    private TextToSpeech textToSpeech;
    private List<Polyline> polylines = null;
    private Polygon polygon = null;
    private double distance; // Lưu giá trị khoảng cách thực tế (mét)


    private LatLng userLatLng; // ⚠️ Lưu vị trí người dùng để dùng khi nhấn nút
    private static final LatLng STORE_LOCATION = new LatLng(10.976238345142892, 106.61804099635049);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

                                // ✅ Khởi tạo các manager
                                mapsManager = new MapsManager(MapsActivity.this, vietMapGL);
                                mapsNavigationManager = new MapsNavigationManager(MapsActivity.this, vietMapGL);

                                // ⚠️ SỬA LỖI 2: Logic xử lý quyền
                                // Kiểm tra quyền vị trí
                                if (!checkPermission()) {
                                    // Nếu chưa có quyền, yêu cầu quyền và dừng lại
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

                                // ✅ Nếu đã có quyền, chạy setup map
                                setupMapAndLocation(style);
                            }
                        }
                );
            }
        });
    }

    /**
     * ✅ SỬA LỖI 2: Tách logic setup ra hàm riêng
     * Hàm này chứa toàn bộ code setup map (thêm marker, bật vị trí, zoom camera)
     * Nó sẽ được gọi ở 2 nơi:
     * 1. onStyleLoaded: Nếu đã có quyền từ trước.
     * 2. onRequestPermissionsResult: Nếu vừa mới được cấp quyền.
     */
    private void setupMapAndLocation(Style style) {
        // ✅ Bật vị trí
        mapsManager.initLocationEngine();
        mapsManager.enableLocationComponent(style);

        // 📍 Thêm marker cửa hàng
        Marker storeMarker = mapsManager.addMarker(STORE_LOCATION);

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

        // ✅ Gán listener click (phải nằm trong này để đảm bảo storeMarker đã được tạo)
        vietMapGL.setOnMarkerClickListener(marker -> {
            // Thêm kiểm tra marker != null để an toàn
            if (marker != null && marker.equals(storeMarker)) {
                showStoreBottomSheet();
                return true;
            }
            return false;
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
                // ✅ SỬA LỖI 2: Gọi hàm setup chính tại đây
                // Sau khi người dùng cấp quyền, chúng ta phải chạy lại toàn bộ logic setup
                vietMapGL.getStyle(style -> {
                    setupMapAndLocation(style);
                });
            }
        } else {
            Toast.makeText(this, "Cần cấp quyền vị trí để hiển thị vị trí của bạn", Toast.LENGTH_SHORT).show();
            // Cân nhắc: Nếu không có quyền, có thể zoom vào cửa hàng
            // if(vietMapGL != null) {
            //     vietMapGL.moveCamera(
            //         vn.vietmap.vietmapsdk.camera.CameraUpdateFactory.newLatLngZoom(STORE_LOCATION, 13.0)
            //     );
            // }
        }
    }


    private void showStoreBottomSheet() {
        // --- Tạo BottomSheetDialog ---
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.dialog_store_info);

        // --- Ánh xạ view ---
        TextView txtDistance = bottomSheetDialog.findViewById(R.id.txtDistance);
        TextView txtStoreAddress = bottomSheetDialog.findViewById(R.id.txtStoreAddress);
        RecyclerView recyclerView = bottomSheetDialog.findViewById(R.id.recyclerImages);
        Button btnNavigate = bottomSheetDialog.findViewById(R.id.btnNavigate);

        // --- Gán thông tin cửa hàng ---
        if (txtStoreAddress != null) {
            txtStoreAddress.setText("Hòa Phú, Củ Chi, Hồ Chí Minh, Việt Nam");
        }

        // --- Danh sách ảnh cửa hàng ---
        int[] imageRes = {R.drawable.store1, R.drawable.store2, R.drawable.store3};
        if (recyclerView != null) {
            recyclerView.setLayoutManager(
                    new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            );
            recyclerView.setAdapter(new StoreImageAdapter(imageRes));
        }

        // ✅ Khi popup mở ra, tính trước khoảng cách thực tế
        if (userLatLng != null && txtDistance != null) {
            txtDistance.setText("Đang tính khoảng cách...");

            mapsNavigationManager.drawShortestRoute(
                    mapView,
                    com.mapbox.geojson.Point.fromLngLat(
                            userLatLng.getLongitude(),
                            userLatLng.getLatitude()
                    ),
                    com.mapbox.geojson.Point.fromLngLat(
                            STORE_LOCATION.getLongitude(),
                            STORE_LOCATION.getLatitude()
                    ),
                    distanceMeters -> runOnUiThread(() -> {
                        distance = distanceMeters; // ✅ lưu lại giá trị
                        String distanceText;
                        if (distanceMeters >= 1000) {
                            distanceText = String.format("%.2f km", distanceMeters / 1000);
                        } else {
                            distanceText = String.format("%.0f m", distanceMeters);
                        }
                        txtDistance.setText("Khoảng cách: " + distanceText);
                    })
            );
        } else if (txtDistance != null) {
            txtDistance.setText("Không thể xác định vị trí người dùng");
        }


        // --- Nút chỉ đường (vẽ tuyến đường thật) ---
        if (btnNavigate != null) {
            btnNavigate.setOnClickListener(v -> {
                if (userLatLng == null) {
                    Toast.makeText(this, "Chưa có vị trí người dùng!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // ✅ Gọi lại hàm để vẽ tuyến đường thật trên bản đồ
                mapsNavigationManager.drawShortestRoute(
                        mapView,
                        com.mapbox.geojson.Point.fromLngLat(
                                userLatLng.getLongitude(),
                                userLatLng.getLatitude()
                        ),
                        com.mapbox.geojson.Point.fromLngLat(
                                STORE_LOCATION.getLongitude(),
                                STORE_LOCATION.getLatitude()
                        ),
                        null // không cần callback nữa
                );
            });
        }

        // --- Hiển thị popup ---
        bottomSheetDialog.show();
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
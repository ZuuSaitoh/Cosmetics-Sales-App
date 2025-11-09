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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private Button btnZoomIn;
    private Button btnZoomOut;
    private double currentZoomLevel = 13.0; // Lưu zoom level hiện tại

    private PermissionsManager permissionsManager;
    private TextToSpeech textToSpeech;
    private List<Polyline> polylines = null;
    private Polygon polygon = null;
    private double distance; // Lưu giá trị khoảng cách thực tế (mét)


    private LatLng userLatLng; // ⚠️ Lưu vị trí người dùng để dùng khi nhấn nút
    
    // ✅ Danh sách các cửa hàng
    private static class Store {
        LatLng location;
        String name;
        String address;
        int[] images; // Danh sách ảnh cho popup
        
        Store(LatLng location, String name, String address, int[] images) {
            this.location = location;
            this.name = name;
            this.address = address;
            this.images = images;
        }
    }
    
    // ✅ 4 vị trí cửa hàng với ảnh khác nhau cho mỗi cửa hàng
    private static final Store[] STORES = {
        new Store(
            new LatLng(10.976238345142892, 106.61804099635049), 
            "Cửa hàng Hòa Phú", 
            "Hòa Phú, Củ Chi, Hồ Chí Minh, Việt Nam",
            new int[]{R.drawable.store1, R.drawable.store2, R.drawable.store3} // Ảnh cho cửa hàng 1
        ),
        new Store(
            new LatLng(10.8231, 106.6297), 
            "Cửa hàng Quận 1", 
            "Đường Nguyễn Huệ, Quận 1, Hồ Chí Minh, Việt Nam",
            new int[]{R.drawable.store2, R.drawable.store3, R.drawable.store1} // Ảnh cho cửa hàng 2
        ),
        new Store(
            new LatLng(10.762622, 106.660172), 
            "Cửa hàng Quận 3", 
            "Đường Lê Văn Sỹ, Quận 3, Hồ Chí Minh, Việt Nam",
            new int[]{R.drawable.store3, R.drawable.store1, R.drawable.store2} // Ảnh cho cửa hàng 3
        ),
        new Store(
            new LatLng(10.8412, 106.8098), 
            "Cửa hàng Quận 7", 
            "Đường Nguyễn Thị Thập, Quận 7, Hồ Chí Minh, Việt Nam",
            new int[]{R.drawable.store1, R.drawable.store3, R.drawable.store2} // Ảnh cho cửa hàng 4
        )
    };
    
    // ✅ Map để lưu marker và cửa hàng tương ứng
    private Map<Marker, Store> markerStoreMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Vietmap.getInstance(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vietmapview);

        mapView = findViewById(R.id.vmMapView);
        mapView.onCreate(savedInstanceState);
        
        // ✅ Ánh xạ các nút zoom
        btnZoomIn = findViewById(R.id.btnZoomIn);
        btnZoomOut = findViewById(R.id.btnZoomOut);
        
        // ✅ Thiết lập click listeners cho các nút zoom
        setupZoomControls();

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

        // 📍 Thêm marker cho tất cả các cửa hàng
        markerStoreMap.clear();
        for (Store store : STORES) {
            Marker marker = mapsManager.addMarker(store.location);
            markerStoreMap.put(marker, store);
        }

        // ✅ Lấy vị trí người dùng và hiển thị camera
        mapsManager.getUserLocation(location -> {
            if (location != null) {
                userLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                // Hiển thị vùng chứa user + tất cả các cửa hàng
                vn.vietmap.vietmapsdk.geometry.LatLngBounds.Builder boundsBuilder =
                        new vn.vietmap.vietmapsdk.geometry.LatLngBounds.Builder()
                                .include(userLatLng);
                
                // Thêm tất cả các cửa hàng vào bounds
                for (Store store : STORES) {
                    boundsBuilder.include(store.location);
                }
                
                vn.vietmap.vietmapsdk.geometry.LatLngBounds bounds = boundsBuilder.build();

                vietMapGL.animateCamera(
                        vn.vietmap.vietmapsdk.camera.CameraUpdateFactory.newLatLngBounds(bounds, 100)
                );
                // Cập nhật zoom level (bounds sẽ tự động tính zoom phù hợp)
                currentZoomLevel = 13.0;
            } else {
                // Nếu không có vị trí người dùng, zoom vào cửa hàng đầu tiên
                currentZoomLevel = 13.0;
                vietMapGL.moveCamera(
                        vn.vietmap.vietmapsdk.camera.CameraUpdateFactory.newLatLngZoom(STORES[0].location, currentZoomLevel)
                );
                Toast.makeText(MapsActivity.this, "Không thể lấy vị trí người dùng", Toast.LENGTH_SHORT).show();
            }
        });

        // ✅ Gán listener click cho tất cả các marker
        vietMapGL.setOnMarkerClickListener(marker -> {
            if (marker != null && markerStoreMap.containsKey(marker)) {
                Store selectedStore = markerStoreMap.get(marker);
                showStoreBottomSheet(selectedStore);
                return true;
            }
            return false;
        });
    }


    /**
     * ✅ Thiết lập các nút zoom controls
     */
    private void setupZoomControls() {
        btnZoomIn.setOnClickListener(v -> {
            if (vietMapGL != null) {
                // Tăng zoom level lên 1
                currentZoomLevel = Math.min(currentZoomLevel + 1, 20.0); // Giới hạn zoom tối đa là 20
                vietMapGL.animateCamera(
                        vn.vietmap.vietmapsdk.camera.CameraUpdateFactory.zoomTo(currentZoomLevel),
                        300
                );
            }
        });

        btnZoomOut.setOnClickListener(v -> {
            if (vietMapGL != null) {
                // Giảm zoom level xuống 1
                currentZoomLevel = Math.max(currentZoomLevel - 1, 3.0); // Giới hạn zoom tối thiểu là 3
                vietMapGL.animateCamera(
                        vn.vietmap.vietmapsdk.camera.CameraUpdateFactory.zoomTo(currentZoomLevel),
                        300
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


    private void showStoreBottomSheet(Store store) {
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
            txtStoreAddress.setText(store.address);
        }

        // --- Danh sách ảnh cửa hàng (sử dụng ảnh riêng cho từng cửa hàng) ---
        if (recyclerView != null) {
            recyclerView.setLayoutManager(
                    new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            );
            // Sử dụng ảnh của cửa hàng được chọn
            recyclerView.setAdapter(new StoreImageAdapter(store.images));
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
                            store.location.getLongitude(),
                            store.location.getLatitude()
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
                                store.location.getLongitude(),
                                store.location.getLatitude()
                        ),
                        null // không cần callback nữa
                );
                bottomSheetDialog.dismiss();
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
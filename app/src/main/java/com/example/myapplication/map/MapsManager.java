package com.example.myapplication.map;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;

import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;

import vn.vietmap.vietmapsdk.annotations.Marker;
import vn.vietmap.vietmapsdk.annotations.MarkerOptions;
import vn.vietmap.vietmapsdk.annotations.Polygon;
import vn.vietmap.vietmapsdk.annotations.PolygonOptions;
import vn.vietmap.vietmapsdk.annotations.Polyline;
import vn.vietmap.vietmapsdk.annotations.PolylineOptions;
import vn.vietmap.vietmapsdk.camera.CameraUpdateFactory;
import vn.vietmap.vietmapsdk.geometry.LatLng;
import vn.vietmap.vietmapsdk.location.LocationComponent;
import vn.vietmap.vietmapsdk.location.LocationComponentActivationOptions;
import vn.vietmap.vietmapsdk.location.engine.LocationEngine;
import vn.vietmap.vietmapsdk.location.engine.LocationEngineCallback;
import vn.vietmap.vietmapsdk.location.engine.LocationEngineDefault;
import vn.vietmap.vietmapsdk.location.engine.LocationEngineResult;
import vn.vietmap.vietmapsdk.location.modes.CameraMode;
import vn.vietmap.vietmapsdk.location.modes.RenderMode;
import vn.vietmap.vietmapsdk.maps.Style;
import vn.vietmap.vietmapsdk.maps.VietMapGL;

public class MapsManager {

    private final Context context;
    private final VietMapGL vietMapGL;
    private LocationEngine locationEngine;
    private LocationComponent locationComponent;
    private Polygon polygon;
    private List<Polyline> polylines;
    private final ArrayList<PolylineOptions> polylineOptions = new ArrayList<>();

    // ✅ Các điểm cố định
    private static final LatLng HOCHIMINH = new LatLng(10.791257, 106.669189);
    private static final LatLng NINHTHUAN = new LatLng(11.550254, 108.960579);
    private static final LatLng DANANG = new LatLng(16.045746, 108.202241);
    private static final LatLng HUE = new LatLng(16.469602, 107.577462);
    private static final LatLng NGHEAN = new LatLng(18.932151, 105.577207);
    private static final LatLng HANOI = new LatLng(21.024696, 105.833099);

    public MapsManager(Context context, VietMapGL vietMapGL) {
        this.context = context;
        this.vietMapGL = vietMapGL;
    }

    // ✅ Thêm marker
    public Marker addMarker(LatLng position) {
        return vietMapGL.addMarker(
                new MarkerOptions()
                        .position(position)
                        .title("Vietmap")
                        .snippet("Vietmap Android SDK")
                        .icon(
                                new IconUtils().drawableToIcon(
                                        context,
                                        R.drawable.ic_cart,
                                        Color.TRANSPARENT // Giữ nguyên màu gốc

                                )
                        )
        );
    }


    // ✅ Khởi tạo bộ lấy dữ liệu GPS
    public  void initLocationEngine() {
        locationEngine = LocationEngineDefault.INSTANCE.getDefaultLocationEngine(context);
    }

    // ✅ Kích hoạt và hiển thị vị trí người dùng
    public void enableLocationComponent(Style style) {
        locationComponent = vietMapGL.getLocationComponent();
        if (locationComponent == null) return;

        // Đảm bảo locationEngine luôn sẵn sàng
        if (locationEngine == null) {
            initLocationEngine();
        }

        // Kích hoạt LocationComponent
        locationComponent.activateLocationComponent(
                LocationComponentActivationOptions.builder(context, style).build()
        );

        // Kiểm tra quyền
        if (!checkPermission()) {
            Toast.makeText(context, "Chưa cấp quyền vị trí", Toast.LENGTH_SHORT).show();
            return;
        }

        // Cấu hình hiển thị
        locationComponent.setLocationEngine(locationEngine);
        locationComponent.setLocationComponentEnabled(true);
        locationComponent.setRenderMode(RenderMode.GPS);
        locationComponent.setCameraMode(CameraMode.TRACKING_GPS_NORTH);
        locationComponent.zoomWhileTracking(18.0);

        // ✅ Lấy vị trí hiện tại và di chuyển camera
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            locationEngine.getLastLocation(new LocationEngineCallback<LocationEngineResult>() {
                @Override
                public void onSuccess(LocationEngineResult result) {
                    Location location = result.getLastLocation();
                    if (location != null) {
                        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
            //        vietMapGL.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 16.0));

                        // Debug: in ra vị trí hiện tại
                        Toast.makeText(context, "Vị trí hiện tại: "
                                + location.getLatitude() + ", " + location.getLongitude(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Không thể lấy vị trí GPS hiện tại", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(context, "Lỗi khi lấy vị trí: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // ✅ Kiểm tra quyền vị trí
    private boolean checkPermission() {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }
    // ✅ Interface callback để trả về vị trí người dùng
    public interface OnUserLocationChangedListener {
        void onLocationChanged(@NonNull Location location);
    }

    // ✅ Hàm lấy vị trí hiện tại (không hiển thị chấm xanh)
    public void getUserLocation(OnUserLocationChangedListener listener) {
        if (locationEngine == null) {
            initLocationEngine();
        }

        if (!checkPermission()) {
            Toast.makeText(context, "Chưa cấp quyền vị trí", Toast.LENGTH_SHORT).show();
            return;
        }

        locationEngine.getLastLocation(new LocationEngineCallback<LocationEngineResult>() {
            @Override
            public void onSuccess(LocationEngineResult result) {
                Location location = result.getLastLocation();
                if (location != null) {
                    listener.onLocationChanged(location);
                } else {
                    Toast.makeText(context, "Không thể lấy vị trí GPS hiện tại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(context, "Lỗi khi lấy vị trí: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }





}
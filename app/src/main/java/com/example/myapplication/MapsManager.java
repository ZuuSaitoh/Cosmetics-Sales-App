package com.example.myapplication;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.List;

import vn.vietmap.vietmapsdk.annotations.Marker;
import vn.vietmap.vietmapsdk.annotations.MarkerOptions;
import vn.vietmap.vietmapsdk.annotations.Polygon;
import vn.vietmap.vietmapsdk.annotations.PolygonOptions;
import vn.vietmap.vietmapsdk.annotations.Polyline;
import vn.vietmap.vietmapsdk.annotations.PolylineOptions;
import vn.vietmap.vietmapsdk.geometry.LatLng;
import vn.vietmap.vietmapsdk.location.LocationComponent;
import vn.vietmap.vietmapsdk.location.LocationComponentActivationOptions;
import vn.vietmap.vietmapsdk.location.engine.LocationEngine;
import vn.vietmap.vietmapsdk.location.engine.LocationEngineDefault;
import vn.vietmap.vietmapsdk.location.modes.CameraMode;
import vn.vietmap.vietmapsdk.location.modes.RenderMode;
import vn.vietmap.vietmapsdk.maps.Style;
import vn.vietmap.vietmapsdk.maps.VietMapGL;

public class MapsManager {

    private VietMapGL vietMapGL;
    private Context context;

    private Polygon polygon;
    private LocationEngine locationEngine;
    private LocationComponent locationComponent;

    private List<Polyline> polylines;
    private ArrayList<PolylineOptions> polylineOptions = new ArrayList<>();

    // Các điểm cố định
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

    // ✅ Thêm marker lên bản đồ
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
                                        ResourcesCompat.getColor(
                                                context.getResources(),
                                                R.color.black,
                                                context.getTheme()
                                        )
                                )
                        )
        );
    }

    // ✅ Tạo 1 polyline giữa 2 điểm
    private PolylineOptions generatePolyline(LatLng start, LatLng end, String color) {
        PolylineOptions line = new PolylineOptions();
        line.add(start);
        line.add(end);
        line.color(Color.parseColor(color));
        return line;
    }

    // ✅ Lấy tất cả polyline options
    private List<PolylineOptions> getAllPolylines() {
        List<PolylineOptions> options = new ArrayList<>();
        options.add(generatePolyline(HOCHIMINH, NINHTHUAN, "#F44336"));
        options.add(generatePolyline(NINHTHUAN, DANANG, "#FF5722"));
        options.add(generatePolyline(DANANG, HUE, "#673AB7"));
        options.add(generatePolyline(HUE, NGHEAN, "#009688"));
        options.add(generatePolyline(NGHEAN, HANOI, "#795548"));
        return options;
    }

    // ✅ Thêm tất cả polyline vào bản đồ + xử lý click
    public void addPolyline() {
        polylineOptions.addAll(getAllPolylines());
        polylines = vietMapGL.addPolylines(polylineOptions);

        vietMapGL.setOnPolylineClickListener(new VietMapGL.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {
                Toast.makeText(
                        context,
                        "You clicked on polyline with id = " + polyline.getId(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    // ✅ Danh sách các điểm polygon
    private static final ArrayList<LatLng> STAR_SHAPE_POINTS = new ArrayList<LatLng>() {{
        add(new LatLng(10.791257, 106.669189));
        add(new LatLng(11.550254, 108.960579));
        add(new LatLng(16.045746, 108.202241));
        add(new LatLng(16.469602, 107.577462));
    }};

    // ✅ Thêm polygon vào bản đồ
    public void addPolygon() {
        polygon = vietMapGL.addPolygon(
                new PolygonOptions()
                        .addAll(STAR_SHAPE_POINTS)
                        .fillColor(Color.parseColor("#3bb2d0"))
        );

        vietMapGL.setOnPolygonClickListener(new VietMapGL.OnPolygonClickListener() {
            @Override
            public void onPolygonClick(Polygon polygon) {
                Toast.makeText(
                        context,
                        "You clicked on polygon with id = " + polygon.getId(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    // ✅ Khởi tạo location engine
    public void initLocationEngine() {
        //ocationEngine = LocationEngineDefault.getBestLocationEngine(context);
    }

    // ✅ Bật location component
    public void enableLocationComponent(Style style) {
        locationComponent = vietMapGL.getLocationComponent();

        if (locationComponent != null) {
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(context, style).build()
            );

            if (!checkPermission()) {
                Toast.makeText(context, "Location permission not granted", Toast.LENGTH_SHORT).show();
                return;
            }

            locationComponent.setCameraMode(CameraMode.TRACKING_GPS_NORTH, 750L, 18.0, 0.0, 0.0, null);
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.zoomWhileTracking(19.0);
            locationComponent.setRenderMode(RenderMode.GPS);
            locationComponent.setLocationEngine(locationEngine);
        }

        updateMyLocationTrackingMode();
        updateMyLocationRenderMode();
    }

    // ✅ Cập nhật chế độ camera tracking
    private void updateMyLocationTrackingMode() {
        int[] vietmapTrackingMode = new int[]{
                CameraMode.NONE,
                CameraMode.TRACKING,
                CameraMode.TRACKING_COMPASS,
                CameraMode.TRACKING_GPS
        };
        locationComponent.setCameraMode(vietmapTrackingMode[0]);
    }

    // ✅ Cập nhật chế độ hiển thị vị trí
    private void updateMyLocationRenderMode() {
        int[] vietmapRenderModes = new int[]{
                RenderMode.NORMAL,
                RenderMode.COMPASS,
                RenderMode.GPS
        };
        locationComponent.setRenderMode(vietmapRenderModes[0]);
    }

    // ✅ Kiểm tra quyền vị trí
    private boolean checkPermission() {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }
}

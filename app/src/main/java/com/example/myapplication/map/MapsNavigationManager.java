package com.example.myapplication.map;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.example.myapplication.R;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;
import vn.vietmap.services.android.navigation.v5.navigation.NavigationMapRoute;
import vn.vietmap.services.android.navigation.v5.navigation.NavigationRoute;
import vn.vietmap.vietmapsdk.annotations.MarkerOptions;
import vn.vietmap.vietmapsdk.camera.CameraUpdate;
import vn.vietmap.vietmapsdk.camera.CameraUpdateFactory;
import vn.vietmap.vietmapsdk.geometry.LatLng;
import vn.vietmap.vietmapsdk.geometry.LatLngBounds;
import vn.vietmap.vietmapsdk.maps.MapView;
import vn.vietmap.vietmapsdk.maps.VietMapGL;

import static com.example.myapplication.BuildConfig.VIETMAP_API_KEY;

public class MapsNavigationManager {

    private final Context context;
    private final VietMapGL vietMapGL;
    private NavigationMapRoute navigationMapRoute;
    private DirectionsRoute currentRoute;
    private List<DirectionsRoute> directionsRoutes;

    // ✅ Interface callback để trả về khoảng cách
    public interface OnRouteDistanceListener {
        void onDistanceCalculated(double distanceMeters);
    }

    public MapsNavigationManager(Context context, VietMapGL vietMapGL) {
        this.context = context;
        this.vietMapGL = vietMapGL;
    }

    /**
     * ✅ Hàm vẽ đường đi và tính khoảng cách thực tế giữa 2 điểm
     */
    public void drawShortestRoute(MapView mapView, Point origin, Point destination, OnRouteDistanceListener listener) {
        if (origin == null || destination == null || vietMapGL == null) return;

        NavigationRoute.builder(context)
                .apikey(VIETMAP_API_KEY)
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        if (response.body() == null || response.body().routes() == null) return;
                        directionsRoutes = response.body().routes();
                        if (directionsRoutes.isEmpty()) return;

                        currentRoute = directionsRoutes.get(0);

                        // Xóa route cũ
                        if (navigationMapRoute != null) {
                            navigationMapRoute.removeRoute();
                        }

                        // Vẽ route mới
                        navigationMapRoute = new NavigationMapRoute(mapView, vietMapGL, "boundary_province");
                        navigationMapRoute.addRoute(currentRoute);

                        // ✅ Tính khoảng cách thực tế
                        double distanceMeters = currentRoute.distance();
                        Log.d("Distance", "Khoảng cách thực tế: " + distanceMeters + "m");

                        // ✅ Gửi kết quả về callback
                        if (listener != null) {
                            listener.onDistanceCalculated(distanceMeters);
                        }

                        // Zoom toàn tuyến
                        animateCameraToRoute(currentRoute);
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                        Timber.e("Fetch route failed: %s", throwable.getMessage());
                    }
                });
    }

    private void animateCameraToRoute(DirectionsRoute route) {
        if (route == null || route.routeOptions() == null) return;
        List<Point> routePoints = route.routeOptions().coordinates();
        if (routePoints == null || routePoints.size() <= 1) return;

        List<LatLng> latLngList = new ArrayList<>();
        for (Point p : routePoints) {
            latLngList.add(new LatLng(p.latitude(), p.longitude()));
        }

        LatLngBounds bounds = new LatLngBounds.Builder().includes(latLngList).build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 100);
        vietMapGL.animateCamera(cameraUpdate, 2000);
    }

    public void addMarker(LatLng position) {
        vietMapGL.addMarker(
                new MarkerOptions()
                        .position(position)
                        .icon(
                                new IconUtils().drawableToIcon(
                                        context,
                                        R.drawable.baseline_location_on_24,
                                        Color.TRANSPARENT
                                )
                        )
        );
    }
}
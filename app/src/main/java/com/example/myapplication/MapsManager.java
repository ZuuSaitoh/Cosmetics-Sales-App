package com.example.myapplication;

import android.content.Context;

import androidx.core.content.res.ResourcesCompat;


import vn.vietmap.vietmapsdk.annotations.Marker;
import vn.vietmap.vietmapsdk.annotations.MarkerOptions;
import vn.vietmap.vietmapsdk.geometry.LatLng;
import vn.vietmap.vietmapsdk.maps.VietMapGL;

public class MapsManager {

    private VietMapGL vietMapGL;
    private Context context;

    public MapsManager(Context context, VietMapGL vietMapGL) {
        this.context = context;
        this.vietMapGL = vietMapGL;
    }

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
                                        ResourcesCompat.getColor(context.getResources(), R.color.black, context.getTheme())
                                )
                        )
        );
    }
}


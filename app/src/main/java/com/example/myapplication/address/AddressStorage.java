package com.example.myapplication.address;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AddressStorage {
    private static final String PREFS_NAME = "address_book_prefs"; // reuse AddressBookActivity storage
    private static final String KEY_ADDRESSES = "addresses";

    public static List<AddressEntry> load(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = sp.getString(KEY_ADDRESSES, "[]");
        List<AddressEntry> result = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                AddressEntry e = new AddressEntry(
                        o.has("id") ? (o.optLong("id") == 0 ? null : o.optLong("id")) : null,
                        o.optString("name"),
                        o.optString("phone"),
                        o.optString("address"),
                        o.optBoolean("isDefault", false)
                );
                result.add(e);
            }
        } catch (JSONException ignored) { }
        return result;
    }

    public static void save(Context context, List<AddressEntry> addresses) {
        JSONArray arr = new JSONArray();
        for (AddressEntry e : addresses) {
            JSONObject o = new JSONObject();
            try {
                if (e.id != null) o.put("id", e.id);
                o.put("name", e.name);
                o.put("phone", e.phone);
                o.put("address", e.address);
                o.put("isDefault", e.isDefault);
                arr.put(o);
            } catch (JSONException ignored) { }
        }
        SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_ADDRESSES, arr.toString()).apply();
    }

    public static int findDefaultIndex(List<AddressEntry> addresses) {
        for (int i = 0; i < addresses.size(); i++) {
            if (addresses.get(i).isDefault) return i;
        }
        return -1;
    }
}



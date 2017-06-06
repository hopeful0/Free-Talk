package cn.studyjams.s2.sj0211.freetalk;

import android.graphics.Color;

import com.google.firebase.database.DataSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by hopeful on 17-5-31.
 */
public class Message {

    public long timeStamp;

    public String fromName;

    public String message;

    public Location location;

    public int color;

    public Message (DataSnapshot data) {
        String name = data.child("FromName").getValue(String.class);
        fromName = name.substring(0, name.length() - 6);
        String sc = name.substring(name.length() - 6);
        color = Color.parseColor("#"+sc.toUpperCase());
        message = data.child("Message").getValue(String.class);
        timeStamp = data.child("TimeStamp").getValue(Long.class);
        if (data.hasChild("Location")) location = new Location(data.child("Location"));
    }

    public static HashMap<String,Object> toValue(String fromName, String message) {
        HashMap<String,Object> value = new HashMap<>(3);
        value.put("FromName", fromName);
        value.put("Message",message);
        value.put("TimeStamp", System.currentTimeMillis());
        return value;
    }

    public static HashMap<String,Object> toValue(String fromName, String message, Location location) {
        HashMap<String,Object> value = toValue(fromName, message);
        value.put("Location", location.toValue());
        return value;
    }

    public String getTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm:ss");
        return sdf.format(new Date(timeStamp));
    }
}

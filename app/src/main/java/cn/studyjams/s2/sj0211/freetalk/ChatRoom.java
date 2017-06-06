package cn.studyjams.s2.sj0211.freetalk;

import java.util.HashMap;

/**
 * Created by hopeful on 17-6-1.
 */
public class ChatRoom {

    public static HashMap<String, Object> toValue(long roomId, String roomName) {
        HashMap<String, Object> value = new HashMap<>(2);
        value.put("RoomId", roomId);
        value.put("RoomName", roomName);
        return value;
    }
}

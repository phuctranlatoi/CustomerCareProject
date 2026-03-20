package com.example.customercareproject.utils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Helper tập trung URL Firebase Realtime Database.
 * Nếu crash với URL này, hãy vào Firebase Console → Realtime Database
 * và copy URL chính xác (thường là https://<project-id>-default-rtdb.firebaseio.com
 * hoặc https://<project-id>-default-rtdb.<region>.firebasedatabase.app)
 */
public class FirebaseHelper {

    // Thay URL này bằng URL thực tế từ Firebase Console → Realtime Database
    public static final String RTDB_URL = "https://console.firebase.google.com/project/duanthuctap-c4aff/database/duanthuctap-c4aff-default-rtdb/data/~2F?hl=vi";

    private static FirebaseDatabase instance;

    public static FirebaseDatabase getRTDB() {
        if (instance == null) {
            instance = FirebaseDatabase.getInstance(RTDB_URL);
        }
        return instance;
    }

    public static DatabaseReference getChatRef(String ticketId) {
        return getRTDB().getReference("chats").child(ticketId).child("messages");
    }

    public static DatabaseReference getStatusRef(String uid) {
        return getRTDB().getReference("status").child(uid);
    }
}

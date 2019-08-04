package com.techbeloved.travelmantics4odife;

import com.google.firebase.auth.FirebaseAuth;

public class FirebaseUtil {

    public static void signOut(FirebaseAuth auth) {
        auth.signOut();
    }

}

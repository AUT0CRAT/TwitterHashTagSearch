/*
 * Copyright 2016 Alim Parkar.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package alim.parkar.twitterwingify;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Singleton class that Handles the logic to save and retrieve the auth token
 */
public class AuthManager {

    private static final String SHARED_PREF_NAME = "AuthManager";
    private static final String PREF_AUTH = "AUTH";
    private static final String TAG = "AuthManager";

    private static AuthManager INSTANCE;

    private AuthManager() {
    }

    public static AuthManager getAuthManager() {
        if (INSTANCE == null) {
            INSTANCE = new AuthManager();
        }

        return INSTANCE;
    }


    public void saveAuthToken(String token) {
        Log.d(TAG, "Saving token : " + token);
        SharedPreferences preferences = TwitterApplication.getContext().getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        preferences.edit().putString(PREF_AUTH, token).apply();
    }

    public String getAuthToken() {
        SharedPreferences preferences = TwitterApplication.getContext().getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getString(PREF_AUTH, null);
    }
}

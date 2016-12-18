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

package alim.parkar.twitterwingify.communication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import alim.parkar.twitterwingify.TwitterApplication;

/**
 * AsyncTask that loads a profile picture
 */
public class ProfilePicLoader extends AsyncTask<Void, Void, Drawable> {

    private static final String TAG = "ProfilePicLoader";

    private String path;
    private long identifier;
    private Callback callback;

    /**
     * Constructor.
     *
     * @param identifier The identifier for the tweet. This will be returned in the callback methods.
     * @param path       Path to the profile picture to be downloaded.
     * @param callback   Callback to whom the result will be sent.
     */
    public ProfilePicLoader(long identifier, String path, Callback callback) {
        this.path = path;
        this.identifier = identifier;
        this.callback = callback;
    }


    @Override
    protected Drawable doInBackground(Void... voids) {
        Bitmap x;
        HttpURLConnection connection;
        try {
            connection = (HttpsURLConnection) new URL(path).openConnection();
            connection.connect();
            InputStream input = connection.getInputStream();

            x = BitmapFactory.decodeStream(input);
            BitmapDrawable profileBitmap = new BitmapDrawable(TwitterApplication.getContext().getResources(), x);

            return profileBitmap;
        } catch (MalformedURLException e) {
            Log.e(TAG, "Invalid Url", e);
            return null;

        } catch (IOException e) {
            Log.e(TAG, "Failed to load bitmap", e);
            return null;
        }

    }

    @Override
    protected void onPostExecute(Drawable drawable) {
        if (callback != null) {
            if (drawable != null) {
                callback.onProfilePicLoaded(identifier, drawable);
            } else {
                callback.onProfilePicLoadFailed(identifier);
            }

        }
    }

    /**
     * Picture result callbacks
     */
    public interface Callback {
        /**
         * Called when the Profile picture is loaded successfully
         *
         * @param identifier The identifier which was passed to {@link ProfilePicLoader#ProfilePicLoader(long, String, Callback)};
         * @param drawable   The drawable loaded from the url passed to {@link ProfilePicLoader#ProfilePicLoader(long, String, Callback)};
         */
        void onProfilePicLoaded(long identifier, Drawable drawable);

        /**
         * Called when the picture fails to load.
         *
         * @param identifier The identifier which was passed to {@link ProfilePicLoader#ProfilePicLoader(long, String, Callback)};
         */
        void onProfilePicLoadFailed(long identifier);
    }
}

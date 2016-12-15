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

import alim.parkar.twitterwingify.TwitterApplication;

/**
 * @author ibasit
 */
public class ProfilePicLoader extends AsyncTask<Void, Void, Drawable> {

    private static final String TAG = "ProfilePicLoader";

    private String path;
    private long identifier;
    private Callback callback;

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
            connection = (HttpURLConnection) new URL(path).openConnection();
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

    public interface Callback {
        void onProfilePicLoaded(long identifier, Drawable drawable);

        void onProfilePicLoadFailed(long identifier);
    }
}

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

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import alim.parkar.twitterwingify.AuthManager;
import alim.parkar.twitterwingify.models.Tweet;

/**
 * @author ibasit
 */
public class TweetsLoaderTask {

    private static final String TAG = "TweetsLoaderTask";

    private long sinceId;
    private long maxId;
    private String searchQuery;
    private CallBack callback;
    private AsyncLoadingTask loadingTask;

    public TweetsLoaderTask(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public TweetsLoaderTask(long sinceId, long maxId, String searchQuery) {
        this.sinceId = sinceId;
        this.maxId = maxId;
        this.searchQuery = searchQuery;
    }

    public void setSinceId(long sinceId) {
        this.sinceId = sinceId;
    }

    public void setMaxId(long maxId) {
        this.maxId = maxId;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public void load(CallBack callBack) {
        this.callback = callBack;
        if (loadingTask != null) {
            loadingTask.cancel(true);
        }

        loadingTask = new AsyncLoadingTask();
        loadingTask.execute();
    }

    private String getUrl() {
        StringBuilder urlBuilder = new StringBuilder("https://api.twitter.com/1.1/search/tweets.json?q=%23");
        urlBuilder.append(searchQuery);
        if (sinceId > 0) {
            urlBuilder.append("&since_id=").append(sinceId);
        }

        if (maxId > 0) {
            urlBuilder.append("&max_id=").append(maxId);
        }

        return urlBuilder.toString();
    }

    public interface CallBack {
        void onSuccess(List<Tweet> tweets);

        void onFailure();
    }

    private class AsyncLoadingTask extends AsyncTask<Void, Void, List<Tweet>> {

        @Override
        protected List<Tweet> doInBackground(Void... params) {

            HttpsURLConnection urlConnection = null;
            final String authToken = AuthManager.getAuthManager().getAuthToken();
            if (authToken == null || authToken.trim().isEmpty()) {
                return null;
            }

            try {
                URL loginUri = new URL(getUrl());
                urlConnection = (HttpsURLConnection) loginUri.openConnection();
                urlConnection.setDoInput(true);
                urlConnection.setRequestProperty("Authorization", "Bearer " + authToken);

                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                List<Tweet> tweets = new TwitterSearchParser().parseResponse(inputStream);
                inputStream.close();
                return tweets;
            } catch (MalformedURLException e) {
                Log.e(TAG, "URI is invalid", e);
            } catch (IOException e) {
                Log.e(TAG, "Failed to read/write stream", e);
                InputStream stream = urlConnection.getErrorStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                String response = null;
                try {
                    response = reader.readLine();
                    reader.close();
                    stream.close();
                } catch (IOException e1) {
                    Log.e(TAG, "Failed to read from error stream", e);
                }

                Log.d(TAG, "Error : " + response);

            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<Tweet> tweets) {
            if (tweets == null) {
                if (callback != null) {
                    callback.onFailure();
                }
            }

            if (callback != null) {
                callback.onSuccess(tweets);
            }
        }

    }
}

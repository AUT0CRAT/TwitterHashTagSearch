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
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

import alim.parkar.twitterwingify.AuthManager;

/**
 * Class that handles Application-only authentication. Performs Login, Saves the token.
 */
public class LoginCommunicator {

    private static final String TAG = "LoginCommunicator";

    private static final String consumerKey = "L2LjOkpjyGpRzIMrqzGfFD2xM";
    private static final String consumerSecret = "UhtUdiF45DsLZBVjS9f0dyx17tIsOlFpKGusahHmFTefbMdR8f";

    private LoginCallback loginCallback;
    private LoginTask mLoginTask;

    public LoginCommunicator() {
    }


    /**
     * The steps to encode an application’s consumer key and secret into a set of credentials to obtain a bearer token are:
     * <ol><li>URL encode the consumer key and the consumer secret according to RFC 1738. Note that at the time of writing, this will not actually change the consumer key and secret, but this step should still be performed in case the format of those values changes in the future.</li>
     * <li>Concatenate the encoded consumer key, a colon character ”:”, and the encoded consumer secret into a single string.</li>
     * <li>Base64 encode the string from the previous step.</li>
     * </ol>
     *
     * @return Returns the Bearer auth token in encoded format. Returns null if encoding fails.
     */
    @Nullable
    private String getBearerAuthToken() {
        try {
            String encodedConsumerKey = URLEncoder.encode(consumerKey, "UTF-8");
            String encodedConsumerSecret = URLEncoder.encode(consumerSecret, "UTF-8");

            Log.d(TAG, "encodedConsumerKey : " + encodedConsumerKey);
            Log.d(TAG, "encodedConsumerSecret : " + encodedConsumerSecret);

            String bearerAuthToken = encodedConsumerKey + ":" + encodedConsumerSecret;
            Log.d(TAG, "bearerAuthToken : " + bearerAuthToken);
            //use Wrap as if we use default, a new line is getting added due to :
            String encodedBearerToken = Base64.encodeToString(bearerAuthToken.getBytes(), Base64.NO_WRAP);
            Log.d(TAG, "Encoded token : " + encodedBearerToken);
            return encodedBearerToken;
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Failed to encode", e);
        }

        return null;
    }

    public void login(LoginCallback callback) {
        this.loginCallback = callback;
        if (mLoginTask != null) {
            mLoginTask.cancel(true);
        }

        mLoginTask = new LoginTask();
        mLoginTask.execute();
    }


    /**
     * Task used to authenticate a twitter login. It saves the token if login is successful
     */
    private class LoginTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            HttpsURLConnection urlConnection = null;
            try {
                URL loginUri = new URL("https://api.twitter.com/oauth2/token");
                final String loginBody = "grant_type=client_credentials";
                urlConnection = (HttpsURLConnection) loginUri.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setRequestMethod("POST");


                urlConnection.setRequestProperty("Authorization", "Basic " + getBearerAuthToken());
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8.");

                OutputStream outputStream = urlConnection.getOutputStream();
                outputStream.write(loginBody.getBytes());
                outputStream.close();

                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String response = reader.readLine();
                reader.close();
                inputStream.close();

                Log.d(TAG, "Response : " + response);

                boolean success = validateResponseAndSaveToken(response);

                Log.i(TAG, "Login Success : " + success);
                return success;
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

            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {

            if (result) {
                if (loginCallback != null) {
                    loginCallback.onLoginSuccess();
                }
            } else {
                if (loginCallback != null) {
                    loginCallback.onLoginFailure();
                }
            }
        }

        /**
         * Validates the response. The response should be of the format
         * <p>{"token_type":"bearer","access_token":"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA%2FAAAAAAAAAAAAAAAAAAAA%3DAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"}
         * </p>
         *
         * @param response The response from twitter auth api
         * @return Returns true if the the response is valid and the Auth token has been saved. Returns false otherwise
         */
        private boolean validateResponseAndSaveToken(String response) {
            if (response == null || response.trim().length() <= 0) {
                return false;
            }

            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(response);
            } catch (JSONException e) {
                Log.e(TAG, "Failed to convert response", e);
                return false;
            }

            String tokenType = jsonObject.optString("token_type", null);
            if (tokenType == null || tokenType.trim().length() <= 0) {
                return false;
            }

            if (!tokenType.equals("bearer")) {
                return false;
            }

            String authToken = jsonObject.optString("access_token", null);
            if (authToken == null || authToken.trim().length() <= 0) {
                return false;
            }

            AuthManager.getAuthManager().saveAuthToken(authToken);

            return true;
        }
    }
}

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

import android.util.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import alim.parkar.twitterwingify.models.Tweet;

/**
 * @author ibasit
 */
public class TwitterSearchParser {

    private static final String TAG = "TwitterSearchParser";

    private static final String STATUSES = "statuses";
    private static final String CREATED_AT = "created_at";
    private static final String ID = "id";
    private static final String TWEET = "text";
    private static final String RETWEET_COUNT = "retweet_count";
    private static final String FAV_COUNT = "favorite_count";

    private static final String USER = "user";
    private static final String USER_NAME = "name";
    private static final String USER_HANDLE = "screen_name";
    private static final String PROFILE_PIC = "profile_image_url_https";


    public List<Tweet> parseResponse(InputStream inputStream) throws IOException {

        JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
        try {
            reader.beginObject();
            String keyName = reader.nextName();
            if (keyName.equals(STATUSES)) {
                return readTweetsArray(reader);
            }
            reader.endObject();
        } finally {
            reader.close();
        }

        return null;
    }

    private List<Tweet> readTweetsArray(JsonReader reader) throws IOException {
        List<Tweet> messages = new ArrayList<>();

        reader.beginArray();
        while (reader.hasNext()) {
            messages.add(readTweet(reader));
        }
        reader.endArray();
        return messages;

    }

    private Tweet readTweet(JsonReader reader) throws IOException {
        long id = -1;
        String tweetText = null;
        String createdAt = null;
        String retweetCount = null;
        String favCount = null;

        User user = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String keyName = reader.nextName();
            switch (keyName) {
                case USER:
                    user = readUser(reader);
                    break;
                case ID:
                    id = reader.nextLong();
                    break;
                case CREATED_AT:
                    createdAt = reader.nextString();
                    break;
                case TWEET:
                    tweetText = reader.nextString();
                    break;
                case FAV_COUNT:
                    favCount = reader.nextString();
                    break;
                case RETWEET_COUNT:
                    retweetCount = reader.nextString();
                    break;
                default:
                    reader.skipValue();
            }
        }
        reader.endObject();

        Tweet tweet = new Tweet(id);
        tweet.setTweet(tweetText);
        tweet.setFavoriteCount(favCount);
        tweet.setRetweetCount(retweetCount);
        tweet.setCreatedAt(createdAt);
        if (user != null) {
            tweet.setTwitterName(user.userName);
            tweet.setTwitterHandle(user.twitterHandle);
            tweet.setProfilePic(user.profilePicUrl);
        }


        return tweet;

    }

    private User readUser(JsonReader reader) throws IOException {
        String name = null;
        String twitterHandle = null;
        String profilePic = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String keyName = reader.nextName();
            switch (keyName) {
                case USER_HANDLE:
                    twitterHandle = reader.nextString();
                    break;
                case USER_NAME:
                    name = reader.nextString();
                    break;
                case PROFILE_PIC:
                    profilePic = reader.nextString();
                    break;
                default:
                    reader.skipValue();
            }
        }
        reader.endObject();

        return new User(name, twitterHandle, profilePic);
    }

    private class User {
        private String userName;
        private String twitterHandle;
        private String profilePicUrl;

        private User(String userName, String twitterHandle, String profilePicUrl) {
            this.userName = userName;
            this.twitterHandle = twitterHandle;
            this.profilePicUrl = profilePicUrl;
        }
    }
}

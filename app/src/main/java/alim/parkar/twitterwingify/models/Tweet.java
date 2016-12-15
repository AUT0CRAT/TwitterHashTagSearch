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

package alim.parkar.twitterwingify.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author ibasit
 */
public class Tweet implements Comparable, Parcelable {

    public static final Creator<Tweet> CREATOR = new Creator<Tweet>() {
        @Override
        public Tweet createFromParcel(Parcel in) {
            return new Tweet(in);
        }

        @Override
        public Tweet[] newArray(int size) {
            return new Tweet[size];
        }
    };
    private long tweetId;
    private String twitterHandle;
    private String twitterName;
    private String tweet;
    private String createdAt;
    private String profilePic;
    private String retweetCount;
    private String favoriteCount;

    public Tweet(long tweetId) {
        this.tweetId = tweetId;
    }

    protected Tweet(Parcel in) {
        tweetId = in.readLong();
        twitterHandle = in.readString();
        twitterName = in.readString();
        tweet = in.readString();
        createdAt = in.readString();
        profilePic = in.readString();
        retweetCount = in.readString();
        favoriteCount = in.readString();
    }

    public long getTweetId() {
        return tweetId;
    }

    public void setTweetId(long tweetId) {
        this.tweetId = tweetId;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getRetweetCount() {
        return retweetCount;
    }

    public void setRetweetCount(String retweetCount) {
        this.retweetCount = retweetCount;
    }

    public String getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(String favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    public String getTwitterHandle() {
        return "@" + twitterHandle;
    }

    public void setTwitterHandle(String twitterHandle) {
        this.twitterHandle = twitterHandle;
    }

    public String getTwitterName() {
        return twitterName;
    }

    public void setTwitterName(String twitterName) {
        this.twitterName = twitterName;
    }

    public String getTweet() {
        return tweet;
    }

    public void setTweet(String tweet) {
        this.tweet = tweet;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public int compareTo(Object compareTo) {

        long compareTweetId = ((Tweet) compareTo).tweetId;

        if (compareTweetId == tweetId) {
            return 0;
        }

        if (compareTweetId > tweetId) {
            return 1;
        }

        return -1;

    }

    @Override
    public int hashCode() {
        return Long.valueOf(tweetId).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof Tweet && ((Tweet) obj).getTweetId() == tweetId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(tweetId);
        parcel.writeString(twitterHandle);
        parcel.writeString(twitterName);
        parcel.writeString(tweet);
        parcel.writeString(createdAt);
        parcel.writeString(profilePic);
        parcel.writeString(retweetCount);
        parcel.writeString(favoriteCount);
    }
}

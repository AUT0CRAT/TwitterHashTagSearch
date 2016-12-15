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

package alim.parkar.twitterwingify.components;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.util.LruCache;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import alim.parkar.twitterwingify.R;
import alim.parkar.twitterwingify.communication.ProfilePicLoader;
import alim.parkar.twitterwingify.models.Tweet;

import static android.content.ComponentCallbacks2.TRIM_MEMORY_BACKGROUND;
import static android.content.ComponentCallbacks2.TRIM_MEMORY_MODERATE;

/**
 * Twitter Feed adapter. Loads the Tweets.
 */
public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder> {

    private static final String TAG = "TweetsAdapter";

    private List<Tweet> data;
    private ProfilePicCache profilePicCache;


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View listItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_feed, parent, false);
        return new ViewHolder(listItemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bindData(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    public void clear() {
        if (data != null) {
            data.clear();
        }
    }

    public void addTweets(List<Tweet> tweets) {
        if (tweets == null) {
            return;
        }

        if (data == null) {
            data = new ArrayList<>(tweets.size());
        }

        if (profilePicCache == null) {
            profilePicCache = new ProfilePicCache();
        }

        Collections.sort(tweets);
        data.addAll(tweets);
        notifyDataSetChanged();
    }

    public ArrayList<Tweet> getAll() {
        return data == null ? null : new ArrayList<>(data);
    }

    public void onTrimMemory(int level) {
        if (level >= TRIM_MEMORY_MODERATE) {
            profilePicCache.evictAll();
        } else if (level >= TRIM_MEMORY_BACKGROUND) {
            profilePicCache.trimToSize(profilePicCache.size() / 2);
        }
    }

    public Tweet getItemAtPosition(int postition) {
        return data == null ? null : data.get(postition);
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvName;
        TextView tvTwitterHandle;
        TextView tvTweet;
        ImageView ivProfilePic;
        TextView tvTime;
        TextView tvRetweets;
        TextView tvFavorite;


        ViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.tvName);
            tvTime = (TextView) itemView.findViewById(R.id.tvTime);
            tvTwitterHandle = (TextView) itemView.findViewById(R.id.tvTwitterHandle);
            tvTweet = (TextView) itemView.findViewById(R.id.tvTweet);
            tvRetweets = (TextView) itemView.findViewById(R.id.tvRetweets);
            tvFavorite = (TextView) itemView.findViewById(R.id.tvFavorite);
            ivProfilePic = (ImageView) itemView.findViewById(R.id.ivProfilePic);
        }

        public void bindData(final Tweet tweet) {
            tvName.setText(tweet.getTwitterName());
            tvTwitterHandle.setText(tweet.getTwitterHandle());
            tvTweet.setText(tweet.getTweet());
            tvFavorite.setText(tweet.getFavoriteCount());
            tvRetweets.setText(tweet.getRetweetCount());
            tvTime.setText(tweet.getCreatedAt());

            Drawable profilePic = profilePicCache.get(tweet.getTweetId());
            if (profilePic != null) {
                ivProfilePic.setImageDrawable(profilePic);
            } else {
                loadProfilePic(tweet);
            }
        }

        private void loadProfilePic(Tweet tweet) {
            new ProfilePicLoader(tweet.getTweetId(), tweet.getProfilePic(), new ProfilePicLoader.Callback() {
                @Override
                public void onProfilePicLoaded(long identifier, Drawable drawable) {
                    ivProfilePic.setImageDrawable(drawable);
                    profilePicCache.put(identifier, drawable);
                }

                @Override
                public void onProfilePicLoadFailed(long identifier) {

                }
            }).execute();
        }
    }

    class ProfilePicCache extends LruCache<Long, Drawable> {

        private static final int MAX_CACHE_SIZE = 10 * 8 * 1024; //10 MB

        public ProfilePicCache() {
            super(MAX_CACHE_SIZE);
        }

        @Override
        protected int sizeOf(Long key, Drawable value) {
            return (sizeOfDrawable(value) / 1024);
        }

        public int sizeOfDrawable(Drawable drawable) {
            if (drawable instanceof BitmapDrawable) {
                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    return bitmap.getAllocationByteCount();
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                    return bitmap.getByteCount();
                } else {
                    return bitmap.getRowBytes() * bitmap.getHeight();
                }
            } else {
                return drawable.getIntrinsicHeight() * drawable.getIntrinsicWidth() * 8;
            }
        }
    }
}

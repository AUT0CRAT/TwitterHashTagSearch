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

package alim.parkar.twitterwingify.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import alim.parkar.twitterwingify.R;
import alim.parkar.twitterwingify.communication.TweetsLoaderTask;
import alim.parkar.twitterwingify.components.TweetsAdapter;
import alim.parkar.twitterwingify.models.Tweet;

/**
 * @author ibasit
 */
public class DashboardActivity extends AppCompatActivity implements View.OnClickListener, TweetsLoaderTask.CallBack, TextView.OnEditorActionListener {

    private static final String TAG = "DashboardActivity";
    private static final String ADAPTER_DATA = "ADAPTER_DATA";
    private static final int VISIBILITY_THRESHOLD = 5;
    private static final String SAVED_QUERY = "SAVED_QUERY";
    private static final String TIMER_RUNNING = "TIMER_RUNNING";

    private Timer fetchLatestTweetsTimer;
    private TweetsAdapter mTweetAdapter;
    private String mSearch;
    private LinearLayoutManager mLayoutManager;
    private EditText etSearch;
    private RecyclerView rvTweetList;
    private View noContent;
    private Snackbar mSnackbar;
    private TweetsLoaderTask mLoaderTask;
    private boolean isLoading;
    private boolean shouldLoad = true;
    private int lastCount = 0;
    private LinearLayout llNewTweets;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dashboard_activity);

        etSearch = (EditText) findViewById(R.id.etSearch);
        ImageView ivSearch = (ImageView) findViewById(R.id.ivSearch);
        rvTweetList = (RecyclerView) findViewById(R.id.rvTweetList);
        noContent = findViewById(R.id.llNoContent);
        llNewTweets = (LinearLayout) findViewById(R.id.llNewTweets);

        mLayoutManager = new LinearLayoutManager(this);
        rvTweetList.setLayoutManager(mLayoutManager);
        rvTweetList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        rvTweetList.setHasFixedSize(true);
        mTweetAdapter = new TweetsAdapter();
        rvTweetList.setAdapter(mTweetAdapter);

        ivSearch.setOnClickListener(this);
        llNewTweets.setOnClickListener(this);
        etSearch.setOnEditorActionListener(this);

        if (savedInstanceState != null) {
            ArrayList savedData = savedInstanceState.<Tweet>getParcelableArrayList(ADAPTER_DATA);
            if (savedData != null) {
                mTweetAdapter.addTweets(savedData);
            }

            mSearch = savedInstanceState.getString(SAVED_QUERY, "");
            boolean timerRunning = savedInstanceState.getBoolean(TIMER_RUNNING, false);
            if (timerRunning) {
                startFetchTimer();
            }
        }

        rvTweetList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int totalItemCount = mTweetAdapter.getItemCount();
                int lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();


                if (!isLoading && totalItemCount <= (lastVisibleItem + VISIBILITY_THRESHOLD) && shouldLoad) {
                    loadMore();
                    isLoading = true;
                }

            }
        });

    }

    private void loadMore() {
        if (mTweetAdapter == null) {
            return;
        }

        int count = mTweetAdapter.getItemCount();
        if (count == 0) {
            return;
        }

        Tweet lastTweet = mTweetAdapter.getItemAtPosition(count - 1);
        if (lastTweet != null) {
            mLoaderTask = new TweetsLoaderTask(0, lastTweet.getTweetId(), mSearch);
            mLoaderTask.load(this);
        }
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.ivSearch) {
            performSearch();
        } else if (id == R.id.llNewTweets) {
            rvTweetList.scrollToPosition(0);
            mTweetAdapter.notifyDataSetChanged();
            showNewTweetsAvailable(false);
        }

    }

    private void performSearch() {
        hideKeyboard(etSearch);
        if (mSnackbar != null && mSnackbar.isShownOrQueued()) {
            mSnackbar.dismiss();
        }

        String query = etSearch.getText().toString();
        if (query.trim().length() == 0) {
            mSnackbar = Snackbar.make(rvTweetList, "Search Cannot be empty", Snackbar.LENGTH_LONG);
            mSnackbar.show();
        }

        mSearch = query;
        searchAndLoad(query);

    }

    protected void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void searchAndLoad(String query) {
        if (query == null || query.isEmpty()) {
            return;
        }
        mTweetAdapter.clear();

        mLoaderTask = new TweetsLoaderTask(query);
        mLoaderTask.load(this);

        if (fetchLatestTweetsTimer != null) {
            fetchLatestTweetsTimer.cancel();
            fetchLatestTweetsTimer.purge();
        }
        startFetchTimer();
    }

    private void startFetchTimer() {
        fetchLatestTweetsTimer = new Timer();
        fetchLatestTweetsTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (mTweetAdapter == null) {
                    return;
                }

                int count = mTweetAdapter.getItemCount();
                if (count == 0) {
                    return;
                }

                Tweet firstTweet = mTweetAdapter.getItemAtPosition(0);
                if (firstTweet != null) {
                    mLoaderTask = new TweetsLoaderTask(firstTweet.getTweetId(), 0, mSearch);
                    mLoaderTask.load(new TweetsLoaderTask.CallBack() {
                        @Override
                        public void onSuccess(List<Tweet> tweets) {
                            Log.i(TAG, "new tweets : " + tweets.size());
                            isLoading = false;
                            if (tweets.size() > 0) {
                                mTweetAdapter.addLatestTweets(tweets);
                                lastCount = mTweetAdapter.getItemCount();

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showNewTweetsAvailable(true);
                                    }
                                });
                            }

                        }

                        @Override
                        public void onFailure() {
                            Toast.makeText(DashboardActivity.this, "Failed to load latest", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }, 3000, 3000);

    }

    private void showNewTweetsAvailable(boolean show) {
        int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
        if (firstVisibleItem == 0 && mTweetAdapter != null) {
            mTweetAdapter.notifyDataSetChanged();
            return;
        }

        if (show) {
            llNewTweets.setVisibility(View.VISIBLE);
        } else {
            llNewTweets.setVisibility(View.GONE);
        }

    }

    @Override
    public void onSuccess(List<Tweet> tweets) {
        mTweetAdapter.addTweets(tweets);
        isLoading = false;
        shouldLoad = lastCount != mTweetAdapter.getItemCount();
        lastCount = mTweetAdapter.getItemCount();

        if (mTweetAdapter.getItemCount() == 0) {
            noContent.setVisibility(View.VISIBLE);
        } else {
            noContent.setVisibility(View.GONE);
        }
    }

    @Override
    public void onFailure() {
        mSnackbar = Snackbar.make(rvTweetList, "Failed to load content", Snackbar.LENGTH_LONG);
        mSnackbar.show();
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            performSearch();
            return true;
        }
        return false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(ADAPTER_DATA, mTweetAdapter.getAll());
        outState.putString(SAVED_QUERY, mSearch);
        outState.putBoolean(TIMER_RUNNING, fetchLatestTweetsTimer != null);
        super.onSaveInstanceState(outState);

    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (mTweetAdapter != null) {
            mTweetAdapter.onTrimMemory(level);
        }
    }

    @Override
    protected void onDestroy() {
        if (mTweetAdapter != null) {
            mTweetAdapter.clear();
            mTweetAdapter = null;
        }

        mSearch = null;
        if (fetchLatestTweetsTimer != null) {
            fetchLatestTweetsTimer.cancel();
            fetchLatestTweetsTimer.purge();
            fetchLatestTweetsTimer = null;
        }
        super.onDestroy();
    }
}

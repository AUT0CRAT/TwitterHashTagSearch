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

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import alim.parkar.twitterwingify.R;
import alim.parkar.twitterwingify.communication.LoginCallback;
import alim.parkar.twitterwingify.communication.LoginCommunicator;

public class InitializationActivity extends AppCompatActivity implements LoginCallback {

    private static final String TASK_IN_PROGRESS = "TASK_IN_PROGRESS";
    private static final String TASK_RESULT = "TASK_RESULT";
    private static final int RESULT_UNKNOWN = 0;
    private static final int RESULT_SUCCESS = 1;
    private static final int RESULT_FAILURE = 2;


    private View progressParent;

    private boolean mTaskInProgress = false;
    private int mTaskResult = 0;
    private boolean mResumed;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initialization);

        progressParent = findViewById(R.id.llProgress);

        if (savedInstanceState != null) {
            mTaskInProgress = savedInstanceState.getBoolean(TASK_IN_PROGRESS, false);
            mTaskResult = savedInstanceState.getInt(TASK_RESULT, RESULT_UNKNOWN);
        }

        if (!mTaskInProgress) {
            if (mTaskResult == RESULT_SUCCESS) {
                proceedToSuccessScreen();
            } else if (mTaskResult == RESULT_FAILURE) {
                showFailureMessage();
            } else {
                LoginCommunicator mLoginCommunicator = new LoginCommunicator();
                mLoginCommunicator.login(this);
                progressParent.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mResumed = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mResumed = true;
    }

    @Override
    public void onLoginSuccess() {
        mTaskInProgress = false;
        mTaskResult = RESULT_SUCCESS;
        if (mResumed) {
            proceedToSuccessScreen();
        }
    }

    private void proceedToSuccessScreen() {
        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }


    @Override
    public void onLoginFailure() {
        mTaskInProgress = false;
        mTaskResult = RESULT_SUCCESS;
        if (mResumed) {
            showFailureMessage();
        }
    }

    private void showFailureMessage() {
        Toast.makeText(this, R.string.error_login_failure, Toast.LENGTH_LONG).show();
        progressParent.setVisibility(View.GONE);
    }
}

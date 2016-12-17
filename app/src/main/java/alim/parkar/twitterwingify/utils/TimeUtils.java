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

package alim.parkar.twitterwingify.utils;

import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author ibasit
 */
public class TimeUtils {

    public static final long ONE_SECOND = 1000;
    public static final long ONE_MINUTE = ONE_SECOND * 60;
    public static final long ONE_HOUR = ONE_MINUTE * 60;
    private static final String TAG = "TimeUtils";

    public static String getDisplayTime(String createdAt) {
        DateFormat format = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH);
        format.setTimeZone(TimeZone.getTimeZone("+530"));
        try {
            Date date = format.parse(createdAt);
            long timeInMilliseconds = date.getTime();
            long currentTime = System.currentTimeMillis();

            long timeDifference = currentTime - timeInMilliseconds;
            if (timeDifference < ONE_MINUTE) {
                return (timeDifference / ONE_SECOND) + "s";
            } else if (timeDifference < ONE_HOUR) {
                return (timeDifference / ONE_MINUTE) + "m";
            } else {
                return (timeDifference / ONE_HOUR) + "h";
            }

        } catch (ParseException e) {
            Log.e(TAG, "Invalid date format", e);
        }

        return "";
    }
}

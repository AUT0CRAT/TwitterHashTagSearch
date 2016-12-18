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

package alim.parkar.twitterwingify;

import android.app.Application;
import android.content.Context;

import java.lang.ref.WeakReference;

/**
 * Application that will be used to save the context and fetch it across the application where context is not available.
 */
public class TwitterApplication extends Application {


    /**
     * As the application is in memory, the weak reference will also be in memory. As soon as the application is killed, the weak reference will also be destroyed, and memory leaks will not occur.
     */
    private static WeakReference<Context> contextWeakReference;

    public static Context getContext() {
        if (contextWeakReference != null) {
            return contextWeakReference.get();
        }

        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        contextWeakReference = new WeakReference<Context>(this);
    }
}

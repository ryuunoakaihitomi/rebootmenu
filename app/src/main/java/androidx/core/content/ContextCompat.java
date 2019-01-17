/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
//CHANGED
package androidx.core.content;

import android.content.Context;
import android.os.Build;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Helper for accessing features in {@link android.content.Context}.
 */
/*public*/ class ContextCompat {

    /**
     * Returns absolute paths to application-specific directories on all
     * external storage devices where the application can place persistent files
     * it owns. These files are internal to the application, and not typically
     * visible to the user as media.
     * <p>
     * This is like {@link Context#getFilesDir()} in that these files will be
     * deleted when the application is uninstalled, however there are some
     * important differences:
     * <ul>
     * <li>External files are not always available: they will disappear if the
     * user mounts the external storage on a computer or removes it.
     * <li>There is no security enforced with these files.
     * </ul>
     * <p>
     * External storage devices returned here are considered a permanent part of
     * the device, including both emulated external storage and physical media
     * slots, such as SD cards in a battery compartment. The returned paths do
     * not include transient devices, such as USB flash drives.
     * <p>
     * An application may store data on any or all of the returned devices. For
     * example, an app may choose to store large files on the device with the
     * most available space, as measured by {@link android.os.StatFs}.
     * <p>
     * Starting in {@link android.os.Build.VERSION_CODES#KITKAT}, no permissions
     * are required to write to the returned paths; they're always accessible to
     * the calling app. Before then,
     * {@link android.Manifest.permission#WRITE_EXTERNAL_STORAGE} is required to
     * write. Write access outside of these paths on secondary external storage
     * devices is not available. To request external storage access in a
     * backwards compatible way, consider using {@code android:maxSdkVersion}
     * like this:
     *
     * <pre class="prettyprint">&lt;uses-permission
     *     android:name="android.permission.WRITE_EXTERNAL_STORAGE"
     *     android:maxSdkVersion="18" /&gt;</pre>
     * <p>
     * The first path returned is the same as
     * {@link Context#getExternalFilesDir(String)}. Returned paths may be
     * {@code null} if a storage device is unavailable.
     *
     * @see Context#getExternalFilesDir(String)
     * @see EnvironmentCompat#getStorageState(File)
     */
    @NonNull
    /*public*/ static File[] getExternalFilesDirs(@NonNull Context context, @Nullable String type) {
        if (Build.VERSION.SDK_INT >= 19) {
            return context.getExternalFilesDirs(type);
        } else {
            return new File[]{context.getExternalFilesDir(type)};
        }
    }

    /**
     * Returns absolute paths to application-specific directories on all
     * external storage devices where the application can place cache files it
     * owns. These files are internal to the application, and not typically
     * visible to the user as media.
     * <p>
     * This is like {@link Context#getCacheDir()} in that these files will be
     * deleted when the application is uninstalled, however there are some
     * important differences:
     * <ul>
     * <li>External files are not always available: they will disappear if the
     * user mounts the external storage on a computer or removes it.
     * <li>There is no security enforced with these files.
     * </ul>
     * <p>
     * External storage devices returned here are considered a permanent part of
     * the device, including both emulated external storage and physical media
     * slots, such as SD cards in a battery compartment. The returned paths do
     * not include transient devices, such as USB flash drives.
     * <p>
     * An application may store data on any or all of the returned devices. For
     * example, an app may choose to store large files on the device with the
     * most available space, as measured by {@link android.os.StatFs}.
     * <p>
     * Starting in {@link android.os.Build.VERSION_CODES#KITKAT}, no permissions
     * are required to write to the returned paths; they're always accessible to
     * the calling app. Before then,
     * {@link android.Manifest.permission#WRITE_EXTERNAL_STORAGE} is required to
     * write. Write access outside of these paths on secondary external storage
     * devices is not available. To request external storage access in a
     * backwards compatible way, consider using {@code android:maxSdkVersion}
     * like this:
     *
     * <pre class="prettyprint">&lt;uses-permission
     *     android:name="android.permission.WRITE_EXTERNAL_STORAGE"
     *     android:maxSdkVersion="18" /&gt;</pre>
     * <p>
     * The first path returned is the same as
     * {@link Context#getExternalCacheDir()}. Returned paths may be {@code null}
     * if a storage device is unavailable.
     *
     * @see Context#getExternalCacheDir()
     * @see EnvironmentCompat#getStorageState(File)
     */
    @NonNull
    /*public*/ static File[] getExternalCacheDirs(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= 19) {
            return context.getExternalCacheDirs();
        } else {
            return new File[]{context.getExternalCacheDir()};
        }
    }
}

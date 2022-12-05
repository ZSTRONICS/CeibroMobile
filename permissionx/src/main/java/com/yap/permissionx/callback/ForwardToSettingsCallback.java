/*
 * Copyright (C)  Digitify, PermissionX Open Source Project
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

package com.yap.permissionx.callback;

import androidx.annotation.NonNull;


import com.yap.permissionx.request.ForwardScope;

import java.util.List;

/**
 * Callback for {@link com.yap.permissionx.request.PermissionBuilder#onForwardToSettings(ForwardToSettingsCallback)} method.
 *
 * @author Digitify
 * @since 2020/6/7
 */
public interface ForwardToSettingsCallback {

    /**
     * Called when you should tell user to allow these permissions in settings.
     * @param scope
     *          Scope to show rationale dialog.
     * @param deniedList
     *          Permissions that should allow in settings.
     */
    void onForwardToSettings(@NonNull ForwardScope scope, @NonNull List<String> deniedList);

}
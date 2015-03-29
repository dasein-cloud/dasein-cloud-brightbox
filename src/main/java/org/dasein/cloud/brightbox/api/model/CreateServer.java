/*
 * Copyright (C) 2009-2015 Dell, Inc.
 * See annotations for authorship information
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */

package org.dasein.cloud.brightbox.api.model;

import com.google.gson.annotations.SerializedName;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by stas on 21/03/2015.
 */
public class CreateServer {
    @SerializedName( "image" )
    private String imageId;
    private String name;
    @SerializedName( "server_type" )
    private String serverTypeId;
    private String zone;
    private String userData;
    @SerializedName( "server_groups" )
    private List<String> serverGroupIds;

    public CreateServer(@Nonnull String imageId) {
        this.imageId = imageId;
    }

    public String getImageId() {
        return imageId;
    }

    public String getName() {
        return name;
    }

    public CreateServer withName(String name) {
        this.name = name;
        return this;
    }

    public String getServerTypeId() {
        return serverTypeId;
    }

    public CreateServer withServerTypeId(String serverTypeId) {
        this.serverTypeId = serverTypeId;
        return this;
    }

    public String getZone() {
        return zone;
    }

    public CreateServer withZone(String zone) {
        this.zone = zone;
        return this;
    }

    public String getUserData() {
        return userData;
    }

    public CreateServer withUserData(String userData) {
        this.userData = userData;
        return this;
    }

    public List<String> getServerGroupIds() {
        return serverGroupIds;
    }

    public CreateServer withServerGroupIds(List<String> serverGroupIds) {
        this.serverGroupIds = serverGroupIds;
        return this;
    }
}

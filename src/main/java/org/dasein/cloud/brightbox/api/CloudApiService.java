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

package org.dasein.cloud.brightbox.api;

import org.dasein.cloud.brightbox.api.model.Image;
import org.dasein.cloud.brightbox.api.model.Server;
import org.dasein.cloud.brightbox.api.model.ServerType;
import org.dasein.cloud.brightbox.api.model.Zone;
import retrofit.http.DELETE;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by stas on 09/02/2015.
 */
public interface CloudApiService {
    String VERSION = "/1.0";

    // zones and regions
    @GET(VERSION + "/zones") List<Zone> listZones();

    // images
    @GET(VERSION + "/images") List<Image> listImages();
    @GET(VERSION + "/images/{id}") Image getImage(@Path("id") String id);
    @DELETE(VERSION + "/images/{id}") void deleteImage(@Path("id") String id);

    // servers
    @FormUrlEncoded
    @POST(VERSION + "/servers")
    Server createServer(@Field("image") @Nonnull String imageId, @Field("name") @Nullable String name, @Field("server_type") @Nullable String serverTypeId, @Field("zone") @Nullable String zone, @Field("user_data") @Nullable String userData, @Field("server_groups") @Nullable List<String> serverGroupIds);

    @GET(VERSION + "/servers/{id}") Server getServer(@Path("id") String id);
    @PUT(VERSION + "/servers/{id}") Server updateServer(@Path("id") String id, @Nullable String name, @Nullable String userData, @Nullable String compatibilityMode);
    @DELETE(VERSION + "/servers/{id}") void deleteServer(@Path("id") String id);
    @POST(VERSION + "/servers/{id}/start") void startServer(@Path("id") String id);
    @POST(VERSION + "/servers/{id}/stop") void stopServer(@Path("id") String id);
    @POST(VERSION + "/servers/{id}/reboot") void rebootServer(@Path("id") String id);
    @POST(VERSION + "/servers/{id}/reset") void resetServer(@Path("id") String id);
    @POST(VERSION + "/servers/{id}/shutdown") void shutdownServer(@Path("id") String id);
    @POST(VERSION + "/servers/{id}/activate_console") void activateConsole(@Path("id") String id);
    @POST(VERSION + "/servers/{id}/snapshot") void snapshotServer(@Path("id") String id);

    // server types
    @GET(VERSION + "/server_types") List<ServerType> listServerTypes();

}

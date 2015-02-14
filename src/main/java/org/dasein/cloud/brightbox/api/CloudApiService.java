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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.dasein.cloud.CloudException;
import org.dasein.cloud.brightbox.api.model.CreateLoadBalancer;
import org.dasein.cloud.brightbox.api.model.Image;
import org.dasein.cloud.brightbox.api.model.LoadBalancer;
import org.dasein.cloud.brightbox.api.model.LoadBalancerHealthcheck;
import org.dasein.cloud.brightbox.api.model.LoadBalancerListener;
import org.dasein.cloud.brightbox.api.model.LoadBalancerNode;
import org.dasein.cloud.brightbox.api.model.Server;
import org.dasein.cloud.brightbox.api.model.ServerType;
import org.dasein.cloud.brightbox.api.model.Zone;
import retrofit.client.Response;
import retrofit.http.Body;
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
    @GET(VERSION + "/zones") List<Zone> listZones() throws CloudException;

    // images
    @GET(VERSION + "/images") List<Image> listImages() throws CloudException;
    @GET(VERSION + "/images/{id}") Image getImage(@Path("id") String id) throws CloudException;
    @DELETE(VERSION + "/images/{id}") Response deleteImage(@Path("id") String id) throws CloudException;

    // servers
    @FormUrlEncoded
    @POST(VERSION + "/servers")
    Server createServer(@Field("image") @Nonnull String imageId, @Field("name") @Nullable String name, @Field("server_type") @Nullable String serverTypeId, @Field("zone") @Nullable String zone, @Field("user_data") @Nullable String userData, @Field("server_groups") @Nullable List<String> serverGroupIds) throws CloudException;

    @GET(VERSION + "/servers/{id}") Server getServer(@Path("id") String id) throws CloudException;
    @PUT(VERSION + "/servers/{id}") Server updateServer(@Path("id") String id, @Nullable String name, @Nullable String userData, @Nullable String compatibilityMode) throws CloudException;
    @DELETE(VERSION + "/servers/{id}") Response deleteServer(@Path("id") String id) throws CloudException;
    @POST(VERSION + "/servers/{id}/start") Response startServer(@Path("id") String id) throws CloudException;
    @POST(VERSION + "/servers/{id}/stop") Response stopServer(@Path("id") String id) throws CloudException;
    @POST(VERSION + "/servers/{id}/reboot") Response rebootServer(@Path("id") String id) throws CloudException;
    @POST(VERSION + "/servers/{id}/reset") Response resetServer(@Path("id") String id) throws CloudException;
    @POST(VERSION + "/servers/{id}/shutdown") Response shutdownServer(@Path("id") String id) throws CloudException;
    @POST(VERSION + "/servers/{id}/activate_console") Response activateConsole(@Path("id") String id) throws CloudException;
    @POST(VERSION + "/servers/{id}/snapshot") Response snapshotServer(@Path("id") String id) throws CloudException;

    // server types
    @GET(VERSION + "/server_types") List<ServerType> listServerTypes() throws CloudException;

    // load balancers
    @GET(VERSION + "/load_balancers") List<LoadBalancer> listLoadBalancers() throws CloudException;
    @POST(VERSION + "/load_balancers")
    LoadBalancer createLoadBalancer(@Body CreateLoadBalancer loadBalancer) throws CloudException;
    @FormUrlEncoded
    @POST(VERSION + "/load_balancers")
    LoadBalancer createLoadBalancer(
            @Field("name") @Nullable String name,
            @Field("nodes") @Nonnull String nodes,
            @Field("policy") @Nullable String policy,
            @Field("certificate_pem") @Nullable String certificatePem,
            @Field("certificate_key") @Nullable String certificateKey,
            @Field("sslv3") boolean sslv3,
            @Field("listeners") @Nonnull JsonArray listeners,
            @Field("healthcheck") @Nonnull JsonObject healthcheck,
            @Field("buffer_size") Integer bufferSize) throws CloudException;
    @GET(VERSION + "/load_balancers/{id}") LoadBalancer getLoadBalancer(@Path("id") String id) throws CloudException;
    @FormUrlEncoded
    @PUT(VERSION + "/load_balancers/{id}")
    LoadBalancer updateLoadBalancer(
            @Path("id") @Nonnull String id,
            @Field("name") @Nullable String name,
            @Field("nodes") @Nullable List<LoadBalancerNode> nodes,
            @Field("policy") @Nullable String policy,
            @Field("certificate_pem") @Nullable String certificatePem,
            @Field("certificate_key") @Nullable String certificateKey,
            @Field("sslv3") boolean sslv3,
            @Field("listeners") @Nullable List<LoadBalancerListener> listeners,
            @Field("healthcheck") @Nullable LoadBalancerHealthcheck healthcheck,
            @Field("buffer_size") Integer bufferSize) throws CloudException;
    @FormUrlEncoded
    @POST(VERSION + "/load_balancers/{id}/add_nodes")
    LoadBalancer addNodesToLoadBalancer(
            @Path("id") @Nonnull String id,
            @Field("nodes") @Nonnull List<Server> nodes) throws CloudException;
    @FormUrlEncoded
    @POST(VERSION + "/load_balancers/{id}/remove_nodes")
    LoadBalancer removeNodesFromLoadBalancer(
            @Path("id") @Nonnull String id,
            @Field("nodes") @Nonnull List<Server> nodes) throws CloudException;
    @FormUrlEncoded
    @POST(VERSION + "/load_balancers/{id}/add_listeners")
    LoadBalancer addListenersToLoadBalancer(
            @Path("id") @Nonnull String id,
            @Field("listeners") @Nonnull List<LoadBalancerListener> listeners) throws CloudException;
    @FormUrlEncoded
    @POST(VERSION + "/load_balancers/{id}/remove_listeners")
    LoadBalancer removeListenersFromLoadBalancer(
            @Path("id") @Nonnull String id,
            @Field("listeners") @Nonnull List<LoadBalancerListener> listeners) throws CloudException;
    @DELETE(VERSION + "/load_balancers/{id}") Response deleteLoadBalancer(@Path("id") String id) throws CloudException;

}

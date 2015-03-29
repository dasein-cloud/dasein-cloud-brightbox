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

import org.dasein.cloud.CloudException;
import org.dasein.cloud.brightbox.api.model.CloudIp;
import org.dasein.cloud.brightbox.api.model.CloudIpDestination;
import org.dasein.cloud.brightbox.api.model.CreateCloudIp;
import org.dasein.cloud.brightbox.api.model.CreateFirewallRule;
import org.dasein.cloud.brightbox.api.model.CreateLoadBalancer;
import org.dasein.cloud.brightbox.api.model.CreateServer;
import org.dasein.cloud.brightbox.api.model.DatabaseServerType;
import org.dasein.cloud.brightbox.api.model.FirewallPolicy;
import org.dasein.cloud.brightbox.api.model.FirewallRule;
import org.dasein.cloud.brightbox.api.model.Image;
import org.dasein.cloud.brightbox.api.model.LoadBalancer;
import org.dasein.cloud.brightbox.api.model.Server;
import org.dasein.cloud.brightbox.api.model.ServerGroup;
import org.dasein.cloud.brightbox.api.model.ServerGroupServer;
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
    final String SERVERS = VERSION + "/servers";
    final String SERVERS_ID = SERVERS + "/{id}";
    @FormUrlEncoded
    @POST(SERVERS)
    Server createServer(@Field("image") @Nonnull String imageId, @Field("name") @Nullable String name, @Field("server_type") @Nullable String serverTypeId, @Field("zone") @Nullable String zone, @Field("user_data") @Nullable String userData, @Field("server_groups") @Nullable List<String> serverGroupIds) throws CloudException;
    @POST(SERVERS) Server createServer(@Body CreateServer server) throws CloudException;

    @GET(SERVERS) List<Server> listServers() throws CloudException;
    @GET(SERVERS_ID) Server getServer(@Path("id") String id) throws CloudException;
    @PUT(SERVERS_ID) Server updateServer(@Path("id") String id, @Nullable String name, @Nullable String userData, @Nullable String compatibilityMode) throws CloudException;
    @DELETE(SERVERS_ID) Response deleteServer(@Path("id") String id) throws CloudException;
    @POST(SERVERS_ID + "/start") Response startServer(@Path("id") String id) throws CloudException;
    @POST(SERVERS_ID + "/stop") Response stopServer(@Path("id") String id) throws CloudException;
    @POST(SERVERS_ID + "/reboot") Response rebootServer(@Path("id") String id) throws CloudException;
    @POST(SERVERS_ID + "/reset") Response resetServer(@Path("id") String id) throws CloudException;
    @POST(SERVERS_ID + "/shutdown") Response shutdownServer(@Path("id") String id) throws CloudException;
    @POST(SERVERS_ID + "/activate_console") Response activateConsole(@Path("id") String id) throws CloudException;
    @POST(SERVERS_ID + "/snapshot") Response snapshotServer(@Path("id") String id) throws CloudException;

    // server types
    @GET(VERSION + "/server_types") List<ServerType> listServerTypes() throws CloudException;

    // server groups
    final String SERVER_GROUPS = VERSION + "/server_groups";
    final String SERVER_GROUPS_ID = SERVER_GROUPS + "/{id}";
    @GET(SERVER_GROUPS) List<ServerGroup> listServerGroups() throws CloudException;
    @FormUrlEncoded
    @POST(SERVER_GROUPS) ServerGroup createServerGroup(@Field("name") @Nullable String name, @Field("description") @Nullable String description) throws CloudException;
    @GET(SERVER_GROUPS_ID) ServerGroup getServerGroup(@Path("id") @Nonnull String id) throws CloudException;
    @POST(SERVER_GROUPS_ID + "/add_servers") ServerGroup addServersToGroup(@Path("id") @Nonnull String id, @Body @Nonnull List<ServerGroupServer> servers) throws CloudException;
    @FormUrlEncoded
    @POST(SERVER_GROUPS_ID + "/remove_servers") ServerGroup removeServersFromGroup(@Path("id") @Nonnull String id, @Field("servers") @Nonnull List<ServerGroupServer> servers) throws CloudException;
    @DELETE(SERVER_GROUPS_ID) ServerGroup deleteServerGroup(@Path("id") @Nonnull String id) throws CloudException;

    // load balancers
    @GET(VERSION + "/load_balancers") List<LoadBalancer> listLoadBalancers() throws CloudException;
    @POST(VERSION + "/load_balancers")
    LoadBalancer createLoadBalancer(@Body CreateLoadBalancer loadBalancer) throws CloudException;
    @GET(VERSION + "/load_balancers/{id}") LoadBalancer getLoadBalancer(@Path("id") @Nonnull String id) throws CloudException;
    @PUT(VERSION + "/load_balancers/{id}")
    LoadBalancer updateLoadBalancer(@Path("id") @Nonnull String id, @Body @Nonnull CreateLoadBalancer loadBalancer) throws CloudException;
    @FormUrlEncoded
    @POST(VERSION + "/load_balancers/{id}/add_nodes")
    LoadBalancer addNodesToLoadBalancer(
            @Path("id") @Nonnull String id,
            @Body @Nonnull CreateLoadBalancer loadBalancer) throws CloudException;
    @POST(VERSION + "/load_balancers/{id}/remove_nodes")
    LoadBalancer removeNodesFromLoadBalancer(
            @Path("id") @Nonnull String id,
            @Body @Nonnull CreateLoadBalancer loadBalancer) throws CloudException;
    @POST(VERSION + "/load_balancers/{id}/add_listeners")
    LoadBalancer addListenersToLoadBalancer(
            @Path("id") @Nonnull String id,
            @Body @Nonnull CreateLoadBalancer loadBalancer) throws CloudException;
    @POST(VERSION + "/load_balancers/{id}/remove_listeners")
    LoadBalancer removeListenersFromLoadBalancer(
            @Path("id") @Nonnull String id,
            @Body @Nonnull CreateLoadBalancer loadBalancer) throws CloudException;
    @DELETE(VERSION + "/load_balancers/{id}")
    Response deleteLoadBalancer(@Path("id") @Nonnull String id) throws CloudException;

    // cloud ips
    @GET(VERSION + "/cloud_ips")
    List<CloudIp> listCloudIps() throws CloudException;
    @POST(VERSION + "/cloud_ips")
    CloudIp createCloudIp(@Body @Nonnull CreateCloudIp cloudIp) throws CloudException;
    @GET(VERSION + "/cloud_ips/{id}")
    CloudIp getCloudIp(@Path("id") @Nonnull String id) throws CloudException;
    @PUT(VERSION + "/cloud_ips/{id}")
    CloudIp updateCloudIp(@Path("id") @Nonnull String id, @Body @Nonnull CreateCloudIp cloudIp) throws CloudException;
    @DELETE(VERSION + "/cloud_ips/{id}")
    Response deleteCloudIp(@Path("id") @Nonnull String id) throws CloudException;
    @POST(VERSION + "/cloud_ips/{id}/map")
    Response mapCloudIp(@Path("id") @Nonnull String id, @Body @Nonnull CloudIpDestination destination) throws CloudException;
    @POST(VERSION + "/cloud_ips/{id}/unmap")
    Response unmapCloudIp(@Path("id") @Nonnull String id) throws CloudException;

    // database server types
    @GET(VERSION + "/database_types")
    List<DatabaseServerType> listDatabaseServerTypes() throws CloudException;
    @GET(VERSION + "/database_types/{id}")
    DatabaseServerType getDatabaseServerType(@Path("id") @Nonnull String id) throws CloudException;

    // firewall
    final String FIREWALL_POLICIES = VERSION + "/firewall_policies";
    final String FIREWALL_POLICIES_ID = FIREWALL_POLICIES + "/{id}";
    @GET(FIREWALL_POLICIES)
    List<FirewallPolicy> listFirewallPolicies() throws CloudException;
    @GET(FIREWALL_POLICIES_ID)
    FirewallPolicy getFirewallPolicy(@Path("id") @Nonnull String id) throws CloudException;
    @PUT(FIREWALL_POLICIES_ID)
    FirewallPolicy updateFirewallPolicy(@Path("id") @Nonnull String id, @Body @Nonnull FirewallPolicy policy) throws CloudException;
    @POST(FIREWALL_POLICIES)
    FirewallPolicy createFirewallPolicy(@Body @Nonnull FirewallPolicy policy) throws CloudException;
    @DELETE(FIREWALL_POLICIES_ID)
    Response deleteFirewallPolicy(@Path("id") @Nonnull String id) throws CloudException;
    @POST(FIREWALL_POLICIES_ID + "/apply_to") @FormUrlEncoded
    FirewallPolicy applyFirewallPolicyToServerGroup(@Path("id") String id, @Field("server_group") @Nonnull String serverGroupId) throws CloudException;

    final String FIREWALL_RULES = VERSION + "/firewall_rules";
    final String FIREWALL_RULES_ID = FIREWALL_RULES + "/{id}";
    @GET(FIREWALL_RULES_ID)
    FirewallRule getFirewallRule(@Path("id") @Nonnull String id) throws CloudException;
    @POST(FIREWALL_RULES)
    FirewallRule createFirewallRule(@Body @Nonnull CreateFirewallRule rule) throws CloudException;
    @PUT(FIREWALL_RULES_ID)
    FirewallRule updateFirewallRule(@Path("id") @Nonnull String id, @Body @Nonnull FirewallRule rule) throws CloudException;
    @DELETE(FIREWALL_RULES_ID)
    Response deleteFirewallRule(@Path("id") @Nonnull String id) throws CloudException;
}

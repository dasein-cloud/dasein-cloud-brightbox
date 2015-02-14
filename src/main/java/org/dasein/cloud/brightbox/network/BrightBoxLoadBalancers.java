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

package org.dasein.cloud.brightbox.network;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.ResourceStatus;
import org.dasein.cloud.brightbox.BrightBoxCloud;
import org.dasein.cloud.brightbox.api.model.LoadBalancerHealthcheck;
import org.dasein.cloud.brightbox.api.model.LoadBalancerListener;
import org.dasein.cloud.brightbox.api.model.LoadBalancerNode;
import org.dasein.cloud.brightbox.api.model.Server;
import org.dasein.cloud.compute.VmState;
import org.dasein.cloud.network.AbstractLoadBalancerSupport;
import org.dasein.cloud.network.HealthCheckFilterOptions;
import org.dasein.cloud.network.HealthCheckOptions;
import org.dasein.cloud.network.LbEndpointType;
import org.dasein.cloud.network.LbListener;
import org.dasein.cloud.network.LbProtocol;
import org.dasein.cloud.network.LoadBalancer;
import org.dasein.cloud.network.LoadBalancerCapabilities;
import org.dasein.cloud.network.LoadBalancerCreateOptions;
import org.dasein.cloud.network.LoadBalancerEndpoint;
import org.dasein.cloud.network.LoadBalancerHealthCheck;
import org.dasein.cloud.network.LoadBalancerSupport;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by stas on 13/02/2015.
 */
public class BrightBoxLoadBalancers extends AbstractLoadBalancerSupport<BrightBoxCloud> {

    public BrightBoxLoadBalancers(BrightBoxCloud provider) {
        super(provider);
    }

    private transient volatile BrightBoxLoadBalancerCapabilities capabilities;

    @Override
    public @Nonnull LoadBalancerCapabilities getCapabilities() throws CloudException, InternalException {
        if( capabilities == null ) {
            capabilities = new BrightBoxLoadBalancerCapabilities(getProvider());
        }
        return capabilities;
    }

    @Override
    public boolean isSubscribed() throws CloudException, InternalException {
        return false;
    }

    private @Nullable String toBBProtocol(@Nonnull LbProtocol protocol) {
        switch (protocol) {
            case RAW_TCP:
                return "tcp";
            case HTTP:
                return "http";
            case WS:
                return "http+ws";
        }
        return null;
    }


    @Override
    public @Nonnull String createLoadBalancer(@Nonnull LoadBalancerCreateOptions options) throws CloudException, InternalException {
        JsonArray nodes = new JsonArray();
        for( LoadBalancerEndpoint endpoint : options.getEndpoints() ) {
            if( LbEndpointType.VM.equals(endpoint.getEndpointType()) ) {
                JsonObject ob = new JsonObject();
                ob.addProperty("node", endpoint.getEndpointValue());
                nodes.add(ob);
                break;
            }
        }
        JsonArray listeners = new JsonArray();
        for( LbListener listener : options.getListeners() ) {
            JsonObject ob = new JsonObject();
            ob.addProperty("protocol", toBBProtocol(listener.getNetworkProtocol()));
            ob.addProperty("in", listener.getPrivatePort());
            ob.addProperty("out", listener.getPublicPort());
            if( options.getLbAttributesOptions() != null ) {
                ob.addProperty("timeout", options.getLbAttributesOptions().getIdleConnectionTimeout());
            }
            listeners.add(ob);
        }
        HealthCheckOptions hco = options.getHealthCheckOptions();
        JsonObject healthcheck = null;
        if( hco != null ) {
            healthcheck = new JsonObject();
            healthcheck.addProperty("timeout", hco.getTimeout());
            healthcheck.addProperty("interval", hco.getInterval());
            healthcheck.addProperty("port", hco.getPort());
            healthcheck.addProperty("request", hco.getPath());
            healthcheck.addProperty("threshold_down", hco.getUnhealthyCount());
            healthcheck.addProperty("threshold_up", hco.getHealthyCount());
            healthcheck.addProperty("type", hco.getProtocol().equals(LoadBalancerHealthCheck.HCProtocol.HTTPS) ? "https" : "http");
        }
        String nodesString = nodes.toString();
        org.dasein.cloud.brightbox.api.model.LoadBalancer lb = getProvider().getCloudApiService().createLoadBalancer(options.getName(), nodesString, null, null, null, false, listeners, healthcheck, null);

        return lb.getId();
    }

    @Override public LoadBalancer getLoadBalancer(@Nonnull String loadBalancerId) throws CloudException, InternalException {
        return super.getLoadBalancer(loadBalancerId);
    }

    @Nonnull @Override public Iterable<LoadBalancer> listLoadBalancers() throws CloudException, InternalException {
        return super.listLoadBalancers();
    }

    @Nonnull @Override public Iterable<ResourceStatus> listLoadBalancerStatus() throws CloudException, InternalException {
        return super.listLoadBalancerStatus();
    }

    @Nonnull @Override public Iterable<LoadBalancerEndpoint> listEndpoints(@Nonnull String forLoadBalancerId) throws CloudException, InternalException {
        return super.listEndpoints(forLoadBalancerId);
    }

    @Nonnull @Override public Iterable<LoadBalancerEndpoint> listEndpoints(@Nonnull String forLoadBalancerId, @Nonnull LbEndpointType type, @Nonnull String... endpoints) throws CloudException, InternalException {
        return super.listEndpoints(forLoadBalancerId, type, endpoints);
    }

    @Override public void removeLoadBalancer(@Nonnull String loadBalancerId) throws CloudException, InternalException {
        super.removeLoadBalancer(loadBalancerId);
    }

    @Override public void removeServers(@Nonnull String fromLoadBalancerId, @Nonnull String... serverIdsToRemove) throws CloudException, InternalException {
        super.removeServers(fromLoadBalancerId, serverIdsToRemove);
    }

    @Override public LoadBalancerHealthCheck createLoadBalancerHealthCheck(@Nonnull HealthCheckOptions options) throws CloudException, InternalException {
        return super.createLoadBalancerHealthCheck(options);
    }

    @Override public LoadBalancerHealthCheck createLoadBalancerHealthCheck(@Nullable String name, @Nullable String description, @Nullable String host, @Nullable LoadBalancerHealthCheck.HCProtocol protocol, int port, @Nullable String path, int interval, int timeout, int healthyCount, int unhealthyCount) throws CloudException, InternalException {
        return super.createLoadBalancerHealthCheck(name, description, host, protocol, port, path, interval, timeout, healthyCount, unhealthyCount);
    }

    @Override public LoadBalancerHealthCheck getLoadBalancerHealthCheck(@Nonnull String providerLBHealthCheckId, @Nullable String providerLoadBalancerId) throws CloudException, InternalException {
        return super.getLoadBalancerHealthCheck(providerLBHealthCheckId, providerLoadBalancerId);
    }

    @Override public Iterable<LoadBalancerHealthCheck> listLBHealthChecks(@Nullable HealthCheckFilterOptions opts) throws CloudException, InternalException {
        return super.listLBHealthChecks(opts);
    }

    @Override public void removeLoadBalancerHealthCheck(@Nonnull String providerLoadBalancerId) throws CloudException, InternalException {
        super.removeLoadBalancerHealthCheck(providerLoadBalancerId);
    }
}

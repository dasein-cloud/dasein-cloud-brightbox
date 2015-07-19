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

import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.OperationNotSupportedException;
import org.dasein.cloud.ResourceStatus;
import org.dasein.cloud.brightbox.BrightBoxCloud;
import org.dasein.cloud.brightbox.api.model.CreateLoadBalancer;
import org.dasein.cloud.brightbox.api.model.LoadBalancerHealthcheck;
import org.dasein.cloud.brightbox.api.model.LoadBalancerListener;
import org.dasein.cloud.brightbox.api.model.LoadBalancerNode;
import org.dasein.cloud.brightbox.api.model.Server;
import org.dasein.cloud.brightbox.api.model.Zone;
import org.dasein.cloud.dc.DataCenter;
import org.dasein.cloud.network.AbstractLoadBalancerSupport;
import org.dasein.cloud.network.HealthCheckFilterOptions;
import org.dasein.cloud.network.HealthCheckOptions;
import org.dasein.cloud.network.LbAlgorithm;
import org.dasein.cloud.network.LbEndpointState;
import org.dasein.cloud.network.LbEndpointType;
import org.dasein.cloud.network.LbListener;
import org.dasein.cloud.network.LbProtocol;
import org.dasein.cloud.network.LbType;
import org.dasein.cloud.network.LoadBalancer;
import org.dasein.cloud.network.LoadBalancerAddressType;
import org.dasein.cloud.network.LoadBalancerCapabilities;
import org.dasein.cloud.network.LoadBalancerCreateOptions;
import org.dasein.cloud.network.LoadBalancerEndpoint;
import org.dasein.cloud.network.LoadBalancerHealthCheck;
import org.dasein.cloud.network.LoadBalancerState;
import org.dasein.cloud.network.SSLCertificate;
import org.dasein.cloud.network.SSLCertificateCreateOptions;
import org.dasein.cloud.network.SetLoadBalancerSSLCertificateOptions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by stas on 13/02/2015.
 */
public class BrightBoxLoadBalancerSupport extends AbstractLoadBalancerSupport<BrightBoxCloud> {

    public BrightBoxLoadBalancerSupport(BrightBoxCloud provider) {
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
    public void addServers(@Nonnull String toLoadBalancerId, @Nonnull String... serverIdsToAdd) throws CloudException, InternalException {
        List<LoadBalancerNode> nodes = new ArrayList<LoadBalancerNode>();
        for( String serverId : serverIdsToAdd ) {
            nodes.add(new LoadBalancerNode(serverId));
        }
        CreateLoadBalancer clb = new CreateLoadBalancer();
        clb.setNodes(nodes);
        getProvider().getCloudApiService().addNodesToLoadBalancer(toLoadBalancerId, clb);
    }

    @Override
    public void addListeners(@Nonnull String toLoadBalancerId, @Nullable LbListener[] listeners) throws CloudException, InternalException {
        // TODO: fix in core, this parameter should not be nullable
        if( listeners == null ) {
            return;
        }
        List<LoadBalancerListener> listenerList = new ArrayList<LoadBalancerListener>();
        for( LbListener listener : listeners ) {
            listenerList.add(new LoadBalancerListener(listener.getPrivatePort(), listener.getPublicPort(), toBBProtocol(listener.getNetworkProtocol()), null));
        }
        CreateLoadBalancer clb = new CreateLoadBalancer();
        clb.setListeners(listenerList);
        getProvider().getCloudApiService().addListenersToLoadBalancer(toLoadBalancerId, clb);
    }

    @Override
    public boolean isSubscribed() throws CloudException, InternalException {
        try {
            getProvider().getCloudApiService().listLoadBalancers();
            return true;
        } catch( CloudException e ) {
            if( e.getHttpCode() == 403 && "Action forbidden".equalsIgnoreCase(e.getProviderCode()) ) {
                return false;
            }
            throw e;
        }
    }

    private @Nullable String toBBAlgorithm(LbAlgorithm algorithm) {
        switch( algorithm ) {
            case LEAST_CONN:
                return "least-connections";
            case ROUND_ROBIN:
                return "round-robin";
        }
        return null;
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

    private @Nullable LbProtocol toLbProtocol(@Nonnull String protocol) {
        if( "tcp".equals(protocol) ) {
            return LbProtocol.RAW_TCP;
        }
        else if( "http".equals(protocol) ) {
            return LbProtocol.HTTP;
        }
        else if( "http+ws".equals(protocol) ) {
            return LbProtocol.WS;
        }
        return null;
    }


    @Override
    public @Nonnull String createLoadBalancer(@Nonnull LoadBalancerCreateOptions options) throws CloudException, InternalException {
        List<LoadBalancerNode> nodes = new ArrayList<LoadBalancerNode>();
        for( LoadBalancerEndpoint endpoint : options.getEndpoints() ) {
            if( LbEndpointType.VM.equals(endpoint.getEndpointType()) ) {
                nodes.add(new LoadBalancerNode(endpoint.getEndpointValue()));
            }
        }
        List<LoadBalancerListener> listeners = new ArrayList<LoadBalancerListener>();
        for( LbListener l : options.getListeners() ) {
            LoadBalancerListener listener = new LoadBalancerListener(l.getPrivatePort(), l.getPublicPort(), toBBProtocol(l.getNetworkProtocol()), null);
            if( options.getLbAttributesOptions() != null ) {
                listener.setTimeout(options.getLbAttributesOptions().getIdleConnectionTimeout());
            }
            listeners.add(listener);
        }
        HealthCheckOptions hco = options.getHealthCheckOptions();
        LoadBalancerHealthcheck healthcheck = null;
        if( hco != null ) {
            healthcheck = new LoadBalancerHealthcheck();
            healthcheck.setTimeout(hco.getTimeout());
            healthcheck.setInterval(hco.getInterval());
            healthcheck.setPort(hco.getPort());
            healthcheck.setRequest(hco.getPath());
            healthcheck.setThresholdDown(hco.getUnhealthyCount());
            healthcheck.setThresholdUp(hco.getHealthyCount());
            healthcheck.setType(hco.getProtocol().equals(LoadBalancerHealthCheck.HCProtocol.HTTPS) ? "https" : "http");
        }
        CreateLoadBalancer clb = new CreateLoadBalancer();
        clb.setName(options.getName());
        clb.setNodes(nodes);
        clb.setHealthcheck(healthcheck);
        clb.setListeners(listeners);
        org.dasein.cloud.brightbox.api.model.LoadBalancer lb = getProvider().getCloudApiService().createLoadBalancer(clb);
        return lb.getId();
    }

    @Override
    public @Nullable LoadBalancer getLoadBalancer(@Nonnull String loadBalancerId) throws CloudException, InternalException {
        try{
            return toLoadBalancer(getProvider().getCloudApiService().getLoadBalancer(loadBalancerId));
        } catch( CloudException e ) {
            if( e.getHttpCode() == 404 ) {
                return null;
            }
            throw e;
        }
    }

    private LoadBalancer toLoadBalancer(org.dasein.cloud.brightbox.api.model.LoadBalancer lb) throws InternalException, CloudException {
        List<LbListener> listeners = new ArrayList<LbListener>();
        int[] ports = new int[lb.getListeners().size()];
        int i = 0;
        for( LoadBalancerListener l : lb.getListeners() ) {
            ports[i] = l.getOut();
            listeners.add(LbListener.getInstance(toLbProtocol(l.getProtocol()), l.getOut(), l.getIn()));
            i++;
        }
//        Map<String, String> dataCenters = new HashMap<String, String>();
//        for( Server s : lb.getNodes() ) {
//            dataCenters.put(s.getZone().getId(), s.getZone().getId());
//        }
        return LoadBalancer.getInstance(lb.getAccount().getId(), getContext().getRegionId(), lb.getId(), toLoadBalancerState(lb.getStatus()), lb.getName(), lb.getName(), LbType.INTERNAL,
LoadBalancerAddressType.IP, lb.getUrl(), lb.getId(), ports).withListeners(listeners.toArray(new LbListener[listeners.size()]));
//        .operatingIn(dataCenters.keySet().toArray(new String[dataCenters.size()]));
    }

    private DataCenter toDataCenter(Zone zone) throws CloudException, InternalException {
        return new DataCenter(zone.getId(), zone.getHandle(), getContext().getRegionId(), true, true);
    };

    private LoadBalancerState toLoadBalancerState(String status) {
        if( "active".equals(status) ) {
            return LoadBalancerState.ACTIVE;
        }
        // TODO: I think load balancers deserve an error state
        else if( "deleted".equals(status) || "failed".equals(status) ) {
            return LoadBalancerState.TERMINATED;
        }
        else if( "creating".equals(status) || "deleting".equals(status) || "failing".equals(status) ) {
            return LoadBalancerState.PENDING;
        }
        return null;
    }

    @Override
    public @Nonnull Iterable<LoadBalancer> listLoadBalancers() throws CloudException, InternalException {
        List<LoadBalancer> loadBalancers = new ArrayList<LoadBalancer>();
        for( org.dasein.cloud.brightbox.api.model.LoadBalancer lb : getProvider().getCloudApiService().listLoadBalancers() ) {
            loadBalancers.add(toLoadBalancer(lb));
        }
        return loadBalancers;
    }

    @Override
    public @Nonnull Iterable<ResourceStatus> listLoadBalancerStatus() throws CloudException, InternalException {
        List<ResourceStatus> statuses = new ArrayList<ResourceStatus>();
        for( LoadBalancer lb : listLoadBalancers() ) {
            statuses.add(new ResourceStatus(lb.getProviderLoadBalancerId(), lb.getCurrentState()));
        }
        return statuses;
    }

    @Override
    public @Nonnull Iterable<LoadBalancerEndpoint> listEndpoints(@Nonnull String forLoadBalancerId) throws CloudException, InternalException {
        org.dasein.cloud.brightbox.api.model.LoadBalancer lb = getProvider().getCloudApiService().getLoadBalancer(forLoadBalancerId);
        return toLoadBalancerEndpoints(lb.getNodes(), null);
    }

    private Iterable<LoadBalancerEndpoint> toLoadBalancerEndpoints(@Nonnull List<Server> nodes, @Nullable String [] filterIds) {
        List<LoadBalancerEndpoint> endpoints = new ArrayList<LoadBalancerEndpoint>();
        for( Server server : nodes ) {
            boolean matches = true;
            if( filterIds != null ) {
                matches = Arrays.binarySearch(filterIds, server.getId(), new Comparator<String>() {
                    @Override public int compare(String o1, String o2) {
                        return o1.compareTo(o2);
                    }
                }) >= 0;
            }
            if( matches ) {
                endpoints.add(LoadBalancerEndpoint.getInstance(LbEndpointType.VM, server.getId(), "active".equals(server.getStatus()) ? LbEndpointState.ACTIVE : LbEndpointState.INACTIVE));
            }
        }
        return endpoints;
    }

    @Override
    public @Nonnull Iterable<LoadBalancerEndpoint> listEndpoints(@Nonnull String forLoadBalancerId, @Nonnull LbEndpointType type, @Nonnull String... endpoints) throws CloudException, InternalException {
        if( LbEndpointType.VM.equals(type) ) {
            org.dasein.cloud.brightbox.api.model.LoadBalancer lb = getProvider().getCloudApiService().getLoadBalancer(forLoadBalancerId);
            return toLoadBalancerEndpoints(lb.getNodes(), endpoints);
        }
        else {
            return Collections.emptyList();
        }
    }

    @Override
    public void removeLoadBalancer(@Nonnull String loadBalancerId) throws CloudException, InternalException {
        getProvider().getCloudApiService().deleteLoadBalancer(loadBalancerId);
    }

    private CreateLoadBalancer toCreateLoadBalancer(org.dasein.cloud.brightbox.api.model.LoadBalancer lb) {
        CreateLoadBalancer clb = new CreateLoadBalancer();
        clb.setName(lb.getName());
        clb.setListeners(lb.getListeners());
        clb.setHealthcheck(lb.getHealthcheck());
        clb.setBufferSize(lb.getBufferSize());
        clb.setPolicy(lb.getPolicy());
        List<LoadBalancerNode> nodes = new ArrayList<LoadBalancerNode>();
        for( Server server : lb.getNodes() ) {
            nodes.add(new LoadBalancerNode(server.getId()));
        }
        clb.setNodes(nodes);
        return clb;
    }

    @Override
    public void removeListeners(@Nonnull String fromLoadBalancerId, @Nullable LbListener[] listeners) throws CloudException, InternalException {
        List<LoadBalancerListener> removeListeners = new ArrayList<LoadBalancerListener>();
        for( LbListener l : listeners ) {
            removeListeners.add(new LoadBalancerListener(l.getPrivatePort(), l.getPublicPort(), toBBProtocol(l.getNetworkProtocol()), 0));
        }
        CreateLoadBalancer lb = new CreateLoadBalancer();
        lb.setListeners(removeListeners);
        getProvider().getCloudApiService().removeListenersFromLoadBalancer(fromLoadBalancerId, lb);
    }

    @Override
    public void removeServers(@Nonnull String fromLoadBalancerId, @Nonnull String... serverIdsToRemove) throws CloudException, InternalException {
        List<LoadBalancerNode> nodes = new ArrayList<LoadBalancerNode>();
        for(String id : serverIdsToRemove ) {
            nodes.add(new LoadBalancerNode(id));
        }
        CreateLoadBalancer lb = new CreateLoadBalancer();
        lb.setNodes(nodes);
        getProvider().getCloudApiService().removeNodesFromLoadBalancer(fromLoadBalancerId, lb);
    }

    @Override
    public @Nullable SSLCertificate getSSLCertificate(@Nonnull String certificateName) throws CloudException, InternalException {
        throw new CloudException(getProvider().getCloudName() + " doesn't support SSL certificates management.");
    }

    @Override
    public SSLCertificate createSSLCertificate(@Nonnull SSLCertificateCreateOptions options) throws CloudException, InternalException {
        throw new CloudException(getProvider().getCloudName() + " doesn't support SSL certificates management.");
    }

    @Override
    public @Nonnull Iterable<SSLCertificate> listSSLCertificates() throws CloudException, InternalException {
        throw new CloudException(getProvider().getCloudName() + " doesn't support SSL certificates management.");
    }

    @Override
    public void removeSSLCertificate(@Nonnull String certificateName) throws CloudException, InternalException {
        throw new CloudException(getProvider().getCloudName() + " doesn't support SSL certificates management.");
    }

    @Override
    public void setSSLCertificate(@Nonnull SetLoadBalancerSSLCertificateOptions options) throws CloudException, InternalException {
        throw new CloudException(getProvider().getCloudName() + " doesn't support SSL certificates management.");
    }

    @Override
    public LoadBalancerHealthCheck createLoadBalancerHealthCheck(@Nonnull HealthCheckOptions options) throws CloudException, InternalException {
        throw new OperationNotSupportedException(getProvider().getCloudName() + " requires load balancers to always have health checks, thus it is impossible to create a health check separately.");
    }

    @Override
    public LoadBalancerHealthCheck createLoadBalancerHealthCheck(@Nullable String name, @Nullable String description, @Nullable String host, @Nullable LoadBalancerHealthCheck.HCProtocol protocol, int port, @Nullable String path, int interval, int timeout, int healthyCount, int unhealthyCount) throws CloudException, InternalException {
        throw new OperationNotSupportedException(getProvider().getCloudName() + " requires load balancers to always have health checks, thus it is impossible to create a health check separately.");
    }

    @Override
    public LoadBalancerHealthCheck getLoadBalancerHealthCheck(@Nonnull String providerLBHealthCheckId, @Nullable String providerLoadBalancerId) throws CloudException, InternalException {
        return super.getLoadBalancerHealthCheck(providerLBHealthCheckId, providerLoadBalancerId);
    }

    @Override
    public Iterable<LoadBalancerHealthCheck> listLBHealthChecks(@Nullable HealthCheckFilterOptions opts) throws CloudException, InternalException {
        return super.listLBHealthChecks(opts);
    }

    @Override
    public void removeLoadBalancerHealthCheck(@Nonnull String providerLoadBalancerId) throws CloudException, InternalException {
        throw new OperationNotSupportedException(getProvider().getCloudName() + " requires load balancers to always have health checks, thus it is impossible to remove a health check.");
    }
}

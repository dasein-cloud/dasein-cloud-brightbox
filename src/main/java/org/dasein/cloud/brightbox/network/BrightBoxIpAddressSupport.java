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
import org.dasein.cloud.ResourceStatus;
import org.dasein.cloud.brightbox.BrightBoxCloud;
import org.dasein.cloud.brightbox.api.model.CloudIp;
import org.dasein.cloud.brightbox.api.model.CloudIpDestination;
import org.dasein.cloud.brightbox.api.model.CreateCloudIp;
import org.dasein.cloud.brightbox.api.model.Interface;
import org.dasein.cloud.brightbox.api.model.PortTranslator;
import org.dasein.cloud.brightbox.api.model.Server;
import org.dasein.cloud.network.AbstractIpAddressSupport;
import org.dasein.cloud.network.AddressType;
import org.dasein.cloud.network.IPAddressCapabilities;
import org.dasein.cloud.network.IPVersion;
import org.dasein.cloud.network.IpAddress;
import org.dasein.cloud.network.IpForwardingRule;
import org.dasein.cloud.network.Protocol;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by stas on 19/02/2015.
 */
public class BrightBoxIpAddressSupport extends AbstractIpAddressSupport<BrightBoxCloud> {

    public BrightBoxIpAddressSupport(@Nonnull BrightBoxCloud provider) {
        super(provider);
    }

    @Override
    public void assign(@Nonnull String addressId, @Nonnull String serverId) throws InternalException, CloudException {
        Server server = getProvider().getCloudApiService().getServer(serverId);
        List<Interface> interfaces = server.getInterfaces();
        if( interfaces == null && interfaces.size() == 0 ) {
            throw new CloudException("Server ["+serverId+"] has no network interfaces");
        }
        if( interfaces != null && interfaces.size() == 1 ) {
            getProvider().getCloudApiService().mapCloudIp(addressId, new CloudIpDestination(interfaces.get(0).getId()));
        }
        else {
            throw new CloudException("Server ["+serverId+"] has more than one network interface, it's impossible to automatically choose the one to assign the IP address to");
        }
    }

    @Override
    public void assignToNetworkInterface(@Nonnull String addressId, @Nonnull String nicId) throws InternalException, CloudException {
        getProvider().getCloudApiService().mapCloudIp(addressId, new CloudIpDestination(nicId));
    }

    @Override
    public @Nonnull String forward(@Nonnull String addressId, int publicPort, @Nonnull Protocol protocol, int privatePort, @Nonnull String onServerId) throws InternalException, CloudException {
        CloudIp cloudIp = getProvider().getCloudApiService().getCloudIp(addressId);
        if( "unmapped".equals(cloudIp.getStatus()) || (cloudIp.getServer() != null && onServerId.equals(cloudIp.getServer().getId())) ) {
            PortTranslator pt = new PortTranslator(publicPort, privatePort, Protocol.UDP.equals(protocol) ? "udp" : "tcp");
            cloudIp.getPortTranslators().add(pt);
            // map to the server if not yet mapped
            if( "unmapped".equals(cloudIp.getStatus()) ) {
                assign(addressId, onServerId);
            }
            getProvider().getCloudApiService().updateCloudIp(addressId, new CreateCloudIp(cloudIp.getReverseDns(), cloudIp.getName(), cloudIp.getPortTranslators()));
            return toForwardingRuleId(addressId, pt);
        }
        else {
            throw new CloudException("Address "+addressId+" is already assigned to a different resource");
        }
    }

    private volatile transient BrightBoxIpAddressCapabilities capabilities;

    @Override
    public @Nonnull IPAddressCapabilities getCapabilities() throws CloudException, InternalException {
        if( capabilities == null ) {
            capabilities = new BrightBoxIpAddressCapabilities(getProvider());
        }
        return capabilities;
    }

    @Override
    public @Nullable IpAddress getIpAddress(@Nonnull String addressId) throws InternalException, CloudException {
        try {
            return toIpAddress(getProvider().getCloudApiService().getCloudIp(addressId));
        }
        catch (CloudException e) {
            if( e.getHttpCode() == 404 ) {
                return null;
            }
            throw e;
        }
    }

    @Override
    public boolean isSubscribed() throws CloudException, InternalException {
        try {
            getProvider().getCloudApiService().listCloudIps();
            return true;
        } catch( CloudException e ) {
            if( e.getHttpCode() == 403 && "Action forbidden".equalsIgnoreCase(e.getProviderCode()) ) {
                return false;
            }
            throw e;
        }
    }

    @Override
    public @Nonnull Iterable<IpAddress> listIpPool(@Nonnull IPVersion version, boolean unassignedOnly) throws InternalException, CloudException {
        List<IpAddress> ipAddresses = new ArrayList<IpAddress>();
        if( !IPVersion.IPV4.equals(version) ) {
            return ipAddresses;
        }
        for( CloudIp cloudIp : getProvider().getCloudApiService().listCloudIps() ) {
            if( !unassignedOnly || "unmapped".equals(cloudIp.getStatus())) {
                ipAddresses.add(toIpAddress(cloudIp));
            }
        }
        return ipAddresses;
    }

    private IpAddress toIpAddress(CloudIp cloudIp) throws CloudException, InternalException {
        IpAddress ipAddress = new IpAddress();
        ipAddress.setAddress(cloudIp.getPublicIp());
        ipAddress.setIpAddressId(cloudIp.getId());
        ipAddress.setVersion(IPVersion.IPV4);
        ipAddress.setAddressType(AddressType.PUBLIC);
        if( cloudIp.getInterface() != null ) {
            ipAddress.setProviderNetworkInterfaceId(cloudIp.getInterface().getId());
        }
        if( cloudIp.getServer() != null ) {
            ipAddress.setServerId(cloudIp.getServer().getId());
        }
        if( cloudIp.getLoadBalancer() != null ) {
            ipAddress.setProviderLoadBalancerId(cloudIp.getLoadBalancer().getId());
        }
        ipAddress.setRegionId(getContext().getRegionId());
        return ipAddress;
    }

    @Override
    public @Nonnull Future<Iterable<IpAddress>> listIpPoolConcurrently(@Nonnull IPVersion version, boolean unassignedOnly) throws InternalException, CloudException {
        return null;
    }

    @Override
    public @Nonnull Iterable<ResourceStatus> listIpPoolStatus(@Nonnull IPVersion version) throws InternalException, CloudException {
        List<ResourceStatus> statuses = new ArrayList<ResourceStatus>();
        if( !IPVersion.IPV4.equals(version) ) {
            return statuses;
        }
        for( CloudIp cloudIp : getProvider().getCloudApiService().listCloudIps() ) {
            statuses.add(new ResourceStatus(cloudIp.getId(), "unmapped".equals(cloudIp.getStatus())));
        }
        return statuses;
    }

    private String toForwardingRuleId(String addressId, PortTranslator pt) {
        return addressId+":"+pt.getIncoming()+":"+pt.getOutgoing()+":"+pt.getProtocol();
    }

    @Override
    public @Nonnull Iterable<IpForwardingRule> listRules(@Nonnull String addressId) throws InternalException, CloudException {
        List<IpForwardingRule> rules = new ArrayList<IpForwardingRule>();
        CloudIp cloudIp = getProvider().getCloudApiService().getCloudIp(addressId);
        for( PortTranslator translator : cloudIp.getPortTranslators() ) {
            IpForwardingRule rule = new IpForwardingRule();
            rule.setAddressId(addressId);
            rule.setPrivatePort(translator.getOutgoing());
            rule.setPublicPort(translator.getIncoming());
            rule.setProtocol("udp".equals(translator.getProtocol()) ? Protocol.UDP : Protocol.TCP);
            rule.setProviderRuleId(toForwardingRuleId(addressId, translator));
            if( cloudIp.getServer() != null ) {
                rule.setServerId(cloudIp.getServer().getId());
            }
            rules.add(rule);
        }
        return rules;
    }

    @Override
    public void releaseFromPool(@Nonnull String addressId) throws InternalException, CloudException {
        getProvider().getCloudApiService().deleteCloudIp(addressId);
    }

    @Override
    public void releaseFromServer(@Nonnull String addressId) throws InternalException, CloudException {
        getProvider().getCloudApiService().unmapCloudIp(addressId);
    }

    @Override
    public @Nonnull String request(@Nonnull IPVersion version) throws InternalException, CloudException {
        if( !IPVersion.IPV4.equals(version) ) {
            throw new org.dasein.cloud.OperationNotSupportedException(getProvider().getCloudName() + " only supports IPV4");
        }
        CloudIp cloudIp = getProvider().getCloudApiService().createCloudIp(new CreateCloudIp());
        return cloudIp.getId();
    }

    @Override
    public @Nonnull String requestForVLAN(@Nonnull IPVersion version) throws InternalException, CloudException {
        throw new org.dasein.cloud.OperationNotSupportedException(getProvider().getCloudName() + " does not support VLAN");
    }

    @Override
    public @Nonnull String requestForVLAN(@Nonnull IPVersion version, @Nonnull String vlanId) throws InternalException, CloudException {
        throw new org.dasein.cloud.OperationNotSupportedException(getProvider().getCloudName() + " does not support VLAN");
    }

    @Override
    public void stopForward(@Nonnull String ruleId) throws InternalException, CloudException {
        String[] parts = ruleId.split(":");
        if( parts.length != 4 ) {
            throw new InternalException("Forward rule id ["+ruleId+"] is incorrect");
        }
        String addressId = parts[0];
        int incoming = Integer.parseInt(parts[1]);
        int outgoing = Integer.parseInt(parts[2]);
        String protocol = parts[3];
        CloudIp ip = getProvider().getCloudApiService().getCloudIp(addressId);
        if( ip.getPortTranslators() == null ) {
            throw new CloudException("IP address ["+addressId+"] has no forwarding rules");
        }
        List<PortTranslator> translators = new ArrayList<PortTranslator>();
        for( PortTranslator pt : ip.getPortTranslators() ) {
            if( !protocol.equals(pt.getProtocol()) && incoming != pt.getIncoming() && outgoing != pt.getOutgoing() ) {
                translators.add(pt);
            }
        }
        if( ip.getPortTranslators().size() > translators.size() ) {
            getProvider().getCloudApiService().updateCloudIp(addressId, new CreateCloudIp(ip.getReverseDns(), ip.getName(), translators));
        }
        else {
            throw new CloudException("IP address ["+addressId+"] did not have forwarding rule ["+ruleId+"]");
        }

    }
}

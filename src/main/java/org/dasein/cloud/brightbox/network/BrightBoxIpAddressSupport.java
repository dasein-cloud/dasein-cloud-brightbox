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
import org.dasein.cloud.network.AbstractIpAddressSupport;
import org.dasein.cloud.network.IPAddressCapabilities;
import org.dasein.cloud.network.IPVersion;
import org.dasein.cloud.network.IpAddress;
import org.dasein.cloud.network.IpForwardingRule;
import org.dasein.cloud.network.Protocol;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

    }

    @Override
    public void assignToNetworkInterface(@Nonnull String addressId, @Nonnull String nicId) throws InternalException, CloudException {

    }

    @Override
    public @Nonnull String forward(@Nonnull String addressId, int publicPort, @Nonnull Protocol protocol, int privatePort, @Nonnull String onServerId) throws InternalException, CloudException {
        return null;
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
        return null;
    }

    @Override
    public boolean isSubscribed() throws CloudException, InternalException {
        return false;
    }

    @Override
    public @Nonnull Iterable<IpAddress> listIpPool(@Nonnull IPVersion version, boolean unassignedOnly) throws InternalException, CloudException {
        return null;
    }

    @Override
    public @Nonnull Future<Iterable<IpAddress>> listIpPoolConcurrently(@Nonnull IPVersion version, boolean unassignedOnly) throws InternalException, CloudException {
        return null;
    }

    @Override
    public @Nonnull Iterable<ResourceStatus> listIpPoolStatus(@Nonnull IPVersion version) throws InternalException, CloudException {
        return null;
    }

    @Override
    public @Nonnull Iterable<IpForwardingRule> listRules(@Nonnull String addressId) throws InternalException, CloudException {
        return null;
    }

    @Override
    public void releaseFromPool(@Nonnull String addressId) throws InternalException, CloudException {

    }

    @Override
    public void releaseFromServer(@Nonnull String addressId) throws InternalException, CloudException {

    }

    @Override
    public @Nonnull String request(@Nonnull IPVersion version) throws InternalException, CloudException {
        return null;
    }

    @Override
    public @Nonnull String requestForVLAN(@Nonnull IPVersion version) throws InternalException, CloudException {
        return null;
    }

    @Override
    public @Nonnull String requestForVLAN(@Nonnull IPVersion version, @Nonnull String vlanId) throws InternalException, CloudException {
        return null;
    }

    @Override
    public void stopForward(@Nonnull String ruleId) throws InternalException, CloudException {

    }
}

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

import org.dasein.cloud.AbstractCapabilities;
import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.Requirement;
import org.dasein.cloud.VisibleScope;
import org.dasein.cloud.brightbox.BrightBoxCloud;
import org.dasein.cloud.network.IPVersion;
import org.dasein.cloud.network.LbAlgorithm;
import org.dasein.cloud.network.LbEndpointType;
import org.dasein.cloud.network.LbPersistence;
import org.dasein.cloud.network.LbProtocol;
import org.dasein.cloud.network.LoadBalancerAddressType;
import org.dasein.cloud.network.LoadBalancerCapabilities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Created by stas on 13/02/2015.
 */
public class BrightBoxLoadBalancerCapabilities extends AbstractCapabilities<BrightBoxCloud> implements LoadBalancerCapabilities {

    public BrightBoxLoadBalancerCapabilities(@Nonnull BrightBoxCloud provider) {
        super(provider);
    }

    @Override
    public @Nonnull LoadBalancerAddressType getAddressType() throws CloudException, InternalException {
        return LoadBalancerAddressType.IP;
    }

    @Override
    public int getMaxPublicPorts() throws CloudException, InternalException {
        return 0;
    }

    @Override
    public @Nonnull String getProviderTermForLoadBalancer(@Nonnull Locale locale) {
        return "load balancer";
    }

    @Override
    public @Nullable VisibleScope getLoadBalancerVisibleScope() {
        return null;
    }

    @Override
    public boolean healthCheckRequiresLoadBalancer() throws CloudException, InternalException {
        return true;
    }

    @Override
    public Requirement healthCheckRequiresName() throws CloudException, InternalException {
        return Requirement.NONE;
    }

    @Override
    public @Nonnull Requirement identifyEndpointsOnCreateRequirement() throws CloudException, InternalException {
        return Requirement.REQUIRED;
    }

    @Override
    public @Nonnull Requirement identifyListenersOnCreateRequirement() throws CloudException, InternalException {
        return Requirement.REQUIRED;
    }

    @Override
    public @Nonnull Requirement identifyVlanOnCreateRequirement() throws CloudException, InternalException {
        return Requirement.NONE;
    }

    @Override
    public @Nonnull Requirement identifyHealthCheckOnCreateRequirement() throws CloudException, InternalException {
        return Requirement.REQUIRED;
    }

    @Override
    public boolean isAddressAssignedByProvider() throws CloudException, InternalException {
        return false;
    }

    @Override
    public boolean isDataCenterLimited() throws CloudException, InternalException {
        return false;
    }

    private volatile transient List<LbAlgorithm> algorithmList;

    @Override
    public @Nonnull Iterable<LbAlgorithm> listSupportedAlgorithms() throws CloudException, InternalException {
        if( algorithmList == null ) {
            algorithmList = Collections.unmodifiableList(Arrays.asList(LbAlgorithm.LEAST_CONN, LbAlgorithm.ROUND_ROBIN));
        }
        return algorithmList;
    }

    private volatile transient List<LbEndpointType> endpointTypes;

    @Override
    public @Nonnull Iterable<LbEndpointType> listSupportedEndpointTypes() throws CloudException, InternalException {
        if( endpointTypes == null ) {
            endpointTypes = Collections.unmodifiableList(Arrays.asList(LbEndpointType.VM));
        }
        return endpointTypes;
    }

    private volatile transient List<IPVersion> ipVersions;

    @Override
    public @Nonnull Iterable<IPVersion> listSupportedIPVersions() throws CloudException, InternalException {
        if( ipVersions == null ) {
            ipVersions = Collections.unmodifiableList(Arrays.asList(IPVersion.IPV4, IPVersion.IPV6));
        }
        return ipVersions;
    }

    private volatile transient List<LbPersistence> persistences;

    @Override
    public @Nonnull Iterable<LbPersistence> listSupportedPersistenceOptions() throws CloudException, InternalException {
        if( persistences == null ) {
            persistences = Collections.unmodifiableList(Arrays.asList(LbPersistence.COOKIE));
        }
        return persistences;
    }

    private volatile transient List<LbProtocol> protocols;

    @Override
    public @Nonnull Iterable<LbProtocol> listSupportedProtocols() throws CloudException, InternalException {
        if( protocols == null ) {
            protocols = Collections.unmodifiableList(
                    Arrays.asList(LbProtocol.RAW_TCP, LbProtocol.HTTP, LbProtocol.WS)
            );
        }
        return protocols;
    }

    @Override
    public boolean supportsAddingEndpoints() throws CloudException, InternalException {
        return true;
    }

    @Override
    public boolean supportsMonitoring() throws CloudException, InternalException {
        return false;
    }

    @Override
    public boolean supportsMultipleTrafficTypes() throws CloudException, InternalException {
        return false;
    }
}

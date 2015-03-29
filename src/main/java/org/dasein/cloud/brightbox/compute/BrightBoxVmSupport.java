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

package org.dasein.cloud.brightbox.compute;

import org.apache.commons.codec.binary.Base64;
import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.brightbox.BrightBoxCloud;
import org.dasein.cloud.brightbox.api.model.CreateServer;
import org.dasein.cloud.brightbox.api.model.Server;
import org.dasein.cloud.brightbox.api.model.ServerGroup;
import org.dasein.cloud.brightbox.api.model.ServerGroupServer;
import org.dasein.cloud.brightbox.api.model.ServerType;
import org.dasein.cloud.compute.AbstractVMSupport;
import org.dasein.cloud.compute.Architecture;
import org.dasein.cloud.compute.Platform;
import org.dasein.cloud.compute.VMLaunchOptions;
import org.dasein.cloud.compute.VirtualMachine;
import org.dasein.cloud.compute.VirtualMachineCapabilities;
import org.dasein.cloud.compute.VirtualMachineProduct;
import org.dasein.cloud.compute.VirtualMachineProductFilterOptions;
import org.dasein.cloud.compute.VmState;
import org.dasein.util.uom.storage.Megabyte;
import org.dasein.util.uom.storage.Storage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by stas on 10/02/2015.
 */
public class BrightBoxVmSupport extends AbstractVMSupport<BrightBoxCloud> {
    public BrightBoxVmSupport(BrightBoxCloud provider) {
        super(provider);
    }

    private transient volatile BrightBoxVmCapabilities capabilities;
    @Override
    public @Nonnull VirtualMachineCapabilities getCapabilities() throws InternalException, CloudException {
        if( capabilities == null ) {
            capabilities = new BrightBoxVmCapabilities(getProvider());
        }
        return capabilities;
    }

    @Override
    public @Nonnull Iterable<VirtualMachineProduct> listProducts(@Nullable VirtualMachineProductFilterOptions options, @Nullable Architecture architecture) throws InternalException, CloudException {
        List<ServerType> serverTypes = getProvider().getCloudApiService().listServerTypes();
        List<VirtualMachineProduct> products = new ArrayList<VirtualMachineProduct>(serverTypes.size());
        for( ServerType type : serverTypes ) {
            VirtualMachineProduct product = toVmProduct(type);
            if( options != null ) {
                if( options.matches(product) ) {
                    products.add(product);
                }
            }
            else {
                products.add(product);
            }
        }
        return products;
    }

    @Override
    public @Nonnull Iterable<VirtualMachine> listVirtualMachines() throws InternalException, CloudException {
        final List<VirtualMachine> virtualMachines = new ArrayList<VirtualMachine>();
        final List<ServerGroup> serverGroups = getProvider().getCloudApiService().listServerGroups();
        for( Server server : getProvider().getCloudApiService().listServers() ) {
            virtualMachines.add(toVirtualMachine(server, serverGroups));
        }
        return virtualMachines;
    }

    private VirtualMachineProduct toVmProduct(ServerType type) {
        VirtualMachineProduct product = new VirtualMachineProduct();
        product.setName(type.getName());
        product.setDescription(type.getName());
        product.setProviderProductId(type.getId());
        product.setArchitectures(Architecture.I64, Architecture.I32);
        product.setCpuCount(type.getCores());
        product.setRamSize(new Storage<Megabyte>(type.getRam(), Storage.MEGABYTE));
        product.setRootVolumeSize(new Storage<Megabyte>(type.getDiskSize(), Storage.MEGABYTE));
        if( "deprecated".equalsIgnoreCase(type.getStatus()) ) {
            product.setStatusDeprecated();
        }
        return product;
    }

    @Override
    public boolean isSubscribed() throws CloudException, InternalException {
        return true;
    }

    @Override
    public @Nonnull VirtualMachine launch(@Nonnull VMLaunchOptions opts) throws CloudException, InternalException {
        final List<ServerGroup> serverGroups = getProvider().getCloudApiService().listServerGroups();
        final Map<String, String> targetGroupIds = new HashMap<String, String>();
        if( opts.getFirewallIds().length > 0 ) {
            for( String firewallId : opts.getFirewallIds() ) {
                boolean existingGroup = false;
                for( ServerGroup group : serverGroups ) {
                    if( group.getFirewallPolicy() == null ) {
                        continue;
                    }
                    if( firewallId.equals(group.getFirewallPolicy().getId()) ) {
                        existingGroup = true;
                        targetGroupIds.put(group.getId(), group.getId());
                        break;
                    }
                }
                if( !existingGroup ) {
                    final ServerGroup newGroup = getProvider().getCloudApiService().createServerGroup(firewallId, "Group for "+firewallId);
                    targetGroupIds.put(newGroup.getId(), newGroup.getId());
                    getProvider().getCloudApiService().applyFirewallPolicyToServerGroup(firewallId, newGroup.getId());
                }
            }
        }
        String userData = null;
        if( opts.getUserData() != null ) {
            try {
                userData = Base64.encodeBase64String(opts.getUserData().getBytes("utf-8"));
            }
            catch( UnsupportedEncodingException ignore ) { }
        }

        Server server = getProvider().getCloudApiService().createServer(
                new CreateServer(opts.getMachineImageId()).withName(opts.getFriendlyName()).withServerTypeId(opts.getStandardProductId()).withZone(opts.getDataCenterId()).withUserData(userData).withServerGroupIds(new ArrayList<String>(targetGroupIds.values()))
        );
        return toVirtualMachine(server, serverGroups);
    }

    @Override
    public @Nullable VirtualMachine getVirtualMachine(@Nonnull String vmId) throws InternalException, CloudException {
        try {
            return toVirtualMachine(getProvider().getCloudApiService().getServer(vmId), getProvider().getCloudApiService().listServerGroups());
        } catch( CloudException e ) {
            if( e.getHttpCode() == 404 ) {
                return null;
            }
            throw e;
        }
    }

    @Override
    public @Nullable String getUserData(@Nonnull String vmId) throws InternalException, CloudException {
        Server server = getProvider().getCloudApiService().getServer(vmId);
        if( server != null && server.getUserData() != null ) {
            try {
                return new String(Base64.decodeBase64(server.getUserData().getBytes("utf-8")), "utf-8");
            }
            catch( UnsupportedEncodingException e ) {
            }
        }
        return null;
    }

    @Override
    public void start(@Nonnull String vmId) throws InternalException, CloudException {
        getProvider().getCloudApiService().startServer(vmId);
    }

    @Override
    public void stop(@Nonnull String vmId, boolean force) throws InternalException, CloudException {
        getProvider().getCloudApiService().stopServer(vmId);
    }

    private VirtualMachine toVirtualMachine(@Nullable Server server, @Nonnull List<ServerGroup> globalServerGroups) throws CloudException, InternalException {
        if( server == null ) {
            return null;
        }
        VirtualMachine vm = new VirtualMachine();
        vm.setProviderVirtualMachineId(server.getId());
        vm.setName(server.getName());
        vm.setDescription(server.getName());
        vm.setCreationTimestamp(server.getCreatedAt().getTime());
        vm.setProviderOwnerId(server.getAccount().getId());
        if( server.getZone() != null ) { // shouldn't be null according to the docs, but i've seen it
            vm.setProviderDataCenterId(server.getZone().getId());
        }
        vm.setProviderRegionId(getContext().getRegionId());
        vm.setProviderMachineImageId(server.getImage().getId());
        vm.setProductId(server.getServerType().getId());
        if( "i686".equalsIgnoreCase(server.getImage().getArch()) ) {
            vm.setArchitecture(Architecture.I32);
        }
        else {
            vm.setArchitecture(Architecture.I64);

        }
        List<ServerGroup> vmServerGroups = server.getServerGroups();
        List<String> firewalls = new ArrayList<String>();
        for( ServerGroup serverGroup : globalServerGroups ) {
            if( vmServerGroups.contains(serverGroup) ) {
                firewalls.add(serverGroup.getFirewallPolicy().getId());
            }
        }
        vm.setProviderFirewallIds(firewalls.toArray(new String[firewalls.size()]));
        vm.setPlatform(Platform.guess(vm.getName()));
        if( vm.getPlatform() == Platform.UNKNOWN ) {
            vm.setPlatform(Platform.guess(server.getImage().getName()));
        }
        if( vm.getPlatform() == Platform.UNKNOWN ) {
            vm.setPlatform(Platform.guess(server.getImage().getDescription()));
        }
        if( vm.getPlatform() == Platform.UNKNOWN ) {
            vm.setPlatform(Platform.guess(server.getImage().getSource()));
        }
        vm.setCurrentState(toVmState(server.getStatus()));
        if( server.getCloudIps() != null && server.getCloudIps().size() > 0 ) {
            // FIXME: this only selects the first ip address, which is crap
            vm.setProviderAssignedIpAddressId(server.getCloudIps().get(0).getId());
        }
        return vm;
    }

    private VmState toVmState(String state) {
        VmState result = null;
        if( "active".equalsIgnoreCase(state) ) {
            result = VmState.RUNNING;
        }
        else if( "creating".equalsIgnoreCase(state) ) {
            result = VmState.PENDING;
        }
        else if( "inactive".equalsIgnoreCase(state) ) {
            result = VmState.STOPPED;
        }
        else if( "deleting".equalsIgnoreCase(state) ) {
            result = VmState.PENDING;
        }
        else if( "deleted".equalsIgnoreCase(state) ) {
            result = VmState.TERMINATED;
        }
        else if( "failed".equalsIgnoreCase(state) ) {
            result = VmState.ERROR;
        }
        else if( "unavailable".equalsIgnoreCase(state) ) {
            result = VmState.ERROR;
        }
        return result;
    }

    @Override
    public void terminate(@Nonnull String vmId, @Nullable String explanation) throws InternalException, CloudException {
        Server server = getProvider().getCloudApiService().getServer(vmId);
        for( ServerGroup group : server.getServerGroups() ) {
            try {
                getProvider().getCloudApiService().removeServersFromGroup(group.getId(), Collections.singletonList(new ServerGroupServer(vmId)));
            } catch (CloudException e) {
                if( e.getHttpCode() != 404 ) {
                    throw e;
                }
            }
        }
        getProvider().getCloudApiService().deleteServer(vmId);
    }
}

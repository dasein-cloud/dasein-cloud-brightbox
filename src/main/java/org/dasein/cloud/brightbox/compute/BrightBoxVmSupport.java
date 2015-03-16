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
import org.bouncycastle.util.encoders.Base64Encoder;
import org.dasein.cloud.Cloud;
import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.brightbox.BrightBoxCloud;
import org.dasein.cloud.brightbox.api.model.Server;
import org.dasein.cloud.brightbox.api.model.ServerType;
import org.dasein.cloud.compute.AbstractVMSupport;
import org.dasein.cloud.compute.Architecture;
import org.dasein.cloud.compute.Platform;
import org.dasein.cloud.compute.VMFilterOptions;
import org.dasein.cloud.compute.VMLaunchOptions;
import org.dasein.cloud.compute.VirtualMachine;
import org.dasein.cloud.compute.VirtualMachineCapabilities;
import org.dasein.cloud.compute.VirtualMachineProduct;
import org.dasein.cloud.compute.VirtualMachineProductFilterOptions;
import org.dasein.cloud.compute.VirtualMachineSupport;
import org.dasein.cloud.compute.VmState;
import org.dasein.util.uom.storage.Gigabyte;
import org.dasein.util.uom.storage.Megabyte;
import org.dasein.util.uom.storage.Storage;
import org.dasein.util.uom.storage.StorageUnit;
import retrofit.client.Response;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
        List<VirtualMachine> virtualMachines = new ArrayList<VirtualMachine>();
        for( Server server : getProvider().getCloudApiService().listServers() ) {
            virtualMachines.add(toVirtualMachine(server));
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
    public @Nonnull VirtualMachine launch(@Nonnull VMLaunchOptions withLaunchOptions) throws CloudException, InternalException {
        String userData = null;
        if( withLaunchOptions.getUserData() != null ) {
            try {
                userData = Base64.encodeBase64String(withLaunchOptions.getUserData().getBytes("utf-8"));
            }
            catch( UnsupportedEncodingException ignore ) { }
        }
        Server server = getProvider().getCloudApiService().createServer(withLaunchOptions.getMachineImageId(), withLaunchOptions.getFriendlyName(), withLaunchOptions.getStandardProductId(), withLaunchOptions.getDataCenterId(), userData, null);
        return toVirtualMachine(server);
    }

    @Override
    public @Nullable VirtualMachine getVirtualMachine(@Nonnull String vmId) throws InternalException, CloudException {
        try {
            return toVirtualMachine(getProvider().getCloudApiService().getServer(vmId));
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

    private VirtualMachine toVirtualMachine(Server server) throws CloudException {
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
        vm.setPlatform(Platform.guess(vm.getName()));
        if( vm.getPlatform() == null ) {
            vm.setPlatform(Platform.guess(server.getImage().getName()));
        }
        if( vm.getPlatform() == null ) {
            vm.setPlatform(Platform.guess(server.getImage().getDescription()));
        }
        if( vm.getPlatform() == null ) {
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
        getProvider().getCloudApiService().deleteServer(vmId);
    }
}

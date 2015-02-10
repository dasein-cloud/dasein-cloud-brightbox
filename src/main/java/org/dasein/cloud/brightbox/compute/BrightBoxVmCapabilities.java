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

import org.dasein.cloud.AbstractCapabilities;
import org.dasein.cloud.Capabilities;
import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.Requirement;
import org.dasein.cloud.VisibleScope;
import org.dasein.cloud.brightbox.BrightBoxCloud;
import org.dasein.cloud.compute.Architecture;
import org.dasein.cloud.compute.ImageClass;
import org.dasein.cloud.compute.Platform;
import org.dasein.cloud.compute.VMScalingCapabilities;
import org.dasein.cloud.compute.VirtualMachineCapabilities;
import org.dasein.cloud.compute.VmState;
import org.dasein.cloud.util.NamingConstraints;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Created by stas on 10/02/2015.
 */
public class BrightBoxVmCapabilities extends AbstractCapabilities<BrightBoxCloud> implements VirtualMachineCapabilities {

    public BrightBoxVmCapabilities(@Nonnull BrightBoxCloud provider) {
        super(provider);
    }

    @Override
    public boolean canAlter(@Nonnull VmState fromState) throws CloudException, InternalException {
        return false;
    }

    @Override
    public boolean canClone(@Nonnull VmState fromState) throws CloudException, InternalException {
        return false;
    }

    @Override
    public boolean canPause(@Nonnull VmState fromState) throws CloudException, InternalException {
        return false;
    }

    @Override
    public boolean canReboot(@Nonnull VmState fromState) throws CloudException, InternalException {
        return VmState.RUNNING.equals(fromState);
    }

    @Override
    public boolean canResume(@Nonnull VmState fromState) throws CloudException, InternalException {
        return false;
    }

    @Override
    public boolean canStart(@Nonnull VmState fromState) throws CloudException, InternalException {
        return VmState.STOPPED.equals(fromState);
    }

    @Override
    public boolean canStop(@Nonnull VmState fromState) throws CloudException, InternalException {
        return VmState.RUNNING.equals(fromState);
    }

    @Override
    public boolean canSuspend(@Nonnull VmState fromState) throws CloudException, InternalException {
        return false;
    }

    @Override
    public boolean canTerminate(@Nonnull VmState fromState) throws CloudException, InternalException {
        return VmState.RUNNING.equals(fromState) || VmState.STOPPED.equals(fromState);
    }

    @Override
    public boolean canUnpause(@Nonnull VmState fromState) throws CloudException, InternalException {
        return false;
    }

    @Override
    public int getMaximumVirtualMachineCount() throws CloudException, InternalException {
        return Capabilities.LIMIT_UNKNOWN;
    }

    @Override
    public int getCostFactor(@Nonnull VmState state) throws CloudException, InternalException {
        return VmState.TERMINATED.equals(state) ? 0 : 100;
    }

    @Override
    public @Nonnull String getProviderTermForVirtualMachine(@Nonnull Locale locale) throws CloudException, InternalException {
        return "server";
    }

    @Override
    public @Nullable VMScalingCapabilities getVerticalScalingCapabilities() throws CloudException, InternalException {
        return null;
    }

    @Override
    public @Nonnull NamingConstraints getVirtualMachineNamingConstraints() throws CloudException, InternalException {
        return NamingConstraints.getHostNameInstance(false);
    }

    @Override
    public @Nullable VisibleScope getVirtualMachineVisibleScope() {
        return null;
    }

    @Override
    public @Nullable VisibleScope getVirtualMachineProductVisibleScope() {
        return null;
    }

    @Override
    public @Nonnull Requirement identifyDataCenterLaunchRequirement() throws CloudException, InternalException {
        return Requirement.OPTIONAL;
    }

    @Override
    public @Nonnull Requirement identifyImageRequirement(@Nonnull ImageClass cls) throws CloudException, InternalException {
        return Requirement.REQUIRED;
    }

    @Override
    public @Nonnull Requirement identifyPasswordRequirement(Platform platform) throws CloudException, InternalException {
        return Requirement.NONE;
    }

    @Override
    public @Nonnull Requirement identifyRootVolumeRequirement() throws CloudException, InternalException {
        return Requirement.NONE;
    }

    @Override
    public @Nonnull Requirement identifyShellKeyRequirement(Platform platform) throws CloudException, InternalException {
        return Requirement.NONE;
    }

    @Override
    public @Nonnull Requirement identifyStaticIPRequirement() throws CloudException, InternalException {
        return Requirement.NONE;
    }

    @Override
    public @Nonnull Requirement identifySubnetRequirement() throws CloudException, InternalException {
        return Requirement.NONE;
    }

    @Override
    public @Nonnull Requirement identifyVlanRequirement() throws CloudException, InternalException {
        return Requirement.NONE;
    }

    @Override
    public boolean isAPITerminationPreventable() throws CloudException, InternalException {
        return false;
    }

    @Override
    public boolean isBasicAnalyticsSupported() throws CloudException, InternalException {
        return false;
    }

    @Override
    public boolean isExtendedAnalyticsSupported() throws CloudException, InternalException {
        return false;
    }

    @Override
    public boolean isUserDataSupported() throws CloudException, InternalException {
        return true;
    }

    @Override
    public boolean isUserDefinedPrivateIPSupported() throws CloudException, InternalException {
        return false;
    }

    private volatile transient List<Architecture> architectures = Collections.unmodifiableList(Arrays.asList(Architecture.I32, Architecture.I64));

    @Override
    public @Nonnull Iterable<Architecture> listSupportedArchitectures() throws InternalException, CloudException {
        return architectures;
    }

    @Override
    public boolean supportsSpotVirtualMachines() throws InternalException, CloudException {
        return false;
    }

    @Override
    public boolean supportsAlterVM() {
        return false;
    }

    @Override
    public boolean supportsClone() {
        return false;
    }

    @Override
    public boolean supportsPause() {
        return false;
    }

    @Override
    public boolean supportsReboot() {
        return true;
    }

    @Override
    public boolean supportsResume() {
        return false;
    }

    @Override
    public boolean supportsStart() {
        return true;
    }

    @Override
    public boolean supportsStop() {
        return true;
    }

    @Override
    public boolean supportsSuspend() {
        return false;
    }

    @Override
    public boolean supportsTerminate() {
        return true;
    }

    @Override
    public boolean supportsUnPause() {
        return false;
    }
}

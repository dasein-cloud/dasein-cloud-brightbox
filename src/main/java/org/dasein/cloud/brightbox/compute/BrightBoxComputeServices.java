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

import org.dasein.cloud.brightbox.BrightBoxCloud;
import org.dasein.cloud.compute.AbstractComputeServices;
import org.dasein.cloud.compute.MachineImageSupport;
import org.dasein.cloud.compute.VirtualMachineSupport;

import javax.annotation.Nullable;

/**
 * Created by stas on 10/02/2015.
 */
public class BrightBoxComputeServices extends AbstractComputeServices<BrightBoxCloud> {

    public BrightBoxComputeServices(BrightBoxCloud provider) {
        super(provider);
    }

    @Override
    public @Nullable MachineImageSupport getImageSupport() {
        return new BrightBoxMachineImageSupport(getProvider());
    }

    @Override
    public @Nullable VirtualMachineSupport getVirtualMachineSupport() {
        return new BrightBoxVmSupport(getProvider());
    }
}

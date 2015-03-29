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

import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.brightbox.BrightBoxCloud;
import org.dasein.cloud.brightbox.api.model.Image;
import org.dasein.cloud.compute.AbstractImageSupport;
import org.dasein.cloud.compute.Architecture;
import org.dasein.cloud.compute.ImageCapabilities;
import org.dasein.cloud.compute.ImageClass;
import org.dasein.cloud.compute.ImageCopyOptions;
import org.dasein.cloud.compute.ImageFilterOptions;
import org.dasein.cloud.compute.MachineImage;
import org.dasein.cloud.compute.MachineImageState;
import org.dasein.cloud.compute.MachineImageType;
import org.dasein.cloud.compute.Platform;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by stas on 10/02/2015.
 */
public class BrightBoxMachineImageSupport extends AbstractImageSupport<BrightBoxCloud> {

    public BrightBoxMachineImageSupport(@Nonnull BrightBoxCloud provider) {
        super(provider);
    }

    private transient volatile BrightBoxImageCapabilities capabilities;

    @Override
    public ImageCapabilities getCapabilities() throws CloudException, InternalException {
        if( capabilities == null ) {
            capabilities = new BrightBoxImageCapabilities(getProvider());
        }
        return capabilities;
    }

    @Override
    public @Nullable MachineImage getImage(@Nonnull String providerImageId) throws CloudException, InternalException {
        try {
            Image image = getProvider().getCloudApiService().getImage(providerImageId);
            return toMachineImage(image);
        } catch (CloudException e) {
            if( e.getHttpCode() == 404 ) {
                return null;
            }
            throw e;
        }
    }

    @Override
    public boolean isSubscribed() throws CloudException, InternalException {
        getImage("img-dummy");
        return true;
    }

    @Override
    public @Nonnull Iterable<MachineImage> listImages(@Nullable ImageFilterOptions options) throws CloudException, InternalException {
        return listImages(options, false);
    }

    @Override
    public @Nonnull Iterable<MachineImage> searchPublicImages(@Nonnull ImageFilterOptions options) throws CloudException, InternalException {
        return listImages(options, true);
    }

    private @Nonnull Iterable<MachineImage> listImages(@Nullable ImageFilterOptions options, boolean onlyPublic) throws CloudException, InternalException {
        List<Image> images = getProvider().getCloudApiService().listImages();
        List<MachineImage> results = new ArrayList<MachineImage>(images.size());
        for( Image image : images ) {
            MachineImage mi = toMachineImage(image);
            if( mi == null ) {
                continue;
            }
            if( options != null ) {
                if( options.matches(mi) && ((onlyPublic && image.isPublic()) || !onlyPublic) ) {
                    results.add(mi);
                }
            }
            else if ((onlyPublic && image.isPublic()) || !onlyPublic){
                results.add(mi);
            }
        }
        return results;
    }

    @Override
    public boolean isImageSharedWithPublic(@Nonnull String providerImageId) throws CloudException, InternalException {
        MachineImage image = getImage(providerImageId);
        if( image != null ) {
            return image.isPublic();
        }
        return false;
    }

    /**
     * Convert BB architecture to core arch
     * @param arch
     * @return
     */
    private Architecture toArchitecture(String arch) {
        Architecture result = null;
        try {
            return Architecture.valueOf(arch);
        } catch( IllegalArgumentException ignore ) {}
        if( "i686".equalsIgnoreCase(arch) ) {
            return Architecture.I32;
        }
        return Architecture.I64;
    }

    /**
     * Convert BB image state to core state
     * @param state
     * @return
     */
    private MachineImageState toState(String state) {
        if( "available".equals(state) || "deprecated".equals(state) ) {
            return MachineImageState.ACTIVE;
        }
        else if( "creating".equals(state) || "deleting".equals(state) ) {
            return MachineImageState.PENDING;
        }
        else if( "deleted".equals(state) ) {
            return MachineImageState.DELETED;
        }
        else {
            return null; // unavailable or failed
        }
    }

    /**
     * Convert BB model to core model
     * @param image
     * @return
     * @throws CloudException
     */
    private MachineImage toMachineImage(Image image) throws CloudException {
        MachineImageState state = toState(image.getStatus());
        if( state == null ) {
            return null; // state is unsupported
        }
        MachineImage result = MachineImage.getInstance(image.getOwner(), getContext().getRegionId(), image.getId(), ImageClass.MACHINE, state, image.getName(), image.getDescription(), toArchitecture(image.getArch()), Platform.guess(image.getName() + " " + image.getSource() + " " + image.getLicenceName()));
        result.setMinimumDiskSizeGb(image.getDiskSize() * 1024);
        if( image.isPublic() ) {
            result.sharedWithPublic();
        }
        result.createdAt(image.getCreatedAt().getTime()).withType(MachineImageType.VOLUME);
        return result;
    }

    @Override
    public void remove(@Nonnull String providerImageId, boolean checkState) throws CloudException, InternalException {
        getProvider().getCloudApiService().deleteImage(providerImageId);
    }
}

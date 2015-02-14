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

package org.dasein.cloud.brightbox.api.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by stas on 14/02/2015.
 */
public class CreateLoadBalancer {
    private @Nullable String                     name;
    private @Nonnull  List<LoadBalancerNode>     nodes;
    private @Nullable String                     policy;
    private @Nullable String                     certificatePem;
    private @Nullable String                     certificateKey;
    private           boolean                    sslv3;
    private @Nonnull  List<LoadBalancerListener> listeners;
    private @Nonnull  LoadBalancerHealthcheck    healthcheck;
    private @Nullable Integer                    bufferSize;

    @Nullable public String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    @Nonnull public List<LoadBalancerNode> getNodes() {
        return nodes;
    }

    public void setNodes(@Nonnull List<LoadBalancerNode> nodes) {
        this.nodes = nodes;
    }

    @Nullable public String getPolicy() {
        return policy;
    }

    public void setPolicy(@Nullable String policy) {
        this.policy = policy;
    }

    @Nullable public String getCertificatePem() {
        return certificatePem;
    }

    public void setCertificatePem(@Nullable String certificatePem) {
        this.certificatePem = certificatePem;
    }

    @Nullable public String getCertificateKey() {
        return certificateKey;
    }

    public void setCertificateKey(@Nullable String certificateKey) {
        this.certificateKey = certificateKey;
    }

    public boolean isSslv3() {
        return sslv3;
    }

    public void setSslv3(boolean sslv3) {
        this.sslv3 = sslv3;
    }

    @Nonnull public List<LoadBalancerListener> getListeners() {
        return listeners;
    }

    public void setListeners(@Nonnull List<LoadBalancerListener> listeners) {
        this.listeners = listeners;
    }

    @Nonnull public LoadBalancerHealthcheck getHealthcheck() {
        return healthcheck;
    }

    public void setHealthcheck(@Nonnull LoadBalancerHealthcheck healthcheck) {
        this.healthcheck = healthcheck;
    }

    @Nullable public Integer getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(@Nullable Integer bufferSize) {
        this.bufferSize = bufferSize;
    }
}

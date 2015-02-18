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
    private @Nullable List<LoadBalancerNode>     nodes;
    private @Nullable String                     policy;
    private @Nullable String                     certificatePem;
    private @Nullable String                     certificateKey;
    private @Nullable Boolean                    sslv3;
    private @Nullable List<LoadBalancerListener> listeners;
    private @Nullable LoadBalancerHealthcheck    healthcheck;
    private @Nullable Integer                    bufferSize;

    public @Nullable String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    public @Nullable List<LoadBalancerNode> getNodes() {
        return nodes;
    }

    public void setNodes(@Nullable List<LoadBalancerNode> nodes) {
        this.nodes = nodes;
    }

    public @Nullable String getPolicy() {
        return policy;
    }

    public void setPolicy(@Nullable String policy) {
        this.policy = policy;
    }

    public @Nullable String getCertificatePem() {
        return certificatePem;
    }

    public void setCertificatePem(@Nullable String certificatePem) {
        this.certificatePem = certificatePem;
    }

    public @Nullable String getCertificateKey() {
        return certificateKey;
    }

    public void setCertificateKey(@Nullable String certificateKey) {
        this.certificateKey = certificateKey;
    }

    public @Nullable Boolean isSslv3() {
        return sslv3;
    }

    public void setSslv3(@Nullable Boolean sslv3) {
        this.sslv3 = sslv3;
    }

    public @Nullable List<LoadBalancerListener> getListeners() {
        return listeners;
    }

    public void setListeners(@Nullable List<LoadBalancerListener> listeners) {
        this.listeners = listeners;
    }

    public @Nullable LoadBalancerHealthcheck getHealthcheck() {
        return healthcheck;
    }

    public void setHealthcheck(@Nullable LoadBalancerHealthcheck healthcheck) {
        this.healthcheck = healthcheck;
    }

    public @Nullable Integer getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(@Nullable Integer bufferSize) {
        this.bufferSize = bufferSize;
    }
}

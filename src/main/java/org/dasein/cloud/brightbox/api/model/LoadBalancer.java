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

import java.util.List;

/**
 * Created by stas on 13/02/2015.
 */
public class LoadBalancer {
    private String                     id;
    private String                     url;
    private String                     name;
    private String                     status;
    private List<LoadBalancerListener> listeners;
    private String                     policy;
    private LoadBalancerHealthcheck    healthcheck;
    private int                        bufferSize;
    private String                     createdAt;
    private String                     deletedAt;
    private boolean                    locked;
    private String                     certificate;
    private Account                    account;
    private List<CloudIp>              cloudIps;
    private List<Server>               nodes;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<LoadBalancerListener> getListeners() {
        return listeners;
    }

    public void setListeners(List<LoadBalancerListener> listeners) {
        this.listeners = listeners;
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    public LoadBalancerHealthcheck getHealthcheck() {
        return healthcheck;
    }

    public void setHealthcheck(LoadBalancerHealthcheck healthcheck) {
        this.healthcheck = healthcheck;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(String deletedAt) {
        this.deletedAt = deletedAt;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public List<CloudIp> getCloudIps() {
        return cloudIps;
    }

    public void setCloudIps(List<CloudIp> cloudIps) {
        this.cloudIps = cloudIps;
    }

    public List<Server> getNodes() {
        return nodes;
    }

    public void setNodes(List<Server> nodes) {
        this.nodes = nodes;
    }
}

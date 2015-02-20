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

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by stas on 10/02/2015.
 */
public class CloudIp {
    private String               id;
    private String               url;
    private String               publicIp;
    private String               status;
    private String               reverseDns;
    private String               name;
    private List<PortTranslator> portTranslators;
    private Account account;
    @SerializedName("interface")
    private Interface _interface;
    private Server server;
    private LoadBalancer loadBalancer;
    private DatabaseServer databaseServer;

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

    public String getPublicIp() {
        return publicIp;
    }

    public void setPublicIp(String publicIp) {
        this.publicIp = publicIp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReverseDns() {
        return reverseDns;
    }

    public void setReverseDns(String reverseDns) {
        this.reverseDns = reverseDns;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PortTranslator> getPortTranslators() {
        return portTranslators;
    }

    public void setPortTranslators(List<PortTranslator> portTranslators) {
        this.portTranslators = portTranslators;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Interface getInterface() {
        return _interface;
    }

    public void setInterface(Interface _interface) {
        this._interface = _interface;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public LoadBalancer getLoadBalancer() {
        return loadBalancer;
    }

    public void setLoadBalancer(LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    public DatabaseServer getDatabaseServer() {
        return databaseServer;
    }

    public void setDatabaseServer(DatabaseServer databaseServer) {
        this.databaseServer = databaseServer;
    }
}


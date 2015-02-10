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

import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;

/**
 * Created by stas on 10/02/2015.
 */
public class Server {
    private           String            id;
    private           String            url;
    private           String            name;
    private           String            status;
    private           String            hostname;
    private           String            fqdn;
    private           Date              createdAt;
    private @Nullable Date            deletedAt;
    private @Nullable Date            startedAt;
    private @Nullable String            userData;
    private           boolean           compatibilityMode;
    private @Nullable String            consoleUrl;
    private @Nullable String            consoleToken;
    private @Nullable String            consoleTokenExpires;
    private           boolean           locked;
    private           Account           account;
    private           Image             image;
    private           ServerType        serverType;
    private           Zone              zone;
    private           List<Image>       snapshots;
    private           List<CloudIp>     cloudIps;
    private           List<Interface>   interfaces;
    private           List<ServerGroup> serverGroups;

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

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getFqdn() {
        return fqdn;
    }

    public void setFqdn(String fqdn) {
        this.fqdn = fqdn;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Nullable public Date getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(@Nullable Date deletedAt) {
        this.deletedAt = deletedAt;
    }

    @Nullable public Date getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(@Nullable Date startedAt) {
        this.startedAt = startedAt;
    }

    @Nullable public String getUserData() {
        return userData;
    }

    public void setUserData(@Nullable String userData) {
        this.userData = userData;
    }

    public boolean isCompatibilityMode() {
        return compatibilityMode;
    }

    public void setCompatibilityMode(boolean compatibilityMode) {
        this.compatibilityMode = compatibilityMode;
    }

    @Nullable public String getConsoleUrl() {
        return consoleUrl;
    }

    public void setConsoleUrl(@Nullable String consoleUrl) {
        this.consoleUrl = consoleUrl;
    }

    @Nullable public String getConsoleToken() {
        return consoleToken;
    }

    public void setConsoleToken(@Nullable String consoleToken) {
        this.consoleToken = consoleToken;
    }

    @Nullable public String getConsoleTokenExpires() {
        return consoleTokenExpires;
    }

    public void setConsoleTokenExpires(@Nullable String consoleTokenExpires) {
        this.consoleTokenExpires = consoleTokenExpires;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public ServerType getServerType() {
        return serverType;
    }

    public void setServerType(ServerType serverType) {
        this.serverType = serverType;
    }

    public Zone getZone() {
        return zone;
    }

    public void setZone(Zone zone) {
        this.zone = zone;
    }

    public List<Image> getSnapshots() {
        return snapshots;
    }

    public void setSnapshots(List<Image> snapshots) {
        this.snapshots = snapshots;
    }

    public List<CloudIp> getCloudIps() {
        return cloudIps;
    }

    public void setCloudIps(List<CloudIp> cloudIps) {
        this.cloudIps = cloudIps;
    }

    public List<Interface> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(List<Interface> interfaces) {
        this.interfaces = interfaces;
    }

    public List<ServerGroup> getServerGroups() {
        return serverGroups;
    }

    public void setServerGroups(List<ServerGroup> serverGroups) {
        this.serverGroups = serverGroups;
    }
}

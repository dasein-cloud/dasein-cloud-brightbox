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

import java.util.Date;
import java.util.List;

/**
 * Created by stas on 19/02/2015.
 */
public class DatabaseServer {
    private String       id;
    private String       url;
    private String       name;
    private String       description;
    private String       status;
    private String       databaseEngine;
    private String       databaseVersion;
    private String       adminUsername;
    private String       adminPassword;
    private Date         createdAt;
    private Date         updatedAt;
    private Date         deletedAt;
    private List<String> allowAccess;
    private boolean      locked;
    private DatabaseServerType databaseServerType;
    private List<CloudIp> cloudIps;
    private Zone zone;

    public DatabaseServer() {
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDatabaseEngine() {
        return databaseEngine;
    }

    public void setDatabaseEngine(String databaseEngine) {
        this.databaseEngine = databaseEngine;
    }

    public String getDatabaseVersion() {
        return databaseVersion;
    }

    public void setDatabaseVersion(String databaseVersion) {
        this.databaseVersion = databaseVersion;
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Date deletedAt) {
        this.deletedAt = deletedAt;
    }

    public List<String> getAllowAccess() {
        return allowAccess;
    }

    public void setAllowAccess(List<String> allowAccess) {
        this.allowAccess = allowAccess;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public DatabaseServerType getDatabaseServerType() {
        return databaseServerType;
    }

    public void setDatabaseServerType(DatabaseServerType databaseServerType) {
        this.databaseServerType = databaseServerType;
    }

    public List<CloudIp> getCloudIps() {
        return cloudIps;
    }

    public void setCloudIps(List<CloudIp> cloudIps) {
        this.cloudIps = cloudIps;
    }

    public Zone getZone() {
        return zone;
    }

    public void setZone(Zone zone) {
        this.zone = zone;
    }
}

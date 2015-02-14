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

/**
 * Created by stas on 13/02/2015.
 */
public class LoadBalancerHealthcheck {
    private String type;
    private String request;
    private int port;
    private int interval;
    private int timeout;
    private int thresholdUp;
    private int thresholdDown;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getThresholdUp() {
        return thresholdUp;
    }

    public void setThresholdUp(int thresholdUp) {
        this.thresholdUp = thresholdUp;
    }

    public int getThresholdDown() {
        return thresholdDown;
    }

    public void setThresholdDown(int thresholdDown) {
        this.thresholdDown = thresholdDown;
    }
}

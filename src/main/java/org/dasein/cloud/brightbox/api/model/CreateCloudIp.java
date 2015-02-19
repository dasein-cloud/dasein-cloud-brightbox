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
 * Created by stas on 19/02/2015.
 */
public class CreateCloudIp {
    private String               reverseDns;
    private String               name;
    private List<PortTranslator> portTranslators;

    public CreateCloudIp(String reverseDns, String name, List<PortTranslator> portTranslators) {
        this.reverseDns = reverseDns;
        this.name = name;
        this.portTranslators = portTranslators;
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
}

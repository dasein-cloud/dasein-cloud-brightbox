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

package org.dasein.cloud.brightbox.network;

import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.brightbox.BrightBoxCloud;
import org.dasein.cloud.brightbox.api.model.FirewallPolicy;
import org.dasein.cloud.network.AbstractFirewallSupport;
import org.dasein.cloud.network.Direction;
import org.dasein.cloud.network.Firewall;
import org.dasein.cloud.network.FirewallCapabilities;
import org.dasein.cloud.network.FirewallRule;
import org.dasein.cloud.network.Permission;
import org.dasein.cloud.network.Protocol;
import org.dasein.cloud.network.RuleTarget;

import javax.annotation.Nonnull;
import javax.print.attribute.standard.Destination;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by stas on 20/02/2015.
 */
public class BrightBoxFirewallSupport extends AbstractFirewallSupport<BrightBoxCloud> {
    public BrightBoxFirewallSupport(BrightBoxCloud provider) {
        super(provider);
    }

    @Override
    public void delete(@Nonnull String firewallId) throws InternalException, CloudException {
        getProvider().getCloudApiService().deleteFirewallPolicy(firewallId);
    }

    private volatile transient BrightBoxFirewallCapabilities capabilities;

    @Override
    public @Nonnull FirewallCapabilities getCapabilities() throws CloudException, InternalException {
        if( capabilities == null ) {
            capabilities = new BrightBoxFirewallCapabilities(getProvider());
        }
        return capabilities;
    }

    @Override
    public boolean isSubscribed() throws CloudException, InternalException {
        try {
            getProvider().getCloudApiService().listFirewallPolicies();
            return true;
        } catch( CloudException e ) {
            if( e.getHttpCode() == 403 && "Action forbidden".equalsIgnoreCase(e.getProviderCode()) ) {
                return false;
            }
            throw e;
        }
    }

    @Override
    public @Nonnull Iterable<Firewall> list() throws InternalException, CloudException {
        List<Firewall> firewalls = new ArrayList<Firewall>();
        List<FirewallPolicy> policies = getProvider().getCloudApiService().listFirewallPolicies();
        for( FirewallPolicy policy : policies ) {
            Firewall firewall = new Firewall();
            firewall.setProviderFirewallId(policy.getId());
            firewall.setName(policy.getName());
            firewall.setDescription(policy.getDescription());
            firewall.setRegionId(getContext().getRegionId());
            firewall.setActive(true);
            firewall.setAvailable(true);
            List<FirewallRule> rules = new ArrayList<FirewallRule>();
            for( org.dasein.cloud.brightbox.api.model.FirewallRule r : policy.getRules() ) {
                RuleTarget src = null;
                if( r.getSource() != null ) {
                    if( "any".equals(r.getSource()) ) {
                        src = RuleTarget.getGlobal(policy.getId());
                    }
                    else if( r.getSource().startsWith("srv-") ) {
                        src = RuleTarget.getVirtualMachine(r.getSource());
                    }
                    else {
                        src = RuleTarget.getCIDR(r.getSource());
                    }
                }
                RuleTarget dest = null;
                if( r.getDestination() != null ) {
                    if( r.getDestination().startsWith("srv-") ) {
                        dest = RuleTarget.getVirtualMachine(r.getDestination());
                    }
                    else {
                        dest = RuleTarget.getCIDR(r.getDestination());
                    }
                }
                Direction direction = null;
                if( src == null && dest != null ) {
                    direction = Direction.INGRESS;
                }
                else if( dest == null && src != null ) {
                    direction = Direction.EGRESS;
                }
                Protocol protocol = Protocol.TCP;
                if( "udp".equalsIgnoreCase(r.getProtocol()) ) {
                    protocol = Protocol.UDP;
                }
                else if( "icmp".equalsIgnoreCase(r.getProtocol())) {
                    protocol = Protocol.ICMP;
                }
                String portsValue = r.getDestinationPort() != null ? r.getDestinationPort() : r.getSourcePort();
                if( portsValue == null ) {
                    continue; // no ports!!! nothing to do
                }
                String [] ports = portsValue.split(",");
                for( String port : ports ) {
                    String [] range = port.split("-");
                    int startPort = Integer.parseInt(range[0]);
                    int endPort = startPort;
                    if( range.length > 1) {
                        endPort = Integer.parseInt(range[1]);
                    }
                    FirewallRule rule = FirewallRule.getInstance(r.getId(), policy.getId(), src, direction, protocol, Permission.ALLOW, dest, startPort, endPort);
                    rules.add(rule);
                }
            }
            firewall.setRules(rules);
        }
        return firewalls;
    }
}

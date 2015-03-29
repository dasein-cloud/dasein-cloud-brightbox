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
import org.dasein.cloud.OperationNotSupportedException;
import org.dasein.cloud.brightbox.BrightBoxCloud;
import org.dasein.cloud.brightbox.api.model.CreateFirewallRule;
import org.dasein.cloud.brightbox.api.model.FirewallPolicy;
import org.dasein.cloud.brightbox.api.model.ServerGroup;
import org.dasein.cloud.network.AbstractFirewallSupport;
import org.dasein.cloud.network.Direction;
import org.dasein.cloud.network.Firewall;
import org.dasein.cloud.network.FirewallCapabilities;
import org.dasein.cloud.network.FirewallCreateOptions;
import org.dasein.cloud.network.FirewallRule;
import org.dasein.cloud.network.FirewallRuleCreateOptions;
import org.dasein.cloud.network.Permission;
import org.dasein.cloud.network.Protocol;
import org.dasein.cloud.network.RuleTarget;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.print.attribute.standard.Destination;
import java.util.ArrayList;
import java.util.Collections;
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
        // delete associated cloud server group
        List<ServerGroup> serverGroups = getProvider().getCloudApiService().listServerGroups();
        for( ServerGroup serverGroup : serverGroups ) {
            if( !serverGroup.isDefault() && serverGroup.getFirewallPolicy() != null && serverGroup.getFirewallPolicy().getId().equals(firewallId) ) {
                getProvider().getCloudApiService().deleteServerGroup(serverGroup.getId());
                break;
            }
        }
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
    public @Nullable Firewall getFirewall(@Nonnull String firewallId) throws InternalException, CloudException {
        try {
            return toFirewall(getProvider().getCloudApiService().getFirewallPolicy(firewallId));
        } catch (CloudException e) {
            if( e.getHttpCode() == 404 ) {
                return null;
            }
            throw e;
        }
    }

    @Override
    public @Nonnull String create(@Nonnull FirewallCreateOptions options) throws InternalException, CloudException {
        if( options.getProviderVlanId() != null ) {
            throw new OperationNotSupportedException("VLAN firewalls are not supported by "+getProvider().getCloudName());
        }
        FirewallPolicy policy = new FirewallPolicy();
        policy.setName(options.getName());
        policy.setDescription(options.getDescription());
        policy = getProvider().getCloudApiService().createFirewallPolicy(policy);
        String firewallId = policy.getId();
        if( options.getInitialRules() != null ) {
            for( FirewallRuleCreateOptions ro : options.getInitialRules() ) {
                authorize(firewallId, ro);
            }
        }
        return firewallId;
    }

    @Override
    public @Nonnull String authorize(@Nonnull String firewallId, @Nonnull Direction direction, @Nonnull Permission permission, @Nonnull RuleTarget sourceEndpoint, @Nonnull Protocol protocol, @Nonnull RuleTarget destinationEndpoint, int beginPort, int endPort, @Nonnegative int precedence) throws CloudException, InternalException {
        if( Permission.DENY.equals(permission) ) {
            throw new OperationNotSupportedException("DENY rules are not supported by " + getProvider().getCloudName());
        }
        CreateFirewallRule rule = new CreateFirewallRule();
        rule.setFirewallPolicyId(firewallId);
        switch( protocol ) {
            case ICMP:
                rule.setProtocol("icmp");
                rule.setIcmpTypeName("echo-request");
                break;
            case TCP:
                rule.setProtocol("tcp");
                break;
            case UDP:
                rule.setProtocol("udp");
                break;
            default:
                throw new OperationNotSupportedException("Rules with protocol " + protocol + " are not supported by " + getProvider().getCloudName());
        }
        String port;
        if( beginPort != endPort ) {
            port = beginPort + "-" + endPort;
        }
        else {
            port = String.valueOf(beginPort);
        }
        if( Direction.INGRESS.equals(direction) ) {
            rule.setDestinationPort(port);
        }
        else if( Direction.EGRESS.equals(direction) ) {
            rule.setSourcePort(port);
        }
        if( destinationEndpoint != null ) {
            switch( destinationEndpoint.getRuleTargetType() ) {
                case CIDR:
                    rule.setDestination(destinationEndpoint.getCidr());
                    break;
                case VM:
                    rule.setDestination(destinationEndpoint.getProviderVirtualMachineId());
                    break;
                default: // global
                    break;
            }
        }
        if( sourceEndpoint != null ) {
            switch( sourceEndpoint.getRuleTargetType() ) {
                case CIDR:
                    rule.setSource(sourceEndpoint.getCidr());
                    break;
                case VM:
                    rule.setSource(sourceEndpoint.getProviderVirtualMachineId());
                    break;
                default: // global
                    break;
            }
        }
        org.dasein.cloud.brightbox.api.model.FirewallRule result = getProvider().getCloudApiService().createFirewallRule(rule);
        return result.getId();
    }

    @Override
    public void revoke(@Nonnull String providerFirewallRuleId) throws InternalException, CloudException {
        getProvider().getCloudApiService().deleteFirewallRule(providerFirewallRuleId);
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

    @Override
    public @Nonnull Iterable<FirewallRule> getRules(@Nonnull String firewallId) throws InternalException, CloudException {
        return toRules(getProvider().getCloudApiService().getFirewallPolicy(firewallId));
    }

    private List<FirewallRule> toRules(@Nullable FirewallPolicy policy) {
        if( policy == null ) {
            return Collections.emptyList();
        }
        List<FirewallRule> result = new ArrayList<FirewallRule>(policy.getRules().size());
        for( org.dasein.cloud.brightbox.api.model.FirewallRule rule : policy.getRules() ) {
            String portsValue = rule.getDestinationPort();
            if( portsValue == null ) {
                portsValue = rule.getSourcePort();
            }
            String[] ports = portsValue.split(",");
            for( String port : ports ) {
                String[] range = port.split("-");
                int startPort = Integer.parseInt(range[0]);
                int endPort = startPort;
                if( range.length > 1 ) {
                    endPort = Integer.parseInt(range[1]);
                }
                result.add(toRule(policy.getId(), rule, startPort, endPort));
            }
        }
        return result;
    }

    private FirewallRule toRule(String firewallId, org.dasein.cloud.brightbox.api.model.FirewallRule rule, int startPort, int endPort) {
        Direction direction = Direction.INGRESS;
        if( rule.getSource() == null ) {
            direction = Direction.EGRESS;
        }
        Protocol protocol = Protocol.TCP;
        if( "udp".equals(rule.getProtocol()) ) {
            protocol = Protocol.UDP;
        }
        else if( "icmp".equals(rule.getProtocol()) ) {
            protocol = Protocol.ICMP;
        }
        RuleTarget source = RuleTarget.getGlobal(firewallId);
        if( rule.getSource() != null && rule.getSource().startsWith("srv-") ) {
            source = RuleTarget.getVirtualMachine(rule.getSource());
        }
        else if( rule.getSource() != null && rule.getSource().indexOf("/") > 0 ) {
            source = RuleTarget.getCIDR(rule.getSource());
        }
        RuleTarget destination = RuleTarget.getGlobal(firewallId);
        if( rule.getDestination() != null && rule.getDestination().startsWith("srv-") ) {
            destination = RuleTarget.getVirtualMachine(rule.getDestination());
        }
        else if( rule.getDestination() != null && rule.getDestination().indexOf("/") > 0 ) {
            destination = RuleTarget.getCIDR(rule.getDestination());
        }
        return FirewallRule.getInstance(rule.getId(), firewallId, source, direction, protocol, Permission.ALLOW, destination, startPort, endPort);
    }

    private @Nullable Firewall toFirewall(@Nullable FirewallPolicy firewallPolicy) throws CloudException {
        if( firewallPolicy == null ) {
            return null;
        }
        Firewall firewall = new Firewall();
        firewall.setName(firewallPolicy.getName());
        firewall.setDescription(firewallPolicy.getDescription());
        firewall.setProviderFirewallId(firewallPolicy.getId());
        firewall.setRegionId(getContext().getRegionId());
        firewall.setRules(toRules(firewallPolicy));
        return firewall;
    }

}

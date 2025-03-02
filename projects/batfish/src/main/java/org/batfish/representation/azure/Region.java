package org.batfish.representation.azure;

import com.fasterxml.jackson.databind.JsonNode;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Configuration;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Region implements Serializable {


    private final Map<String, NetworkInterface> _interfaces = new HashMap<>();
    private final Map<String, VNet> _vnets = new HashMap<>();
    private final Map<String, Subnet> _subnets = new HashMap<>();
    private final Map<String, Instance> _instances = new HashMap<>();
    private final Map<String, NetworkSecurityGroup> _networkSecurityGroups = new HashMap<>();
    private final String _regionName;

    public Region(String regionName) {
        this._regionName = regionName;
    }

    public Map<String, NetworkInterface> getInterfaces() {
        return _interfaces;
    }

    public Map<String, VNet> getVnets() {
        return _vnets;
    }
    public Map<String, Subnet> getSubnets() {
        return _subnets;
    }

    public Map<String, Instance> getInstances() {
        return _instances;
    }
    public Map<String, NetworkSecurityGroup> getNetworkSecurityGroups() {
        return _networkSecurityGroups;
    }

    public void addConfigElement(JsonNode node){
        if(!node.has(AzureEntities.JSON_KEY_TYPE) || !node.has(AzureEntities.JSON_KEY_PROPERTIES)){

            return; // throw an error because azure always specify the type -> not an azure config
        }

        if(!node.get(AzureEntities.JSON_KEY_TYPE).isTextual()) {
            return;
        }

        String type = node.get(AzureEntities.JSON_KEY_TYPE).textValue();

        switch(type){
            case AzureEntities.JSON_TYPE_VM:
                Instance vm = BatfishObjectMapper.mapper().convertValue(node, VirtualMachine.class);
                _instances.put(vm.getId(), vm);
                break;
            case AzureEntities.JSON_TYPE_VNET:
                VNet network = BatfishObjectMapper.mapper().convertValue(node, VNet.class);
                _vnets.put(network.getId(), network);
                for(Subnet subnet : network.getProperties().getSubnets()){
                    _subnets.put(subnet.getId(), subnet);
                }
                break;
            case AzureEntities.JSON_TYPE_INTERFACE:
                NetworkInterface networkInterface = BatfishObjectMapper.mapper().convertValue(node, NetworkInterface.class);
                _interfaces.put(networkInterface.getId(), networkInterface);
                break;
            case AzureEntities.JSON_TYPE_NETWORK_SECURITY_GROUP:
                NetworkSecurityGroup nsg = BatfishObjectMapper.mapper().convertValue(node, NetworkSecurityGroup.class);
                _networkSecurityGroups.put(nsg.getId(), nsg);
                break;
            default:
                return;
        }
    }

    public void toConfigurationNode(ConvertedConfiguration convertedConfiguration) {


        for (Instance instance : _instances.values()) {
            Configuration cfgNode = instance.toConfigurationNode(this, convertedConfiguration);
            convertedConfiguration.addNode(cfgNode);
        }

        for (Subnet subnet : _subnets.values()) {
            Configuration cfgNode = subnet.toConfigurationNode(this, convertedConfiguration);
            convertedConfiguration.addNode(cfgNode);
        }

        // VNet interacts with subnet nodes so subnets must be generated before
        for (VNet vnet : _vnets.values()) {
            Configuration cfgNode = vnet.toConfigurationNode(this, convertedConfiguration);
            convertedConfiguration.addNode(cfgNode);
        }
    }
}

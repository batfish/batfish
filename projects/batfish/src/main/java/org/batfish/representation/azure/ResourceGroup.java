package org.batfish.representation.azure;

import com.fasterxml.jackson.databind.JsonNode;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Configuration;

import java.util.HashMap;
import java.util.Map;

public class ResourceGroup {


    private final Map<String, NetworkInterface> _interfaces = new HashMap<>();
    private final Map<String, Subnet> _subnets = new HashMap<>();
    private final Map<String, Instance> _instances = new HashMap<>();
    private final String _regionName;

    public ResourceGroup(String regionName) {
        this._regionName = regionName;
    }

    public Map<String, NetworkInterface> getInterfaces() {
        return _interfaces;
    }

    public Map<String, Subnet> getSubnets() {
        return _subnets;
    }

    public Map<String, Instance> getInstances() {
        return _instances;
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
                for(Subnet subnet : network.getProperties().getSubnets()){
                    _subnets.put(subnet.getId(), subnet);
                }
                break;
            case AzureEntities.JSON_TYPE_INTERFACE:
                NetworkInterface networkInterface = BatfishObjectMapper.mapper().convertValue(node, NetworkInterface.class);
                _interfaces.put(networkInterface.getId(), networkInterface);
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
    }
}

package org.batfish.representation.azure;

import com.fasterxml.jackson.databind.JsonNode;
import org.batfish.common.util.BatfishObjectMapper;

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

    public Map<String, Instance> getInstances() {
        return _instances;
    }

    public void addConfigElement(JsonNode node){
        String type = String.valueOf(node.get(AzureEntities.JSON_KEY_TYPE));
        if(type == null){
            return; // throw an error because azure always specify the type -> not an azure config
        }

        JsonNode content = node.get(AzureEntities.JSON_KEY_PROPERTIES);

        switch(type){
            case AzureEntities.JSON_TYPE_VM:
                Instance vm = BatfishObjectMapper.mapper().convertValue(content, VirtualMachine.class);
                _instances.put(vm.getId(), vm);
                break;
            case AzureEntities.JSON_TYPE_VNET:
                VNet network = BatfishObjectMapper.mapper().convertValue(content, VNet.class);
                for(Subnet subnet : network.getProperties().getSubnets()){
                    _subnets.put(subnet.getId(), subnet);
                }
                break;
            default:
                return;
        }
    }

    public void toConfigurationNode(){
        for (NetworkInterface iface : _interfaces.values()) {
            iface.toConfigurationNode();
            // add to Converted config
        }

        for (Instance instance : _instances.values()) {
            instance.toConfigurationNode(this);
            // add to Converted config
        }
    }
}

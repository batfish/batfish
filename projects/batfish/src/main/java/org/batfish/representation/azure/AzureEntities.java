package org.batfish.representation.azure;

public interface AzureEntities {
    String JSON_API_VERSION = "apiVersion";
    String JSON_KEY_ID = "id";
    String JSON_KEY_NAME = "name";
    String JSON_KEY_TYPE = "type";
    String JSON_KEY_LOCATION = "location";
    String JSON_KEY_PROPERTIES = "properties";

    String JSON_KEY_NETWORK_INTERFACE_ID = "networkInterfaces";
    String JSON_KEY_NETWORK_PROFILE = "networkProfile";

    String JSON_KEY_VNET_SUBNETS = "subnets";
    String JSON_KEY_VNET_ADDRESS_SPACE = "addressSpace";
    String JSON_KEY_VNET_ADDRESS_PREFIX = "addressPrefixes";

    String JSON_KEY_VNET_IP_CONFIGURATIONS = "ipConfigurations";
    String JSON_KEY_INTERFACE_PRIVATE_IP_ADDRESS = "privateIPAddress";
    String JSON_KEY_INTERFACE_MAC_ADDRESS = "macAddress";
    String JSON_KEY_INTERFACE_SUBNET = "subnet";

    String JSON_TYPE_VM = "Microsoft.Compute/virtualMachines";
    String JSON_TYPE_VNET = "Microsoft.Network/virtualNetworks";
    String JSON_TYPE_INTERFACE = "Microsoft.Network/networkInterfaces";

}

package org.batfish.representation.azure;

public interface AzureEntities {
    String JSON_API_VERSION = "apiVersion";
    String JSON_KEY_ID = "id";
    String JSON_KEY_NAME = "name";
    String JSON_KEY_TYPE = "type";
    String JSON_KEY_LOCATION = "location";
    String JSON_KEY_PROPERTIES = "properties";

    String JSON_KEY_NETWORK_INTERFACE_ID = "networkInterfaces";

    String JSON_KEY_VNET_SUBNETS = "subnets";
    String JSON_KEY_VNET_ADDRESS_SPACE = "addressSpace";

    String JSON_TYPE_VM = "microsoft.compute/virtualmachines";
    String JSON_TYPE_VNET = "microsoft.net/virtualmachines";

}

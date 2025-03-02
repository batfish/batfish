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

    String JSON_KEY_SUBNET_ADDRESS_PREFIX = "addressPrefix";
    String JSON_KEY_SUBNET_NAT_GATEWAY = "natGateway";
    String JSON_KEY_SUBNET_IP_CONFIGURATIONS = "ipConfigurations";

    String JSON_KEY_INTERFACE_PRIVATE_IP_ADDRESS = "privateIPAddress";
    String JSON_KEY_INTERFACE_MAC_ADDRESS = "macAddress";
    String JSON_KEY_INTERFACE_SUBNET = "subnet";
    String JSON_KEY_INTERFACE_NGS = "networkSecurityGroup";
    String JSON_KEY_INTERFACE_PUBLIC_IP_ADDRESS = "publicIPAddress";

    String JSON_KEY_NSG_PROTOCOL = "protocol";
    String JSON_KEY_NSG_SRC_PORT = "sourcePortRange";
    String JSON_KEY_NSG_DST_PORT = "destinationPortRange";
    String JSON_KEY_NSG_SRC_PORTS = "sourcePortRanges";
    String JSON_KEY_NSG_DST_PORTS = "destinationPortRanges";
    String JSON_KEY_NSG_SRC_PREFIX = "sourceAddressPrefix";
    String JSON_KEY_NSG_DST_PREFIX = "destinationAddressPrefix";
    String JSON_KEY_NSG_SRC_PREFIXES = "sourceAddressPrefixes";
    String JSON_KEY_NSG_DST_PREFIXES = "destinationAddressPrefixes";
    String JSON_KEY_NSG_ACCESS = "access";
    String JSON_KEY_NSG_PRIORITY = "priority";
    String JSON_KEY_NSG_DIRECTION = "direction";

    String JSON_KEY_PUBLIC_IP_ADDRESS = "ipAddress";

    String JSON_KEY_NAT_GATEWAY_PUBLIC_IP_ADDRESSES = "publicIpAddresses";
    String JSON_KEY_NAT_GATEWAY_PUBLIC_IP_PREFIXES = "publicIpPrefixes";
    String JSON_KEY_NAT_GATEWAY_SUBNETS = "subnets";

    String JSON_KEY_POSTGRES_NETWORK = "network";
    String JSON_KEY_POSTGRES_NETWORK_DELEGATED_SUBNET_ID = "delegatedSubnetResourceId";


    String JSON_TYPE_VM = "Microsoft.Compute/virtualMachines";
    String JSON_TYPE_VNET = "Microsoft.Network/virtualNetworks";
    String JSON_TYPE_INTERFACE = "Microsoft.Network/networkInterfaces";
    String JSON_TYPE_NETWORK_SECURITY_GROUP = "Microsoft.Network/networkSecurityGroups";
    String JSON_TYPE_POSTGRES = "Microsoft.DBforPostgreSQL/flexibleServers";
    String JSON_TYPE_NAT_GATEWAY = "Microsoft.Network/natGateways";
    String JSON_TYPE_PUBLIC_IP = "Microsoft.Network/publicIPAddresses";
}

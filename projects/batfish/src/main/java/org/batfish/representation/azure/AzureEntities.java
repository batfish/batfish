package org.batfish.representation.azure;

public final class AzureEntities {
  public static final String JSON_API_VERSION = "apiVersion";
  public static final String JSON_KEY_ID = "id";
  public static final String JSON_KEY_NAME = "name";
  public static final String JSON_KEY_TYPE = "type";
  public static final String JSON_KEY_LOCATION = "location";
  public static final String JSON_KEY_PROPERTIES = "properties";

  public static final String JSON_KEY_NETWORK_INTERFACE_ID = "networkInterfaces";
  public static final String JSON_KEY_NETWORK_PROFILE = "networkProfile";

  public static final String JSON_KEY_VNET_SUBNETS = "subnets";
  public static final String JSON_KEY_VNET_ADDRESS_SPACE = "addressSpace";
  public static final String JSON_KEY_VNET_ADDRESS_PREFIX = "addressPrefixes";
  public static final String JSON_KEY_VNET_IP_CONFIGURATIONS = "ipConfigurations";

  public static final String JSON_KEY_SUBNET_ADDRESS_PREFIX = "addressPrefix";
  public static final String JSON_KEY_SUBNET_NAT_GATEWAY = "natGateway";
  public static final String JSON_KEY_SUBNET_IP_CONFIGURATIONS = "ipConfigurations";

  public static final String JSON_KEY_INTERFACE_PRIVATE_IP_ADDRESS = "privateIPAddress";
  public static final String JSON_KEY_INTERFACE_MAC_ADDRESS = "macAddress";
  public static final String JSON_KEY_INTERFACE_SUBNET = "subnet";
  public static final String JSON_KEY_INTERFACE_NGS = "networkSecurityGroup";
  public static final String JSON_KEY_INTERFACE_PUBLIC_IP_ADDRESS = "publicIPAddress";
  public static final String JSON_KEY_INTERFACE_PRIMARY = "primary";

  public static final String JSON_KEY_NSG_SECURITY_RULES = "securityRules";
  public static final String JSON_KEY_NSG_DEFAULT_SECURITY_RULES = "defaultSecurityRules";
  public static final String JSON_KEY_NSG_PROTOCOL = "protocol";
  public static final String JSON_KEY_NSG_SRC_PORT = "sourcePortRange";
  public static final String JSON_KEY_NSG_DST_PORT = "destinationPortRange";
  public static final String JSON_KEY_NSG_SRC_PORTS = "sourcePortRanges";
  public static final String JSON_KEY_NSG_DST_PORTS = "destinationPortRanges";
  public static final String JSON_KEY_NSG_SRC_PREFIX = "sourceAddressPrefix";
  public static final String JSON_KEY_NSG_DST_PREFIX = "destinationAddressPrefix";
  public static final String JSON_KEY_NSG_SRC_PREFIXES = "sourceAddressPrefixes";
  public static final String JSON_KEY_NSG_DST_PREFIXES = "destinationAddressPrefixes";
  public static final String JSON_KEY_NSG_ACCESS = "access";
  public static final String JSON_KEY_NSG_PRIORITY = "priority";
  public static final String JSON_KEY_NSG_DIRECTION = "direction";

  public static final String JSON_KEY_PUBLIC_IP_ADDRESS = "ipAddress";
  public static final String JSON_KEY_NAT_GATEWAY_PUBLIC_IP_ADDRESSES = "publicIpAddresses";
  public static final String JSON_KEY_NAT_GATEWAY_PUBLIC_IP_PREFIXES = "publicIpPrefixes";
  public static final String JSON_KEY_NAT_GATEWAY_SUBNETS = "subnets";

  public static final String JSON_KEY_POSTGRES_NETWORK = "network";
  public static final String JSON_KEY_POSTGRES_NETWORK_DELEGATED_SUBNET_ID =
      "delegatedSubnetResourceId";

  public static final String JSON_KEY_CONTAINER_GROUP_CONTAINERS = "containers";
  public static final String JSON_KEY_CONTAINER_INSTANCE_PORTS = "ports";
  public static final String JSON_KEY_CONTAINER_INSTANCE_PORT_NUMBER = "port";
  public static final String JSON_KEY_CONTAINER_INSTANCE_PORT_PROTOCOL = "protocol";
  public static final String JSON_KEY_CONTAINER_INSTANCE_IP_ADDRESS = "ipAddress";
  public static final String JSON_KEY_CONTAINER_INSTANCE_IP = "ip";
  public static final String JSON_KEY_CONTAINER_INSTANCE_SUBNET_IDS = "subnetIds";

  public static final String JSON_TYPE_VM = "Microsoft.Compute/virtualMachines";
  public static final String JSON_TYPE_VNET = "Microsoft.Network/virtualNetworks";
  public static final String JSON_TYPE_INTERFACE = "Microsoft.Network/networkInterfaces";
  public static final String JSON_TYPE_NETWORK_SECURITY_GROUP =
      "Microsoft.Network/networkSecurityGroups";
  public static final String JSON_TYPE_POSTGRES = "Microsoft.DBforPostgreSQL/flexibleServers";
  public static final String JSON_TYPE_NAT_GATEWAY = "Microsoft.Network/natGateways";
  public static final String JSON_TYPE_PUBLIC_IP = "Microsoft.Network/publicIPAddresses";
  public static final String JSON_TYPE_CONTAINER_GROUP =
      "Microsoft.ContainerInstance/containerGroups";

  private AzureEntities() {
    // Prevent instantiation
  }
}

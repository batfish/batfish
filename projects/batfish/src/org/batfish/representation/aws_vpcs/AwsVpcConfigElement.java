package org.batfish.representation.aws_vpcs;

public interface AwsVpcConfigElement {

	static final String JSON_KEY_ADDRESSES = "Addresses";
	static final String JSON_KEY_ASSOCIATIONS = "Associations";
	static final String JSON_KEY_CIDR_BLOCK = "CidrBlock";
	static final String JSON_KEY_CIDR_IP = "CidrIp";
	static final String JSON_KEY_DESTINATION_CIDR_BLOCK = "DestinationCidrBlock";
	static final String JSON_KEY_EGRESS = "Egress";
	static final String JSON_KEY_ENTRIES = "Entries";
	static final String JSON_KEY_FROM_PORT = "FromPort";
	static final String JSON_KEY_GATEWAY_ID = "GatewayId";
	static final String JSON_KEY_GROUP_NAME = "GroupName";
	static final String JSON_KEY_GROUP_ID = "GroupId";
	static final String JSON_KEY_INSTANCES = "Instances";
	static final String JSON_KEY_INSTANCE_ID = "InstanceId";
	static final String JSON_KEY_IP_PERMISSIONS = "IpPermissions";
	static final String JSON_KEY_IP_PERMISSIONS_EGRESS = "IpPermissionsEgress";
	static final String JSON_KEY_IP_PROTOCOL = "IpProtocol";
	static final String JSON_KEY_IP_RANGES = "IpRanges";
	static final String JSON_KEY_NETWORK_ACLS = "NetworkAcls";
	static final String JSON_KEY_NETWORK_ACL_ID = "NetworkAclId";
	static final String JSON_KEY_NETWORK_INTERFACES = "NetworkInterfaces";
	static final String JSON_KEY_NETWORK_INTERFACE_ID = "NetworkInterfaceId";
	static final String JSON_KEY_PUBLIC_IP = "PublicIp";
	static final String JSON_KEY_PRIVATE_IP_ADDRESS = "PrivateIpAddress";
	static final String JSON_KEY_MAIN = "Main";
	static final String JSON_KEY_PROTOCOL = "Protocol";
	static final String JSON_KEY_RESERVATIONS = "Reservations";
	static final String JSON_KEY_ROUTES = "Routes";
	static final String JSON_KEY_ROUTE_TABLES = "RouteTables";
	static final String JSON_KEY_ROUTE_TABLE_ID = "RouteTableId";
	static final String JSON_KEY_RULE_ACTION = "RuleAction";
	static final String JSON_KEY_RULE_NUMBER = "RuleNumber";
	static final String JSON_KEY_SECURITY_GROUPS = "SecurityGroups";
	static final String JSON_KEY_STATE = "State";
	static final String JSON_KEY_SUBNETS = "Subnets";
	static final String JSON_KEY_SUBNET_ID = "SubnetId";
	static final String JSON_KEY_TO_PORT = "ToPort";
	static final String JSON_KEY_VPCS = "Vpcs";
	static final String JSON_KEY_VPC_ID = "VpcId";
	static final String JSON_KEY_VPC_PEERING_CONNECTION_ID = "VpcPeeringConnectionId";

	public abstract String getId();
}

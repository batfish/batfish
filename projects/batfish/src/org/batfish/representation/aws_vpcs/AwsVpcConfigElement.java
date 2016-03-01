package org.batfish.representation.aws_vpcs;

public interface AwsVpcConfigElement {
   
   final static String JSON_KEY_ASSOCIATIONS = "Associations";
   final static String JSON_KEY_CIDR_BLOCK = "CidrBlock";
   static final String JSON_KEY_DESTINATION_CIDR_BLOCK = "DestinationCidrBlock";
   static final String JSON_KEY_GATEWAY_ID = "GatewayId";
   static final String JSON_KEY_INSTANCE_ID = "InstanceId";
   static final String JSON_KEY_MAIN = "Main";
   final static String JSON_KEY_ROUTES = "Routes";
   final static String JSON_KEY_ROUTE_TABLES = "RouteTables";
   final static String JSON_KEY_ROUTE_TABLE_ID = "RouteTableId";
   static final String JSON_KEY_STATE = "State";
   final static String JSON_KEY_SUBNETS = "Subnets";
   final static String JSON_KEY_SUBNET_ID = "SubnetId";
   final static String JSON_KEY_VPCS = "Vpcs";
   final static String JSON_KEY_VPC_ID = "VpcId";
   static final String JSON_KEY_VPC_PEERING_CONNECTION_ID = "VpcPeeringConnectionId";
   
	public abstract String getId();
}

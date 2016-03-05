package org.batfish.representation.aws_vpcs;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.batfish.common.BatfishLogger;
import org.batfish.main.Warnings;
import org.batfish.representation.Configuration;
import org.batfish.representation.GenericConfigObject;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class AwsVpcConfiguration implements Serializable, GenericConfigObject {

	private static final long serialVersionUID = 1L;

	private Map<String,Address> _addresses = new HashMap<String,Address>();

	private Map<String,CustomerGateway> _customerGateways = new HashMap<String,CustomerGateway>();

	private Map<String,Instance> _instances = new HashMap<String,Instance>();

	private Map<String,InternetGateway> _internetGateways = new HashMap<String,InternetGateway>();

	private Map<String,NetworkAcl> _networkAcls = new HashMap<String,NetworkAcl>();

	private Map<String,NetworkInterface> _networkInterfaces = new HashMap<String,NetworkInterface>();

	private Map<String,RouteTable> _routeTables = new HashMap<String,RouteTable>();

	private Map<String,SecurityGroup> _securityGroups = new HashMap<String,SecurityGroup>();

	private Map<String,Subnet> _subnets = new HashMap<String,Subnet>();

	private Map<String,Vpc> _vpcs = new HashMap<String,Vpc>();

	private Map<String,VpcPeeringConnection> _vpcPeerings = new HashMap<String,VpcPeeringConnection>();

	private Map<String,VpnConnection> _vpnConnections = new HashMap<String,VpnConnection>();

	private Map<String,VpnGateway> _vpnGateways = new HashMap<String,VpnGateway>();

	public void addConfigElement(JSONObject jsonObj, BatfishLogger logger) throws JSONException {

		Iterator<?> keys = jsonObj.keys();

		while( keys.hasNext() ) {
			String key = (String)keys.next();

			if (ignoreElement(key)) 
				continue;

			JSONArray jsonArray = jsonObj.getJSONArray(key);

			for (int index = 0; index < jsonArray.length(); index++) {
				JSONObject childObject = jsonArray.getJSONObject(index);
				addConfigElement(key, childObject, logger);
			}

		}
	}

	private void addConfigElement(String elementType, 
			JSONObject jsonObject, BatfishLogger logger) throws JSONException {
		switch (elementType) {
		case AwsVpcConfigElement.JSON_KEY_ADDRESSES:
			Address address = new Address(jsonObject, logger);
			_addresses.put(address.getId(), address);
			break;
		case AwsVpcConfigElement.JSON_KEY_INSTANCES:
			Instance instance = new Instance(jsonObject, logger);
			_instances.put(instance.getId(), instance);
			break;
		case AwsVpcConfigElement.JSON_KEY_CUSTOMER_GATEWAYS:
			CustomerGateway cGateway = new CustomerGateway(jsonObject, logger);
			_customerGateways.put(cGateway.getId(), cGateway);
			break;
		case AwsVpcConfigElement.JSON_KEY_INTERNET_GATEWAYS:
			InternetGateway iGateway = new InternetGateway(jsonObject, logger);
			_internetGateways.put(iGateway.getId(), iGateway);
			break;
		case AwsVpcConfigElement.JSON_KEY_NETWORK_ACLS:
			NetworkAcl networkAcl = new NetworkAcl(jsonObject, logger);
			_networkAcls.put(networkAcl.getId(), networkAcl);
			break;
		case AwsVpcConfigElement.JSON_KEY_NETWORK_INTERFACES:
			NetworkInterface networkInterface = new NetworkInterface(jsonObject, logger);
			_networkInterfaces.put(networkInterface.getId(), networkInterface);
			break;
		case AwsVpcConfigElement.JSON_KEY_RESERVATIONS:
			//instances are embedded inside reservations
			JSONArray jsonArray = jsonObject.getJSONArray(AwsVpcConfigElement.JSON_KEY_INSTANCES);
			for (int index = 0; index < jsonArray.length(); index++) {
				JSONObject childObject = jsonArray.getJSONObject(index);
				addConfigElement(AwsVpcConfigElement.JSON_KEY_INSTANCES, childObject, logger);
			}			
			break;
		case AwsVpcConfigElement.JSON_KEY_ROUTE_TABLES:
			RouteTable routeTable = new RouteTable(jsonObject, logger);
			_routeTables.put(routeTable.getId(), routeTable);
			break;
		case AwsVpcConfigElement.JSON_KEY_SECURITY_GROUPS:
			SecurityGroup sGroup = new SecurityGroup(jsonObject, logger);
			_securityGroups.put(sGroup.getId(), sGroup);			
			break;
		case AwsVpcConfigElement.JSON_KEY_SUBNETS:
			Subnet subnet = new Subnet(jsonObject, logger);
			_subnets.put(subnet.getId(), subnet);
			break;
		case AwsVpcConfigElement.JSON_KEY_VPCS:
			Vpc vpc = new Vpc(jsonObject, logger);
			_vpcs.put(vpc.getId(),  vpc);
			break;
		case AwsVpcConfigElement.JSON_KEY_VPC_PEERING_CONNECTIONS:
			VpcPeeringConnection vpcPeerConn = new VpcPeeringConnection(jsonObject, logger);
			_vpcPeerings.put(vpcPeerConn.getId(),  vpcPeerConn);
			break;
		case AwsVpcConfigElement.JSON_KEY_VPN_CONNECTIONS:
			VpnConnection vpnConnection = new VpnConnection(jsonObject, logger);
			_vpnConnections.put(vpnConnection.getId(),  vpnConnection);
			break;
		case AwsVpcConfigElement.JSON_KEY_VPN_GATEWAYS:
			VpnGateway vpnGateway = new VpnGateway(jsonObject, logger);
			_vpnGateways.put(vpnGateway.getId(),  vpnGateway);
			break;
		default:
			//do nothing here
			logger.debugf("skipping top-level element: %s\n", elementType);
		}
	}			

	public Map<String, Vpc> getVpcs() {
		return _vpcs;
	}

	private boolean ignoreElement(String key) {
		switch (key) {
		case AwsVpcConfigElement.JSON_KEY_AVAILABILITY_ZONES:
		case AwsVpcConfigElement.JSON_KEY_DHCP_OPTIONS:
		case AwsVpcConfigElement.JSON_KEY_REGIONS:			
		case AwsVpcConfigElement.JSON_KEY_TAGS:
		case AwsVpcConfigElement.JSON_KEY_INSTANCE_STATUSES:
		case AwsVpcConfigElement.JSON_KEY_PLACEMENT_GROUPS:
			return true;
		default:
			return false;
		}	
	}

   public Map<String, Configuration> toConfigurations(Warnings warnings) {
      Map<String, Configuration> configurations = new HashMap<String, Configuration>();
      
      configurations.put("hola1", new Configuration("hola1"));
      configurations.put("hola2", new Configuration("hola2"));
            
      return configurations;
   }
}

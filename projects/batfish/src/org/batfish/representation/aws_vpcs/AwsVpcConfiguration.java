package org.batfish.representation.aws_vpcs;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.batfish.common.BatfishLogger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class AwsVpcConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;

	private Map<String,Address> _addresses = new HashMap<String,Address>();

	private Map<String,Instance> _instances = new HashMap<String,Instance>();

	private Map<String,NetworkAcl> _networkAcls = new HashMap<String,NetworkAcl>();

	private Map<String,RouteTable> _routeTables = new HashMap<String,RouteTable>();

	private Map<String,Subnet> _subnets = new HashMap<String,Subnet>();

	private Map<String,Vpc> _vpcs = new HashMap<String,Vpc>();

	public void AddConfigElement(JSONObject jsonObj, BatfishLogger logger) throws JSONException {

		Iterator<?> keys = jsonObj.keys();

		while( keys.hasNext() ) {
			String key = (String)keys.next();
			JSONArray jsonArray = jsonObj.getJSONArray(key);

			for (int index = 0; index < jsonArray.length(); index++) {
				JSONObject childObject = jsonArray.getJSONObject(index);
				AddConfigElement(key, childObject, logger);
			}

		}
	}

	private void AddConfigElement(String elementType, 
			JSONObject jsonObject, BatfishLogger logger) throws JSONException {
		switch (elementType) {
		case AwsVpcConfigElement.JSON_KEY_ADDRESSES:
			Address address = new Address(jsonObject, logger);
			_addresses.put(address.getId(), address);
		case AwsVpcConfigElement.JSON_KEY_INSTANCES:
			Instance instance = new Instance(jsonObject, logger);
			_instances.put(instance.getId(), instance);
			break;
		case AwsVpcConfigElement.JSON_KEY_NETWORK_ACLS:
			NetworkAcl networkAcl = new NetworkAcl(jsonObject, logger);
			_networkAcls.put(networkAcl.getId(), networkAcl);
			break;
		case AwsVpcConfigElement.JSON_KEY_RESERVATIONS:
			//instances are embedded inside reservations
			JSONArray jsonArray = jsonObject.getJSONArray(AwsVpcConfigElement.JSON_KEY_INSTANCES);
			for (int index = 0; index < jsonArray.length(); index++) {
				JSONObject childObject = jsonArray.getJSONObject(index);
				AddConfigElement(AwsVpcConfigElement.JSON_KEY_INSTANCES, childObject, logger);
			}			
			break;
		case AwsVpcConfigElement.JSON_KEY_ROUTE_TABLES:
			RouteTable routeTable = new RouteTable(jsonObject, logger);
			_routeTables.put(routeTable.getId(), routeTable);
			break;
		case AwsVpcConfigElement.JSON_KEY_SUBNETS:
			Subnet subnet = new Subnet(jsonObject, logger);
			_subnets.put(subnet.getId(), subnet);
			break;
		case AwsVpcConfigElement.JSON_KEY_VPCS:
			Vpc vpc = new Vpc(jsonObject, logger);
			_vpcs.put(vpc.getId(),  vpc);
			break;
		default:
			//do nothing here
			logger.debugf("skipping top-level element: %s\n", elementType);
		}
	}			

	public Map<String, Vpc> getVpcs() {
		return _vpcs;
	}

}

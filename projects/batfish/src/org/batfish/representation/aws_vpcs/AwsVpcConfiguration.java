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
		case "Vpcs":
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

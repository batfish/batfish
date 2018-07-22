package org.batfish.representation.aws;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.batfish.datamodel.Ip;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class NetworkInterface implements AwsVpcEntity, Serializable {

  private static final long serialVersionUID = 1L;

  private final Ip _associationPublicIp;

  private final String _attachmentInstanceId;

  private final List<String> _groups;

  private final Map<Ip, Ip> _ipAddressAssociations;

  private final String _networkInterfaceId;

  private final String _subnetId;

  private final String _vpcId;

  public NetworkInterface(JSONObject jObj) throws JSONException {
    _groups = new LinkedList<>();
    _ipAddressAssociations = new HashMap<>();

    _networkInterfaceId = jObj.getString(JSON_KEY_NETWORK_INTERFACE_ID);

    // logger.debugf("doing network interface %s\n", _networkInterfaceId);

    _subnetId = jObj.getString(JSON_KEY_SUBNET_ID);
    _vpcId = jObj.getString(JSON_KEY_VPC_ID);

    JSONArray groups = jObj.getJSONArray(JSON_KEY_GROUPS);
    for (int index = 0; index < groups.length(); index++) {
      JSONObject childObject = groups.getJSONObject(index);
      _groups.add(childObject.getString(JSON_KEY_GROUP_ID));
    }

    JSONArray privateIpAddresses = jObj.getJSONArray(JSON_KEY_PRIVATE_IP_ADDRESSES);
    initIpAddressAssociations(privateIpAddresses);

    if (jObj.has(JSON_KEY_ASSOCIATION)) {
      JSONObject assocJson = jObj.getJSONObject(JSON_KEY_ASSOCIATION);
      _associationPublicIp = new Ip(assocJson.getString(JSON_KEY_PUBLIC_IP));
    } else {
      _associationPublicIp = null;
    }

    if (jObj.has(JSON_KEY_ATTACHMENT)) {
      JSONObject attachJson = jObj.getJSONObject(JSON_KEY_ATTACHMENT);
      if (attachJson.getString(JSON_KEY_STATUS).equals("attached")) {
        _attachmentInstanceId = Utils.tryGetString(attachJson, JSON_KEY_INSTANCE_ID);
      } else {
        _attachmentInstanceId = null;
      }
    } else {
      _attachmentInstanceId = null;
    }
  }

  public Ip getAssociationPublicIp() {
    return _associationPublicIp;
  }

  public String getAttachmentInstanceId() {
    return _attachmentInstanceId;
  }

  public List<String> getGroups() {
    return _groups;
  }

  @Override
  public String getId() {
    return _networkInterfaceId;
  }

  public Map<Ip, Ip> getIpAddressAssociations() {
    return _ipAddressAssociations;
  }

  public String getNetworkInterfaceId() {
    return _networkInterfaceId;
  }

  public String getSubnetId() {
    return _subnetId;
  }

  public String getVpcId() {
    return _vpcId;
  }

  private void initIpAddressAssociations(JSONArray associations) throws JSONException {

    for (int index = 0; index < associations.length(); index++) {
      JSONObject childObject = associations.getJSONObject(index);

      Ip privateIpAddress = new Ip(childObject.getString(JSON_KEY_PRIVATE_IP_ADDRESS));

      Ip publicIpAddress = null;

      if (childObject.has(JSON_KEY_ASSOCIATION)) {
        JSONObject assocJson = childObject.getJSONObject(JSON_KEY_ASSOCIATION);

        publicIpAddress = new Ip(assocJson.getString(JSON_KEY_PUBLIC_IP));
      }

      _ipAddressAssociations.put(privateIpAddress, publicIpAddress);
    }
  }
}

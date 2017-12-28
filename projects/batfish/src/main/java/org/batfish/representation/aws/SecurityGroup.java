package org.batfish.representation.aws;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import org.batfish.common.BatfishLogger;
import org.batfish.datamodel.IpAccessListLine;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class SecurityGroup implements AwsVpcEntity, Serializable {

  private static final long serialVersionUID = 1L;

  private final String _groupId;

  private final String _groupName;

  private final List<IpPermissions> _ipPermsEgress;

  private final List<IpPermissions> _ipPermsIngress;

  public SecurityGroup(JSONObject jObj, BatfishLogger logger) throws JSONException {
    _ipPermsEgress = new LinkedList<>();
    _ipPermsIngress = new LinkedList<>();
    _groupId = jObj.getString(JSON_KEY_GROUP_ID);
    _groupName = jObj.getString(JSON_KEY_GROUP_NAME);

    // logger.debugf("doing security group %s\n", _groupId);

    JSONArray permsEgress = jObj.getJSONArray(JSON_KEY_IP_PERMISSIONS_EGRESS);
    initIpPerms(_ipPermsEgress, permsEgress, logger);

    JSONArray permsIngress = jObj.getJSONArray(JSON_KEY_IP_PERMISSIONS);
    initIpPerms(_ipPermsIngress, permsIngress, logger);
  }

  private void addEgressAccessLines(
      List<IpPermissions> permsList, List<IpAccessListLine> accessList) {
    for (IpPermissions ipPerms : permsList) {
      accessList.add(ipPerms.toEgressIpAccessListLine());
    }
  }

  private void addIngressAccessLines(
      List<IpPermissions> permsList, List<IpAccessListLine> accessList) {
    for (IpPermissions ipPerms : permsList) {
      accessList.add(ipPerms.toIngressIpAccessListLine());
    }
  }

  public void addInOutAccessLines(
      List<IpAccessListLine> inboundRules, List<IpAccessListLine> outboundRules) {
    addIngressAccessLines(_ipPermsIngress, inboundRules);
    addEgressAccessLines(_ipPermsEgress, outboundRules);
  }

  public String getGroupId() {
    return _groupId;
  }

  public String getGroupName() {
    return _groupName;
  }

  @Override
  public String getId() {
    return _groupId;
  }

  public List<IpPermissions> getIpPermsEgress() {
    return _ipPermsEgress;
  }

  public List<IpPermissions> getIpPermsIngress() {
    return _ipPermsIngress;
  }

  private void initIpPerms(
      List<IpPermissions> ipPermsList, JSONArray ipPermsJson, BatfishLogger logger)
      throws JSONException {

    for (int index = 0; index < ipPermsJson.length(); index++) {
      JSONObject childObject = ipPermsJson.getJSONObject(index);
      ipPermsList.add(new IpPermissions(childObject, logger));
    }
  }
}

package org.batfish.representation.aws;

import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import org.batfish.common.BatfishLogger;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.TcpFlags;
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

  private void addReverseAcls(
      List<IpAccessListLine> inboundRules, List<IpAccessListLine> outboundRules) {

    List<IpAccessListLine> reverseInboundRules =
        inboundRules
            .stream()
            .map(
                ipAccessListLine ->
                    IpAccessListLine.builder()
                        .setIpProtocols(ipAccessListLine.getIpProtocols())
                        .setAction(ipAccessListLine.getAction())
                        .setDstIps(ipAccessListLine.getSrcIps())
                        .setSrcPorts(ipAccessListLine.getDstPorts())
                        .build())
            .collect(Collectors.toList());

    List<IpAccessListLine> reverseOutboundRules =
        outboundRules
            .stream()
            .map(
                ipAccessListLine ->
                    IpAccessListLine.builder()
                        .setIpProtocols(ipAccessListLine.getIpProtocols())
                        .setAction(ipAccessListLine.getAction())
                        .setSrcIps(ipAccessListLine.getDstIps())
                        .setSrcPorts(ipAccessListLine.getDstPorts())
                        .build())
            .collect(Collectors.toList());

    // denying SYN-only packets to prevent new TCP connections
    IpAccessListLine rejectSynOnly =
        IpAccessListLine.builder()
            .setTcpFlags(ImmutableSet.of(TcpFlags.builder().setAck(false).setSyn(true).build()))
            .setAction(LineAction.REJECT)
            .build();
    inboundRules.add(rejectSynOnly);
    outboundRules.add(rejectSynOnly);

    // adding reverse inbound/outbound rules for stateful allowance of packets
    outboundRules.addAll(reverseInboundRules);
    inboundRules.addAll(reverseOutboundRules);
  }

  public void addInOutAccessLines(
      List<IpAccessListLine> inboundRules, List<IpAccessListLine> outboundRules) {
    addIngressAccessLines(_ipPermsIngress, inboundRules);
    addEgressAccessLines(_ipPermsEgress, outboundRules);
    addReverseAcls(inboundRules, outboundRules);
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

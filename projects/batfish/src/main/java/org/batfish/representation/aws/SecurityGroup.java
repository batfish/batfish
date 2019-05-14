package org.batfish.representation.aws;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.TcpFlagsMatchConditions;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.visitors.HeaderSpaceConverter;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class SecurityGroup implements AwsVpcEntity, Serializable {

  private static final long serialVersionUID = 1L;

  private final String _groupId;

  private final String _groupName;

  private final List<IpPermissions> _ipPermsEgress;

  private final List<IpPermissions> _ipPermsIngress;

  private final Set<IpWildcard> _usersIpSpace = new HashSet<>();

  public SecurityGroup(JSONObject jObj) throws JSONException {
    _ipPermsEgress = new LinkedList<>();
    _ipPermsIngress = new LinkedList<>();
    _groupId = jObj.getString(JSON_KEY_GROUP_ID);
    _groupName = jObj.getString(JSON_KEY_GROUP_NAME);

    // logger.debugf("doing security group %s\n", _groupId);

    JSONArray permsEgress = jObj.getJSONArray(JSON_KEY_IP_PERMISSIONS_EGRESS);
    initIpPerms(_ipPermsEgress, permsEgress);

    JSONArray permsIngress = jObj.getJSONArray(JSON_KEY_IP_PERMISSIONS);
    initIpPerms(_ipPermsIngress, permsIngress);
  }

  private void addEgressAccessLines(
      List<IpPermissions> permsList, List<IpAccessListLine> accessList, Region region) {
    for (IpPermissions ipPerms : permsList) {
      HeaderSpace headerSpace = ipPerms.toEgressIpAccessListLine(region);
      // Destination IPs should have been populated using either SG or IP ranges,  if not then this
      // Ip perm is incomplete
      if (headerSpace.getDstIps() != null) {
        accessList.add(IpAccessListLine.acceptingHeaderSpace(headerSpace));
      }
    }
  }

  private void addIngressAccessLines(
      List<IpPermissions> permsList, List<IpAccessListLine> accessList, Region region) {
    for (IpPermissions ipPerms : permsList) {
      HeaderSpace headerSpace = ipPerms.toIngressIpAccessListLine(region);
      // Source IPs should have been populated using either SG or IP ranges, if not then this Ip
      // perm is incomplete
      if (headerSpace.getSrcIps() != null) {
        accessList.add(IpAccessListLine.acceptingHeaderSpace(headerSpace));
      }
    }
  }

  private void addReverseAcls(
      List<IpAccessListLine> inboundRules, List<IpAccessListLine> outboundRules) {

    List<IpAccessListLine> reverseInboundRules =
        inboundRules.stream()
            .map(
                ipAccessListLine -> {
                  HeaderSpace srcHeaderSpace =
                      HeaderSpaceConverter.convert(ipAccessListLine.getMatchCondition());
                  return IpAccessListLine.builder()
                      .setMatchCondition(
                          new MatchHeaderSpace(
                              HeaderSpace.builder()
                                  .setIpProtocols(srcHeaderSpace.getIpProtocols())
                                  .setDstIps(srcHeaderSpace.getSrcIps())
                                  .setSrcPorts(srcHeaderSpace.getDstPorts())
                                  .setTcpFlags(
                                      ImmutableSet.of(TcpFlagsMatchConditions.ACK_TCP_FLAG))
                                  .build()))
                      .setAction(ipAccessListLine.getAction())
                      .build();
                })
            .collect(ImmutableList.toImmutableList());

    List<IpAccessListLine> reverseOutboundRules =
        outboundRules.stream()
            .map(
                ipAccessListLine -> {
                  HeaderSpace srcHeaderSpace =
                      HeaderSpaceConverter.convert(ipAccessListLine.getMatchCondition());
                  return IpAccessListLine.builder()
                      .setMatchCondition(
                          new MatchHeaderSpace(
                              HeaderSpace.builder()
                                  .setIpProtocols(srcHeaderSpace.getIpProtocols())
                                  .setSrcIps(srcHeaderSpace.getDstIps())
                                  .setSrcPorts(srcHeaderSpace.getDstPorts())
                                  .setTcpFlags(
                                      ImmutableSet.of(TcpFlagsMatchConditions.ACK_TCP_FLAG))
                                  .build()))
                      .setAction(ipAccessListLine.getAction())
                      .build();
                })
            .collect(ImmutableList.toImmutableList());

    addToBeginning(inboundRules, reverseOutboundRules);
    addToBeginning(outboundRules, reverseInboundRules);
  }

  public void addInOutAccessLines(
      List<IpAccessListLine> inboundRules, List<IpAccessListLine> outboundRules, Region region) {
    addIngressAccessLines(_ipPermsIngress, inboundRules, region);
    addEgressAccessLines(_ipPermsEgress, outboundRules, region);
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

  public Set<IpWildcard> getUsersIpSpace() {
    return _usersIpSpace;
  }

  public void updateConfigIps(Configuration configuration) {
    configuration.getAllInterfaces().values().stream()
        .flatMap(iface -> iface.getAllAddresses().stream())
        .map(InterfaceAddress::getIp)
        .map(IpWildcard::create)
        .forEach(ipWildcard -> getUsersIpSpace().add(ipWildcard));
  }

  private void initIpPerms(List<IpPermissions> ipPermsList, JSONArray ipPermsJson)
      throws JSONException {

    for (int index = 0; index < ipPermsJson.length(); index++) {
      JSONObject childObject = ipPermsJson.getJSONObject(index);
      ipPermsList.add(new IpPermissions(childObject));
    }
  }

  private static void addToBeginning(
      List<IpAccessListLine> ipAccessListLines, List<IpAccessListLine> toBeAdded) {
    for (int i = toBeAdded.size() - 1; i >= 0; i--) {
      ipAccessListLines.add(0, toBeAdded.get(i));
    }
  }
}

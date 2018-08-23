package org.batfish.representation.aws;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.io.Serializable;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SubRange;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class IpPermissions implements Serializable {

  private static final long serialVersionUID = 1L;

  @Nullable
  public static IpProtocol toIpProtocol(String ipProtocolAsString) {
    switch (ipProtocolAsString) {
      case "tcp":
        return IpProtocol.TCP;
      case "udp":
        return IpProtocol.UDP;
      case "icmp":
        return IpProtocol.ICMP;
      case "-1":
        return null;
      default:
        try {
          int ipProtocolAsInt = Integer.parseInt(ipProtocolAsString);
          return IpProtocol.fromNumber(ipProtocolAsInt);
        } catch (NumberFormatException e) {
          throw new BatfishException(
              "Could not convert AWS IP protocol: \""
                  + ipProtocolAsString
                  + "\" to batfish Ip Protocol",
              e);
        }
    }
  }

  private int _fromPort = 0;

  private String _ipProtocol;

  private List<Prefix> _ipRanges = new LinkedList<>();

  private List<String> _securityGroups = new LinkedList<>();

  private int _toPort = 65535;

  public IpPermissions(JSONObject jObj) throws JSONException {
    _ipProtocol = jObj.getString(AwsVpcEntity.JSON_KEY_IP_PROTOCOL);

    _fromPort = Utils.tryGetInt(jObj, AwsVpcEntity.JSON_KEY_FROM_PORT, _fromPort);
    _toPort = Utils.tryGetInt(jObj, AwsVpcEntity.JSON_KEY_TO_PORT, _toPort);

    // filtering invalid values
    _fromPort = (0 <= _fromPort && _fromPort <= 65535) ? _fromPort : 0;
    _toPort = (0 <= _toPort && _toPort <= 65535) ? _toPort : 65535;

    JSONArray ranges = jObj.getJSONArray(AwsVpcEntity.JSON_KEY_IP_RANGES);

    for (int index = 0; index < ranges.length(); index++) {
      JSONObject childObject = ranges.getJSONObject(index);
      _ipRanges.add(Prefix.parse(childObject.getString(AwsVpcEntity.JSON_KEY_CIDR_IP)));
    }

    JSONArray userIdPairs = jObj.getJSONArray(AwsVpcEntity.JSON_KEY_USER_ID_PAIRS);

    for (int index = 0; index < userIdPairs.length(); index++) {
      JSONObject childObject = userIdPairs.getJSONObject(index);
      _securityGroups.add(childObject.getString(AwsVpcEntity.JSON_KEY_GROUP_ID));
    }
  }

  private SortedSet<IpWildcard> collectIpWildCards(Region region) {
    ImmutableSortedSet.Builder<IpWildcard> ipWildcardBuilder =
        new ImmutableSortedSet.Builder<>(Comparator.naturalOrder());

    _ipRanges.stream().map(IpWildcard::new).forEach(ipWildcardBuilder::add);

    _securityGroups
        .stream()
        .map(sgID -> region.getSecurityGroups().get(sgID))
        .filter(Objects::nonNull)
        .flatMap(sg -> sg.getUsersIpSpace().stream())
        .forEach(ipWildcardBuilder::add);
    return ipWildcardBuilder.build();
  }

  public HeaderSpace toEgressIpAccessListLine(Region region) {
    return toHeaderSpaceBuilder().setDstIps(collectIpWildCards(region)).build();
  }

  public HeaderSpace toIngressIpAccessListLine(Region region) {
    return toHeaderSpaceBuilder().setSrcIps(collectIpWildCards(region)).build();
  }

  private HeaderSpace.Builder toHeaderSpaceBuilder() {
    HeaderSpace.Builder headerSpaceBuilder = HeaderSpace.builder();
    //    line.setAction(LineAction.PERMIT);
    IpProtocol protocol = toIpProtocol(_ipProtocol);
    if (protocol != null) {
      headerSpaceBuilder.setIpProtocols(ImmutableSet.of(protocol));
    }

    // if the range isn't all ports, set it in ACL
    if (_fromPort != 0 || _toPort != 65535) {
      headerSpaceBuilder.setDstPorts(ImmutableSet.of(new SubRange(_fromPort, _toPort)));
    }

    return headerSpaceBuilder;
  }
}

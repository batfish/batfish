package org.batfish.representation.aws_vpcs;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SubRange;
import org.batfish.representation.IpAccessListLine;
import org.batfish.representation.IpWildcard;
import org.batfish.representation.LineAction;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class IpPermissions implements Serializable {

   private static final long serialVersionUID = 1L;

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
         }
         catch (NumberFormatException e) {
            throw new BatfishException("Could not convert AWS IP protocol: \""
                  + ipProtocolAsString + "\" to batfish Ip Protocol", e);
         }

      }

   }

   private int _fromPort = -1;

   private String _ipProtocol;

   private List<Prefix> _ipRanges = new LinkedList<Prefix>();

   private int _toPort = -1;

   public IpPermissions(JSONObject jObj, BatfishLogger logger)
         throws JSONException {
      _ipProtocol = jObj.getString(AwsVpcEntity.JSON_KEY_IP_PROTOCOL);

      _fromPort = Utils.tryGetInt(jObj, AwsVpcEntity.JSON_KEY_FROM_PORT,
            _fromPort);
      _toPort = Utils.tryGetInt(jObj, AwsVpcEntity.JSON_KEY_TO_PORT, _toPort);

      JSONArray ranges = jObj.getJSONArray(AwsVpcEntity.JSON_KEY_IP_RANGES);

      for (int index = 0; index < ranges.length(); index++) {
         JSONObject childObject = ranges.getJSONObject(index);
         _ipRanges.add(new Prefix(childObject
               .getString(AwsVpcEntity.JSON_KEY_CIDR_IP)));
      }
   }

   public IpAccessListLine toEgressIpAccessListLine() {
      IpAccessListLine line = toIpAccessListLine();
      for (Prefix ipRange : _ipRanges) {
         IpWildcard wildcard = new IpWildcard(ipRange);
         line.getDstIpWildcards().add(wildcard);
      }
      return line;
   }

   public IpAccessListLine toIngressIpAccessListLine() {
      IpAccessListLine line = toIpAccessListLine();
      for (Prefix ipRange : _ipRanges) {
         IpWildcard wildcard = new IpWildcard(ipRange);
         line.getSrcIpWildcards().add(wildcard);
      }
      return line;
   }

   private IpAccessListLine toIpAccessListLine() {
      IpAccessListLine line = new IpAccessListLine();
      line.setAction(LineAction.ACCEPT);
      IpProtocol protocol = toIpProtocol(_ipProtocol);
      if (protocol != null) {
         line.getProtocols().add(protocol);
      }
      if (_fromPort != -1) {
         line.getSrcPortRanges().add(new SubRange(_fromPort, _fromPort));
      }
      if (_toPort != -1) {
         line.getDstPortRanges().add(new SubRange(_toPort, _toPort));
      }
      return line;
   }

}
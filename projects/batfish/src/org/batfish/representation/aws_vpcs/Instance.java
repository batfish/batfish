package org.batfish.representation.aws_vpcs;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.main.ConfigurationFormat;
import org.batfish.representation.Configuration;
import org.batfish.representation.Interface;
import org.batfish.representation.Ip;
import org.batfish.representation.IpAccessList;
import org.batfish.representation.IpAccessListLine;
import org.batfish.representation.LineAction;
import org.batfish.representation.Prefix;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class Instance implements AwsVpcEntity, Serializable {

   private static final long serialVersionUID = 1L;

   private transient IpAccessList _inAcl;

   private String _instanceId;

   private List<String> _networkInterfaces = new LinkedList<String>();

   private transient IpAccessList _outAcl;

   private List<String> _securityGroups = new LinkedList<String>();

   private String _subnetId;

   private String _vpcId;

   public Instance(JSONObject jObj, BatfishLogger logger) throws JSONException {
      _instanceId = jObj.getString(JSON_KEY_INSTANCE_ID);

      if (jObj.has(JSON_KEY_VPC_ID)) {
         _vpcId = jObj.getString(JSON_KEY_VPC_ID);

         _subnetId = jObj.getString(JSON_KEY_SUBNET_ID);
      }

      JSONArray securityGroups = jObj.getJSONArray(JSON_KEY_SECURITY_GROUPS);
      initSecurityGroups(securityGroups, logger);

      JSONArray networkInterfaces = jObj
            .getJSONArray(JSON_KEY_NETWORK_INTERFACES);
      initNetworkInterfaces(networkInterfaces, logger);

      // check if the public and private ip addresses are associated with an
      // interface
   }

   @Override
   public String getId() {
      return _instanceId;
   }

   public IpAccessList getInAcl() {
      return _inAcl;
   }

   public IpAccessList getOutAcl() {
      return _outAcl;
   }

   private void initNetworkInterfaces(JSONArray routes, BatfishLogger logger)
         throws JSONException {

      for (int index = 0; index < routes.length(); index++) {
         JSONObject childObject = routes.getJSONObject(index);
         _networkInterfaces.add(childObject
               .getString(JSON_KEY_NETWORK_INTERFACE_ID));
      }
   }

   private void initSecurityGroups(JSONArray associations, BatfishLogger logger)
         throws JSONException {

      for (int index = 0; index < associations.length(); index++) {
         JSONObject childObject = associations.getJSONObject(index);
         _securityGroups.add(childObject.getString(JSON_KEY_GROUP_ID));
      }
   }

   public Configuration toConfigurationNode(AwsVpcConfiguration awsVpcConfig) {
      String sgIngressAclName = "~SECURITY_GROUP_INGRESS_ACL~";
      String sgEgressAclName = "~SECURITY_GROUP_EGRESS_ACL~";
      Configuration cfgNode = new Configuration(_instanceId);

      List<IpAccessListLine> inboundRules = new LinkedList<IpAccessListLine>();
      List<IpAccessListLine> outboundRules = new LinkedList<IpAccessListLine>();
      // create ACLs from inboundRules and outboundRules
      IpAccessList inAcl = new IpAccessList(sgIngressAclName, inboundRules);
      IpAccessList outAcl = new IpAccessList(sgEgressAclName, outboundRules);
      cfgNode.getIpAccessLists().put(sgIngressAclName, inAcl);
      cfgNode.getIpAccessLists().put(sgEgressAclName, outAcl);
      _inAcl = inAcl;
      _outAcl = outAcl;

      for (String sGroupId : _securityGroups) {
         SecurityGroup sGroup = awsVpcConfig.getSecurityGroups().get(sGroupId);

         if (sGroup == null) {
            throw new BatfishException("Security group " + sGroupId
                  + " for instance " + _instanceId + " not found");
         }

         sGroup.addInOutAccessLines(inboundRules, outboundRules);
      }

      for (String interfaceId : _networkInterfaces) {

         NetworkInterface netInterface = awsVpcConfig.getNetworkInterfaces()
               .get(interfaceId);

         if (netInterface == null) {
            throw new BatfishException("Network interface " + interfaceId
                  + " for instance " + _instanceId + " not found");
         }

         Interface iface = new Interface(interfaceId, cfgNode);

         Set<Ip> privateIpAddresses = new TreeSet<Ip>();
         privateIpAddresses.addAll(netInterface.getIpAddressAssociations()
               .keySet());
         Subnet subnet = awsVpcConfig.getSubnets().get(
               netInterface.getSubnetId());
         Prefix ifaceSubnet = subnet.getCidrBlock();
         for (Ip ip : privateIpAddresses) {
            if (!ifaceSubnet.contains(ip)) {
               throw new BatfishException("Instance subnet: "
                     + ifaceSubnet.toString()
                     + " does not contain private ip: " + ip.toString());
            }
            if (ip.equals(ifaceSubnet.getEndAddress())) {
               throw new BatfishException("Expected end address: "
                     + ip.toString() + " to be used by generated subnet node");
            }
            Prefix prefix = new Prefix(ip, ifaceSubnet.getPrefixLength());
            iface.getAllPrefixes().add(prefix);
         }
         Ip lowestIp = privateIpAddresses.toArray(new Ip[] {})[0];
         iface.setPrefix(new Prefix(lowestIp, ifaceSubnet.getPrefixLength()));

         // apply ACLs to interface
         iface.setIncomingFilter(inAcl);
         iface.setOutgoingFilter(outAcl);
         cfgNode.getInterfaces().put(interfaceId, iface);

      }

      return cfgNode;
   }
}
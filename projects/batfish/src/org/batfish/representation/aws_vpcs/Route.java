package org.batfish.representation.aws_vpcs;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.representation.Ip;
import org.batfish.representation.Prefix;
import org.batfish.representation.StaticRoute;
import org.batfish.util.Util;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class Route implements Serializable {

   public enum TargetType {
      Gateway,
      Instance,
      NetworkInterface,
      VpcPeeringConnection
   }

   private static final int DEFAULT_STATIC_ROUTE_ADMIN = 1;

   private static final int DEFAULT_STATIC_ROUTE_COST = 0;

   private static final long serialVersionUID = 1L;

   private Prefix _destinationCidrBlock;
   private String _state;
   private String _target;
   private TargetType _targetType;

   public Route(JSONObject jObj, BatfishLogger logger) throws JSONException {
      _destinationCidrBlock = new Prefix(
            jObj.getString(AwsVpcEntity.JSON_KEY_DESTINATION_CIDR_BLOCK));
      _state = jObj.getString(AwsVpcEntity.JSON_KEY_STATE);

      if (jObj.has(AwsVpcEntity.JSON_KEY_VPC_PEERING_CONNECTION_ID)) {
         _targetType = TargetType.VpcPeeringConnection;
         _target = jObj
               .getString(AwsVpcEntity.JSON_KEY_VPC_PEERING_CONNECTION_ID);
      }
      else if (jObj.has(AwsVpcEntity.JSON_KEY_GATEWAY_ID)) {
         _targetType = TargetType.Gateway;
         _target = jObj.getString(AwsVpcEntity.JSON_KEY_GATEWAY_ID);
      }
      else if (jObj.has(AwsVpcEntity.JSON_KEY_INSTANCE_ID)) {
         _targetType = TargetType.Instance;
         _target = jObj.getString(AwsVpcEntity.JSON_KEY_INSTANCE_ID);
      }
      else if (jObj.has(AwsVpcEntity.JSON_KEY_NETWORK_INTERFACE_ID)) {
         _targetType = TargetType.NetworkInterface;
         _target = jObj.getString(AwsVpcEntity.JSON_KEY_NETWORK_INTERFACE_ID);
      }
      else {
         throw new JSONException("Target not found in route " + jObj.toString());
      }
   }

   public StaticRoute toStaticRoute(AwsVpcConfiguration awsVpcConfiguration,
         Ip vpcAddress, Ip igwAddress, Ip vgwAddress, Subnet subnet) {
      StaticRoute staticRoute;
      if (_state.equals("blackhole")) {
         staticRoute = new StaticRoute(_destinationCidrBlock, null,
               Util.NULL_INTERFACE_NAME, DEFAULT_STATIC_ROUTE_ADMIN,
               DEFAULT_STATIC_ROUTE_COST);
      }
      else {
         switch (_targetType) {
         case Gateway:
            if (_target.equals("local")) {
               // send to the vpc router
               staticRoute = new StaticRoute(_destinationCidrBlock, vpcAddress,
                     null, DEFAULT_STATIC_ROUTE_ADMIN,
                     DEFAULT_STATIC_ROUTE_COST);
            }
            else {
               // send to the specified internet gateway
               // if it's not the igw or vgw set for this subnet (and VPC),
               // throw an
               // exception
               if (_target.equals(subnet.getInternetGatewayId())) {
                  staticRoute = new StaticRoute(_destinationCidrBlock,
                        igwAddress, null, DEFAULT_STATIC_ROUTE_ADMIN,
                        DEFAULT_STATIC_ROUTE_COST);
               }
               else if (_target.equals(subnet.getVpnGatewayId())) {
                  staticRoute = new StaticRoute(_destinationCidrBlock,
                        vgwAddress, null, DEFAULT_STATIC_ROUTE_ADMIN,
                        DEFAULT_STATIC_ROUTE_COST);
               }
               else {
                  throw new BatfishException(
                        "Internet gateway \""
                              + _target
                              + "\" specified in this route not accessible from this subnet");
               }
            }
            break;

         case NetworkInterface:
            NetworkInterface networkInterface = awsVpcConfiguration
                  .getNetworkInterfaces().get(_target);
            if (!networkInterface.getSubnetId().equals(subnet.getId())) {
               throw new BatfishException(
                     "Do not support network interface on different subnet");
            }
            Set<Ip> networkInterfaceIps = new TreeSet<Ip>();
            networkInterfaceIps.addAll(networkInterface
                  .getIpAddressAssociations().keySet());
            Ip lowestIp = networkInterfaceIps.toArray(new Ip[] {})[0];
            if (!subnet.getCidrBlock().contains(lowestIp)) {
               throw new BatfishException(
                     "Ip of network interface specified in static route not in containing subnet");
            }
            staticRoute = new StaticRoute(_destinationCidrBlock, lowestIp,
                  null, DEFAULT_STATIC_ROUTE_ADMIN, DEFAULT_STATIC_ROUTE_COST);
            break;

         case VpcPeeringConnection:
            // TODO: create route for vpc peering connection
            awsVpcConfiguration.getWarnings().redFlag(
                  "Skipping creating route to "
                        + _destinationCidrBlock.toString()
                        + " for vpc peering connection: \"" + _target + "\"");
            return null;

         case Instance:
            // TODO: create route for instance
            awsVpcConfiguration.getWarnings().redFlag(
                  "Skipping creating route to "
                        + _destinationCidrBlock.toString()
                        + " for instance: \"" + _target + "\"");
            return null;

         default:
            throw new BatfishException("Unsupported target type: "
                  + _targetType.toString());

         }
      }
      return staticRoute;
   }
}

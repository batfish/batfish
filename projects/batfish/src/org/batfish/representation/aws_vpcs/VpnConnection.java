package org.batfish.representation.aws_vpcs;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.representation.BgpNeighbor;
import org.batfish.representation.BgpProcess;
import org.batfish.representation.Configuration;
import org.batfish.representation.DiffieHellmanGroup;
import org.batfish.representation.EncryptionAlgorithm;
import org.batfish.representation.IkeAuthenticationAlgorithm;
import org.batfish.representation.IkeAuthenticationMethod;
import org.batfish.representation.IkeGateway;
import org.batfish.representation.IkePolicy;
import org.batfish.representation.IkeProposal;
import org.batfish.representation.Interface;
import org.batfish.representation.IpsecAuthenticationAlgorithm;
import org.batfish.representation.IpsecPolicy;
import org.batfish.representation.IpsecProposal;
import org.batfish.representation.IpsecProtocol;
import org.batfish.representation.IpsecVpn;
import org.batfish.representation.LineAction;
import org.batfish.representation.PolicyMap;
import org.batfish.representation.PolicyMapAction;
import org.batfish.representation.PolicyMapClause;
import org.batfish.representation.PolicyMapMatchProtocolLine;
import org.batfish.representation.PolicyMapMatchRouteFilterListLine;
import org.batfish.representation.Prefix;
import org.batfish.representation.RouteFilterLine;
import org.batfish.representation.RouteFilterList;
import org.batfish.representation.RoutingProtocol;
import org.batfish.representation.StaticRoute;
import org.batfish.util.SubRange;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class VpnConnection implements AwsVpcEntity, Serializable {

   private static final Integer BGP_NEIGHBOR_DEFAULT_METRIC = 0;

   private static final long serialVersionUID = 1L;

   private static DiffieHellmanGroup toDiffieHellmanGroup(
         String perfectForwardSecrecy) {
      switch (perfectForwardSecrecy) {
      case "group2":
         return DiffieHellmanGroup.GROUP2;
      default:
         throw new BatfishException(
               "No conversion to Diffie-Hellman group for string: \""
                     + perfectForwardSecrecy + "\"");
      }
   }

   private static EncryptionAlgorithm toEncryptionAlgorithm(
         String encryptionProtocol) {
      switch (encryptionProtocol) {
      case "aes-128-cbc":
         return EncryptionAlgorithm.AES_128_CBC;
      default:
         throw new BatfishException(
               "No conversion to encryption algorithm for string: \""
                     + encryptionProtocol + "\"");
      }
   }

   private static IkeAuthenticationAlgorithm toIkeAuthenticationAlgorithm(
         String ikeAuthProtocol) {
      switch (ikeAuthProtocol) {

      case "sha1":
         return IkeAuthenticationAlgorithm.SHA1;

      default:
         throw new BatfishException(
               "No conversion to ike authentication algorithm for string: \""
                     + ikeAuthProtocol + "\"");
      }
   }

   private static IpsecAuthenticationAlgorithm toIpsecAuthenticationAlgorithm(
         String ipsecAuthProtocol) {
      switch (ipsecAuthProtocol) {
      case "hmac-sha1-96":
         return IpsecAuthenticationAlgorithm.HMAC_SHA1_96;
      default:
         throw new BatfishException(
               "No conversion to ipsec authentication algorithm for string: \""
                     + ipsecAuthProtocol + "\"");
      }
   }

   private static IpsecProtocol toIpsecProtocol(String ipsecProtocol) {
      switch (ipsecProtocol) {
      case "esp":
         return IpsecProtocol.ESP;
      default:
         throw new BatfishException(
               "No conversion to ipsec protocol for string: \"" + ipsecProtocol
                     + "\"");
      }
   }

   private String _customerGatewayId;

   private List<IpsecTunnel> _ipsecTunnels = new LinkedList<IpsecTunnel>();

   private List<Prefix> _routes = new LinkedList<Prefix>();

   private boolean _staticRoutesOnly = false;

   private List<VgwTelemetry> _vgwTelemetrys = new LinkedList<VgwTelemetry>();

   private String _vpnConnectionId;

   private String _vpnGatewayId;

   public VpnConnection(JSONObject jObj, BatfishLogger logger)
         throws JSONException {
      _vpnConnectionId = jObj.getString(JSON_KEY_VPN_CONNECTION_ID);
      _customerGatewayId = jObj.getString(JSON_KEY_CUSTOMER_GATEWAY_ID);
      _vpnGatewayId = jObj.getString(JSON_KEY_VPN_GATEWAY_ID);

      logger.debugf("parsing vpnconnection id: %s\n", _vpnConnectionId);

      String cgwConfiguration = jObj
            .getString(JSON_KEY_CUSTOMER_GATEWAY_CONFIGURATION);
      Document document;
      try {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         DocumentBuilder builder = factory.newDocumentBuilder();
         InputSource is = new InputSource(new StringReader(cgwConfiguration));
         document = builder.parse(is);
      }
      catch (ParserConfigurationException | SAXException | IOException e) {
         throw new BatfishException(
               "Could not parse XML for CustomerGatewayConfiguration for vpn connection "
                     + _vpnConnectionId + " " + e);
      }

      Element vpnConnection = (Element) document.getElementsByTagName(
            XML_KEY_VPN_CONNECTION).item(0);

      NodeList nodeList = document.getElementsByTagName(XML_KEY_IPSEC_TUNNEL);

      for (int index = 0; index < nodeList.getLength(); index++) {
         Element ipsecTunnel = (Element) nodeList.item(index);
         _ipsecTunnels.add(new IpsecTunnel(ipsecTunnel, vpnConnection));
      }

      if (jObj.has(JSON_KEY_ROUTES)) {
         JSONArray routes = jObj.getJSONArray(JSON_KEY_ROUTES);
         for (int index = 0; index < routes.length(); index++) {
            JSONObject childObject = routes.getJSONObject(index);
            _routes.add(new Prefix(childObject
                  .getString(JSON_KEY_DESTINATION_CIDR_BLOCK)));
         }
      }

      JSONArray vgwTelemetry = jObj.getJSONArray(JSON_KEY_VGW_TELEMETRY);
      for (int index = 0; index < vgwTelemetry.length(); index++) {
         JSONObject childObject = vgwTelemetry.getJSONObject(index);
         _vgwTelemetrys.add(new VgwTelemetry(childObject, logger));
      }

      if (jObj.has(JSON_KEY_OPTIONS)) {
         JSONObject options = jObj.getJSONObject(JSON_KEY_OPTIONS);
         _staticRoutesOnly = Utils.tryGetBoolean(options,
               JSON_KEY_STATIC_ROUTES_ONLY, false);
      }
   }

   public void applyToVpnGateway(AwsVpcConfiguration awsVpcConfiguration) {
      Configuration vpnGatewayCfgNode = awsVpcConfiguration
            .getConfigurationNodes().get(_vpnGatewayId);
      for (int i = 0; i < _ipsecTunnels.size(); i++) {
         int idNum = i + 1;
         String vpnId = _vpnConnectionId + "-" + idNum;
         IpsecTunnel ipsecTunnel = _ipsecTunnels.get(i);
         if (ipsecTunnel.getCgwBgpAsn() != -1
               && (_staticRoutesOnly || _routes.size() != 0)) {
            throw new BatfishException(
                  "Unexpected combination of BGP and static routes for VPN connection: \""
                        + _vpnConnectionId + "\"");
         }
         // create representation structures and add to configuration node
         IpsecVpn ipsecVpn = new IpsecVpn(vpnId, vpnGatewayCfgNode);
         vpnGatewayCfgNode.getIpsecVpns().put(vpnId, ipsecVpn);
         IpsecPolicy ipsecPolicy = new IpsecPolicy(vpnId);
         vpnGatewayCfgNode.getIpsecPolicies().put(vpnId, ipsecPolicy);
         ipsecVpn.setIpsecPolicy(ipsecPolicy);
         IpsecProposal ipsecProposal = new IpsecProposal(vpnId);
         vpnGatewayCfgNode.getIpsecProposals().put(vpnId, ipsecProposal);
         ipsecPolicy.getProposals().put(vpnId, ipsecProposal);
         IkeGateway ikeGateway = new IkeGateway(vpnId);
         vpnGatewayCfgNode.getIkeGateways().put(vpnId, ikeGateway);
         ipsecVpn.setGateway(ikeGateway);
         IkePolicy ikePolicy = new IkePolicy(vpnId);
         vpnGatewayCfgNode.getIkePolicies().put(vpnId, ikePolicy);
         ikeGateway.setIkePolicy(ikePolicy);
         IkeProposal ikeProposal = new IkeProposal(vpnId);
         vpnGatewayCfgNode.getIkeProposals().put(vpnId, ikeProposal);
         ikePolicy.getProposals().put(vpnId, ikeProposal);
         String externalInterfaceName = "external" + idNum;
         Interface externalInterface = new Interface(externalInterfaceName,
               vpnGatewayCfgNode);
         vpnGatewayCfgNode.getInterfaces().put(externalInterfaceName,
               externalInterface);
         String vpnInterfaceName = "vpn" + idNum;
         Interface vpnInterface = new Interface(vpnInterfaceName,
               vpnGatewayCfgNode);
         vpnGatewayCfgNode.getInterfaces().put(vpnInterfaceName, vpnInterface);

         // Set fields within representation structures

         // bind and vpn interfaces
         Prefix externalInterfacePrefix = new Prefix(
               ipsecTunnel.getVgwOutsideAddress(), 32);
         externalInterface.setPrefix(externalInterfacePrefix);
         externalInterface.getAllPrefixes().add(externalInterfacePrefix);
         Prefix vpnInterfacePrefix = new Prefix(
               ipsecTunnel.getVgwInsideAddress(),
               ipsecTunnel.getVgwInsidePrefixLength());
         vpnInterface.setPrefix(vpnInterfacePrefix);
         vpnInterface.getAllPrefixes().add(vpnInterfacePrefix);

         // ipsec
         ipsecVpn.setBindInterface(vpnInterface);
         ipsecPolicy.setPfsKeyGroup(toDiffieHellmanGroup(ipsecTunnel
               .getIpsecPerfectForwardSecrecy()));
         ipsecProposal
               .setAuthenticationAlgorithm(toIpsecAuthenticationAlgorithm(ipsecTunnel
                     .getIpsecAuthProtocol()));
         ipsecProposal.setEncryptionAlgorithm(toEncryptionAlgorithm(ipsecTunnel
               .getIpsecEncryptionProtocol()));
         ipsecProposal.setProtocol(toIpsecProtocol(ipsecTunnel
               .getIpsecProtocol()));
         ipsecProposal.setLifetimeSeconds(ipsecTunnel.getIpsecLifetime());

         // ike
         ikeGateway.setExternalInterface(externalInterface);
         ikeGateway.setAddress(ipsecTunnel.getCgwOutsideAddress());
         if (ipsecTunnel.getIkePreSharedKeyHash() != null) {
            ikePolicy.setPreSharedKeyHash(ipsecTunnel.getIkePreSharedKeyHash());
            ikeProposal
                  .setAuthenticationMethod(IkeAuthenticationMethod.PRE_SHARED_KEYS);
         }
         ikeProposal
               .setAuthenticationAlgorithm(toIkeAuthenticationAlgorithm(ipsecTunnel
                     .getIkeAuthProtocol()));
         ikeProposal.setDiffieHellmanGroup(toDiffieHellmanGroup(ipsecTunnel
               .getIkePerfectForwardSecrecy()));
         ikeProposal.setEncryptionAlgorithm(toEncryptionAlgorithm(ipsecTunnel
               .getIkeEncryptionProtocol()));
         ikeProposal.setLifetimeSeconds(ipsecTunnel.getIkeLifetime());

         // bgp (if configured)
         if (ipsecTunnel.getVgwBgpAsn() != -1) {
            BgpProcess proc = vpnGatewayCfgNode.getBgpProcess();
            if (proc == null) {
               proc = new BgpProcess();
               vpnGatewayCfgNode.setBgpProcess(proc);
            }
            BgpNeighbor cgBgpNeighbor = new BgpNeighbor(
                  ipsecTunnel.getCgwInsideAddress(), vpnGatewayCfgNode);
            proc.getNeighbors().put(cgBgpNeighbor.getPrefix(), cgBgpNeighbor);
            cgBgpNeighbor.setRemoteAs(ipsecTunnel.getCgwBgpAsn());
            cgBgpNeighbor.setLocalAs(ipsecTunnel.getVgwBgpAsn());
            cgBgpNeighbor.setLocalIp(ipsecTunnel.getVgwInsideAddress());
            cgBgpNeighbor.setDefaultMetric(BGP_NEIGHBOR_DEFAULT_METRIC);
            cgBgpNeighbor.setSendCommunity(false);
            VpnGateway vpnGateway = awsVpcConfiguration.getVpnGateways().get(
                  _vpnGatewayId);
            List<String> attachmentVpcIds = vpnGateway.getAttachmentVpcIds();
            if (attachmentVpcIds.size() != 1) {
               throw new BatfishException(
                     "Not sure what routes to advertise since VPN Gateway: \""
                           + _vpnGatewayId + "\" for VPN connection: \""
                           + _vpnConnectionId + "\" is linked to multiple VPCs");
            }
            String vpcId = attachmentVpcIds.get(0);
            Vpc vpc = awsVpcConfiguration.getVpcs().get(vpcId);
            Prefix outgoingPrefix = vpc.getCidrBlock();
            int outgoingPrefixLength = outgoingPrefix.getPrefixLength();
            String originationPolicyName = vpnId + "_origination";
            PolicyMap originationPolicy = new PolicyMap(originationPolicyName);
            vpnGatewayCfgNode.getPolicyMaps().put(originationPolicyName, originationPolicy);
            cgBgpNeighbor.getOriginationPolicies().add(originationPolicy);
            PolicyMapClause originationClause = new PolicyMapClause();
            originationPolicy.getClauses().add(originationClause);
            originationClause.setAction(PolicyMapAction.PERMIT);
            RouteFilterList originationRouteFilter = new RouteFilterList(originationPolicyName);
            vpnGatewayCfgNode.getRouteFilterLists().put(originationPolicyName, originationRouteFilter);
            RouteFilterLine matchOutgoingPrefix = new RouteFilterLine(LineAction.ACCEPT, outgoingPrefix, new SubRange(outgoingPrefixLength, outgoingPrefixLength));
            originationRouteFilter.addLine(matchOutgoingPrefix);
            PolicyMapMatchRouteFilterListLine matchLine = new PolicyMapMatchRouteFilterListLine(Collections.singleton(originationRouteFilter));
            originationClause.getMatchLines().add(matchLine);
            PolicyMapMatchProtocolLine matchStatic = new PolicyMapMatchProtocolLine(RoutingProtocol.STATIC);
            originationClause.getMatchLines().add(matchStatic);
//            cgBgpNeighbor.getOutboundPolicyMaps().add(originationPolicy);
         }

         // static routes (if configured)
         for (Prefix staticRoutePrefix : _routes) {
            StaticRoute staticRoute = new StaticRoute(staticRoutePrefix,
                  ipsecTunnel.getCgwInsideAddress(), null,
                  Route.DEFAULT_STATIC_ROUTE_ADMIN,
                  Route.DEFAULT_STATIC_ROUTE_COST);
            vpnGatewayCfgNode.getStaticRoutes().add(staticRoute);
         }
      }
   }

   @Override
   public String getId() {
      return _vpnConnectionId;
   }
}

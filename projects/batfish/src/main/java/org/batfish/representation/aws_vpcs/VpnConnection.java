package org.batfish.representation.aws_vpcs;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.datamodel.BgpNeighbor;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DiffieHellmanGroup;
import org.batfish.datamodel.EncryptionAlgorithm;
import org.batfish.datamodel.IkeAuthenticationAlgorithm;
import org.batfish.datamodel.IkeAuthenticationMethod;
import org.batfish.datamodel.IkeGateway;
import org.batfish.datamodel.IkePolicy;
import org.batfish.datamodel.IkeProposal;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.IpsecAuthenticationAlgorithm;
import org.batfish.datamodel.IpsecPolicy;
import org.batfish.datamodel.IpsecProposal;
import org.batfish.datamodel.IpsecProtocol;
import org.batfish.datamodel.IpsecVpn;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RouteFilterLine;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class VpnConnection implements AwsVpcEntity, Serializable {

  private static final int BGP_NEIGHBOR_DEFAULT_METRIC = 0;

  private static final long serialVersionUID = 1L;

  private static DiffieHellmanGroup toDiffieHellmanGroup(String perfectForwardSecrecy) {
    switch (perfectForwardSecrecy) {
      case "group2":
        return DiffieHellmanGroup.GROUP2;
      default:
        throw new BatfishException(
            "No conversion to Diffie-Hellman group for string: \"" + perfectForwardSecrecy + "\"");
    }
  }

  private static EncryptionAlgorithm toEncryptionAlgorithm(String encryptionProtocol) {
    switch (encryptionProtocol) {
      case "aes-128-cbc":
        return EncryptionAlgorithm.AES_128_CBC;
      default:
        throw new BatfishException(
            "No conversion to encryption algorithm for string: \"" + encryptionProtocol + "\"");
    }
  }

  private static IkeAuthenticationAlgorithm toIkeAuthenticationAlgorithm(String ikeAuthProtocol) {
    switch (ikeAuthProtocol) {
      case "sha1":
        return IkeAuthenticationAlgorithm.SHA1;

      default:
        throw new BatfishException(
            "No conversion to ike authentication algorithm for string: \""
                + ikeAuthProtocol
                + "\"");
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
                + ipsecAuthProtocol
                + "\"");
    }
  }

  private static IpsecProtocol toIpsecProtocol(String ipsecProtocol) {
    switch (ipsecProtocol) {
      case "esp":
        return IpsecProtocol.ESP;
      default:
        throw new BatfishException(
            "No conversion to ipsec protocol for string: \"" + ipsecProtocol + "\"");
    }
  }

  private final String _customerGatewayId;

  private final List<IpsecTunnel> _ipsecTunnels;

  private final List<Prefix> _routes;

  private final boolean _staticRoutesOnly;

  private final List<VgwTelemetry> _vgwTelemetrys;

  private final String _vpnConnectionId;

  private final String _vpnGatewayId;

  public VpnConnection(JSONObject jObj, BatfishLogger logger) throws JSONException {
    _vgwTelemetrys = new LinkedList<>();
    _routes = new LinkedList<>();
    _ipsecTunnels = new LinkedList<>();
    _vpnConnectionId = jObj.getString(JSON_KEY_VPN_CONNECTION_ID);
    _customerGatewayId = jObj.getString(JSON_KEY_CUSTOMER_GATEWAY_ID);
    _vpnGatewayId = jObj.getString(JSON_KEY_VPN_GATEWAY_ID);

    logger.debugf("parsing vpnconnection id: %s\n", _vpnConnectionId);

    String cgwConfiguration = jObj.getString(JSON_KEY_CUSTOMER_GATEWAY_CONFIGURATION);
    Document document;
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      InputSource is = new InputSource(new StringReader(cgwConfiguration));
      document = builder.parse(is);
    } catch (ParserConfigurationException | SAXException | IOException e) {
      throw new BatfishException(
          "Could not parse XML for CustomerGatewayConfiguration for vpn connection "
              + _vpnConnectionId
              + " "
              + e);
    }

    Element vpnConnection = (Element) document.getElementsByTagName(XML_KEY_VPN_CONNECTION).item(0);

    NodeList nodeList = document.getElementsByTagName(XML_KEY_IPSEC_TUNNEL);

    for (int index = 0; index < nodeList.getLength(); index++) {
      Element ipsecTunnel = (Element) nodeList.item(index);
      _ipsecTunnels.add(new IpsecTunnel(ipsecTunnel, vpnConnection));
    }

    if (jObj.has(JSON_KEY_ROUTES)) {
      JSONArray routes = jObj.getJSONArray(JSON_KEY_ROUTES);
      for (int index = 0; index < routes.length(); index++) {
        JSONObject childObject = routes.getJSONObject(index);
        _routes.add(new Prefix(childObject.getString(JSON_KEY_DESTINATION_CIDR_BLOCK)));
      }
    }

    JSONArray vgwTelemetry = jObj.getJSONArray(JSON_KEY_VGW_TELEMETRY);
    for (int index = 0; index < vgwTelemetry.length(); index++) {
      JSONObject childObject = vgwTelemetry.getJSONObject(index);
      _vgwTelemetrys.add(new VgwTelemetry(childObject, logger));
    }

    if (jObj.has(JSON_KEY_OPTIONS)) {
      JSONObject options = jObj.getJSONObject(JSON_KEY_OPTIONS);
      _staticRoutesOnly = Utils.tryGetBoolean(options, JSON_KEY_STATIC_ROUTES_ONLY, false);
    } else {
      _staticRoutesOnly = false;
    }
  }

  public void applyToVpnGateway(AwsVpcConfiguration awsVpcConfiguration) {
    Configuration vpnGatewayCfgNode =
        awsVpcConfiguration.getConfigurationNodes().get(_vpnGatewayId);
    for (int i = 0; i < _ipsecTunnels.size(); i++) {
      int idNum = i + 1;
      String vpnId = _vpnConnectionId + "-" + idNum;
      IpsecTunnel ipsecTunnel = _ipsecTunnels.get(i);
      if (ipsecTunnel.getCgwBgpAsn() != -1 && (_staticRoutesOnly || _routes.size() != 0)) {
        throw new BatfishException(
            "Unexpected combination of BGP and static routes for VPN connection: \""
                + _vpnConnectionId
                + "\"");
      }
      // create representation structures and add to configuration node
      IpsecVpn ipsecVpn = new IpsecVpn(vpnId, vpnGatewayCfgNode);
      vpnGatewayCfgNode.getIpsecVpns().put(vpnId, ipsecVpn);
      IpsecPolicy ipsecPolicy = new IpsecPolicy(vpnId);
      vpnGatewayCfgNode.getIpsecPolicies().put(vpnId, ipsecPolicy);
      ipsecVpn.setIpsecPolicy(ipsecPolicy);
      IpsecProposal ipsecProposal = new IpsecProposal(vpnId, -1);
      vpnGatewayCfgNode.getIpsecProposals().put(vpnId, ipsecProposal);
      ipsecPolicy.getProposals().put(vpnId, ipsecProposal);
      IkeGateway ikeGateway = new IkeGateway(vpnId);
      vpnGatewayCfgNode.getIkeGateways().put(vpnId, ikeGateway);
      ipsecVpn.setIkeGateway(ikeGateway);
      IkePolicy ikePolicy = new IkePolicy(vpnId);
      vpnGatewayCfgNode.getIkePolicies().put(vpnId, ikePolicy);
      ikeGateway.setIkePolicy(ikePolicy);
      IkeProposal ikeProposal = new IkeProposal(vpnId, -1);
      vpnGatewayCfgNode.getIkeProposals().put(vpnId, ikeProposal);
      ikePolicy.getProposals().put(vpnId, ikeProposal);
      String externalInterfaceName = "external" + idNum;
      Interface externalInterface = new Interface(externalInterfaceName, vpnGatewayCfgNode);
      vpnGatewayCfgNode.getInterfaces().put(externalInterfaceName, externalInterface);
      vpnGatewayCfgNode
          .getDefaultVrf()
          .getInterfaces()
          .put(externalInterfaceName, externalInterface);
      String vpnInterfaceName = "vpn" + idNum;
      Interface vpnInterface = new Interface(vpnInterfaceName, vpnGatewayCfgNode);
      vpnGatewayCfgNode.getInterfaces().put(vpnInterfaceName, vpnInterface);
      vpnGatewayCfgNode.getDefaultVrf().getInterfaces().put(vpnInterfaceName, vpnInterface);

      // Set fields within representation structures

      // bind and vpn interfaces
      Prefix externalInterfacePrefix = new Prefix(ipsecTunnel.getVgwOutsideAddress(), 32);
      externalInterface.setPrefix(externalInterfacePrefix);
      externalInterface.getAllPrefixes().add(externalInterfacePrefix);
      Prefix vpnInterfacePrefix =
          new Prefix(ipsecTunnel.getVgwInsideAddress(), ipsecTunnel.getVgwInsidePrefixLength());
      vpnInterface.setPrefix(vpnInterfacePrefix);
      vpnInterface.getAllPrefixes().add(vpnInterfacePrefix);

      // ipsec
      ipsecVpn.setBindInterface(vpnInterface);
      ipsecPolicy.setPfsKeyGroup(toDiffieHellmanGroup(ipsecTunnel.getIpsecPerfectForwardSecrecy()));
      ipsecProposal.setAuthenticationAlgorithm(
          toIpsecAuthenticationAlgorithm(ipsecTunnel.getIpsecAuthProtocol()));
      ipsecProposal.setEncryptionAlgorithm(
          toEncryptionAlgorithm(ipsecTunnel.getIpsecEncryptionProtocol()));
      ipsecProposal.setProtocol(toIpsecProtocol(ipsecTunnel.getIpsecProtocol()));
      ipsecProposal.setLifetimeSeconds(ipsecTunnel.getIpsecLifetime());

      // ike
      ikeGateway.setExternalInterface(externalInterface);
      ikeGateway.setAddress(ipsecTunnel.getCgwOutsideAddress());
      if (ipsecTunnel.getIkePreSharedKeyHash() != null) {
        ikePolicy.setPreSharedKeyHash(ipsecTunnel.getIkePreSharedKeyHash());
        ikeProposal.setAuthenticationMethod(IkeAuthenticationMethod.PRE_SHARED_KEYS);
      }
      ikeProposal.setAuthenticationAlgorithm(
          toIkeAuthenticationAlgorithm(ipsecTunnel.getIkeAuthProtocol()));
      ikeProposal.setDiffieHellmanGroup(
          toDiffieHellmanGroup(ipsecTunnel.getIkePerfectForwardSecrecy()));
      ikeProposal.setEncryptionAlgorithm(
          toEncryptionAlgorithm(ipsecTunnel.getIkeEncryptionProtocol()));
      ikeProposal.setLifetimeSeconds(ipsecTunnel.getIkeLifetime());

      // bgp (if configured)
      if (ipsecTunnel.getVgwBgpAsn() != -1) {
        BgpProcess proc = vpnGatewayCfgNode.getDefaultVrf().getBgpProcess();
        if (proc == null) {
          proc = new BgpProcess();
          vpnGatewayCfgNode.getDefaultVrf().setBgpProcess(proc);
        }
        BgpNeighbor cgBgpNeighbor =
            new BgpNeighbor(ipsecTunnel.getCgwInsideAddress(), vpnGatewayCfgNode);
        cgBgpNeighbor.setVrf(Configuration.DEFAULT_VRF_NAME);
        proc.getNeighbors().put(cgBgpNeighbor.getPrefix(), cgBgpNeighbor);
        cgBgpNeighbor.setRemoteAs(ipsecTunnel.getCgwBgpAsn());
        cgBgpNeighbor.setLocalAs(ipsecTunnel.getVgwBgpAsn());
        cgBgpNeighbor.setLocalIp(ipsecTunnel.getVgwInsideAddress());
        cgBgpNeighbor.setDefaultMetric(BGP_NEIGHBOR_DEFAULT_METRIC);
        cgBgpNeighbor.setSendCommunity(false);
        VpnGateway vpnGateway = awsVpcConfiguration.getVpnGateways().get(_vpnGatewayId);
        List<String> attachmentVpcIds = vpnGateway.getAttachmentVpcIds();
        if (attachmentVpcIds.size() != 1) {
          throw new BatfishException(
              "Not sure what routes to advertise since VPN Gateway: \""
                  + _vpnGatewayId
                  + "\" for VPN connection: \""
                  + _vpnConnectionId
                  + "\" is linked to multiple VPCs");
        }
        String vpcId = attachmentVpcIds.get(0);
        Vpc vpc = awsVpcConfiguration.getVpcs().get(vpcId);
        Prefix outgoingPrefix = vpc.getCidrBlock();
        int outgoingPrefixLength = outgoingPrefix.getPrefixLength();
        String originationPolicyName = vpnId + "_origination";
        RoutingPolicy originationRoutingPolicy =
            new RoutingPolicy(originationPolicyName, vpnGatewayCfgNode);
        vpnGatewayCfgNode.getRoutingPolicies().put(originationPolicyName, originationRoutingPolicy);
        cgBgpNeighbor.setExportPolicy(originationPolicyName);
        If originationIf = new If();
        List<Statement> statements = originationRoutingPolicy.getStatements();
        statements.add(originationIf);
        statements.add(Statements.ExitReject.toStaticStatement());
        originationIf.getTrueStatements().add(Statements.ExitAccept.toStaticStatement());
        RouteFilterList originationRouteFilter = new RouteFilterList(originationPolicyName);
        vpnGatewayCfgNode.getRouteFilterLists().put(originationPolicyName, originationRouteFilter);
        RouteFilterLine matchOutgoingPrefix =
            new RouteFilterLine(
                LineAction.ACCEPT,
                outgoingPrefix,
                new SubRange(outgoingPrefixLength, outgoingPrefixLength));
        originationRouteFilter.addLine(matchOutgoingPrefix);
        Conjunction conj = new Conjunction();
        originationIf.setGuard(conj);
        conj.getConjuncts().add(new MatchProtocol(RoutingProtocol.STATIC));
        conj.getConjuncts()
            .add(
                new MatchPrefixSet(
                    new DestinationNetwork(), new NamedPrefixSet(originationPolicyName)));
      }

      // static routes (if configured)
      for (Prefix staticRoutePrefix : _routes) {
        StaticRoute staticRoute =
            StaticRoute.builder()
                .setNetwork(staticRoutePrefix)
                .setNextHopIp(ipsecTunnel.getCgwInsideAddress())
                .setAdministrativeCost(Route.DEFAULT_STATIC_ROUTE_ADMIN)
                .setTag(Route.DEFAULT_STATIC_ROUTE_COST)
                .build();

        vpnGatewayCfgNode.getDefaultVrf().getStaticRoutes().add(staticRoute);
      }
    }
  }

  public String getCustomerGatewayId() {
    return _customerGatewayId;
  }

  @Override
  public String getId() {
    return _vpnConnectionId;
  }

  public List<IpsecTunnel> getIpsecTunnels() {
    return _ipsecTunnels;
  }

  public List<Prefix> getRoutes() {
    return _routes;
  }

  public boolean getStaticRoutesOnly() {
    return _staticRoutesOnly;
  }

  public List<VgwTelemetry> getVgwTelemetrys() {
    return _vgwTelemetrys;
  }

  public String getVpnConnectionId() {
    return _vpnConnectionId;
  }

  public String getVpnGatewayId() {
    return _vpnGatewayId;
  }
}

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
import org.batfish.representation.Prefix;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class VpnConnection implements AwsVpcEntity, Serializable {

   private static final long serialVersionUID = 1L;

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
      
      String cgwConfiguration = jObj.getString(JSON_KEY_CUSTOMER_GATEWAY_CONFIGURATION);
      Document document;
      try {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         DocumentBuilder builder = factory.newDocumentBuilder();
         InputSource is = new InputSource(new StringReader(cgwConfiguration));
         document = builder.parse(is);
      }
      catch (ParserConfigurationException | SAXException | IOException e) {
         throw new BatfishException("Could not parse XML for CustomerGatewayConfiguration for vpn connection " 
               + _vpnConnectionId + " " + e);
      }
      
      NodeList nodeList = document.getElementsByTagName(XML_KEY_IPSEC_TUNNEL);
      
      for (int index = 0; index < nodeList.getLength(); index++) {
         Element element = (Element) nodeList.item(index);
         _ipsecTunnels.add(new IpsecTunnel(element));
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

   @Override
   public String getId() {
      return _vpnConnectionId;
   }
}

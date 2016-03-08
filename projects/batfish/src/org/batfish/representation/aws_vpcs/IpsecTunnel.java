package org.batfish.representation.aws_vpcs;

import java.io.Serializable;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.batfish.representation.Ip;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public class IpsecTunnel implements Serializable {

   private static final long serialVersionUID = 1L;

   private String _vpnConnectionAttributes;
   
   private Ip _cgwOutsideAddress;
   
   private Ip _cgwInsideAddress;
   
   private int _cgwInsidePrefixLength;
   
   private int _cgwBgpAsn=-1;
   
   private Ip _vgwOutsideAddress;
   
   private Ip _vgwInsideAddress;
   
   private int _vgwInsidePrefixLength;
   
   private int _vgwBgpAsn=-1;
   
   private String _ikeAuthProtocol;
   
   private String _ikeEncryptionProtocol;
   
   private int _ikeLifetime;
   
   private String _ikePerfectForwardSecrecy;
   
   private String _ikeMode;
   
   private String _ikePreSharedKey;
   
   private String _ipsecProtocol;
   
   private String _ipsecAuthProtocol;
   
   private String _ipsecEncryptionProtocol;
   
   private int _ipsecLifetime;
   
   private String _ipsecPerfectForwardSecrecy;
   
   private String _ipsecMode;
   
   public IpsecTunnel(Element element) {
      
      //this is an optional field
      if (element.getElementsByTagName(AwsVpcEntity.XML_KEY_VPN_CONNECTION_ATTRIBUTES).getLength() > 0)
         _vpnConnectionAttributes = getText(element, AwsVpcEntity.XML_KEY_VPN_CONNECTION_ATTRIBUTES);
            
      Element cgwElement = (Element) element.getElementsByTagName(AwsVpcEntity.XML_KEY_CUSTOMER_GATEWAY).item(0);
      
      _cgwOutsideAddress = new Ip(getText(cgwElement, 
            AwsVpcEntity.XML_KEY_TUNNEL_OUTSIDE_ADDRESS, 
            AwsVpcEntity.XML_KEY_IP_ADDRESS));
      
      _cgwInsideAddress = new Ip(getText(cgwElement, 
            AwsVpcEntity.XML_KEY_TUNNEL_INSIDE_ADDRESS, 
            AwsVpcEntity.XML_KEY_IP_ADDRESS));
      
      _cgwInsidePrefixLength = Integer.parseInt(getText(cgwElement, 
            AwsVpcEntity.XML_KEY_TUNNEL_INSIDE_ADDRESS, 
            AwsVpcEntity.XML_KEY_NETWORK_CIDR));
      
      // when vpnconnection attribute is 'NoBGPVPNConnection' we see no asn configured
      if (_vpnConnectionAttributes != null && !_vpnConnectionAttributes.contains("NoBGP")) 
         _cgwBgpAsn = Integer.parseInt(getText(cgwElement, 
               AwsVpcEntity.XML_KEY_BGP, AwsVpcEntity.XML_KEY_ASN));
      
      Element vgwElement = (Element) element.getElementsByTagName(AwsVpcEntity.XML_KEY_VPN_GATEWAY).item(0);
      
      _vgwOutsideAddress = new Ip(getText(vgwElement, 
            AwsVpcEntity.XML_KEY_TUNNEL_OUTSIDE_ADDRESS, 
            AwsVpcEntity.XML_KEY_IP_ADDRESS));
      
      _vgwInsideAddress = new Ip(getText(vgwElement, 
            AwsVpcEntity.XML_KEY_TUNNEL_INSIDE_ADDRESS, 
            AwsVpcEntity.XML_KEY_IP_ADDRESS));
      
      _vgwInsidePrefixLength = Integer.parseInt(getText(vgwElement, 
            AwsVpcEntity.XML_KEY_TUNNEL_INSIDE_ADDRESS, 
            AwsVpcEntity.XML_KEY_NETWORK_CIDR));
      
      // when vpnconnection attribute is 'NoBGPVPNConnection' we see no asn configured
      if (_vpnConnectionAttributes != null && !_vpnConnectionAttributes.contains("NoBGP")) 
         _vgwBgpAsn = Integer.parseInt(getText(vgwElement, 
               AwsVpcEntity.XML_KEY_BGP, AwsVpcEntity.XML_KEY_ASN));
      
      Element ikeElement = (Element) element.getElementsByTagName(AwsVpcEntity.XML_KEY_IKE).item(0);
      
      _ikeAuthProtocol = getText(ikeElement, AwsVpcEntity.XML_KEY_AUTHENTICATION_PROTOCOL);
      _ikeEncryptionProtocol = getText(ikeElement, AwsVpcEntity.XML_KEY_ENCRYPTION_PROTOCOL);
      _ikeLifetime = Integer.parseInt(getText(ikeElement, AwsVpcEntity.XML_KEY_LIFETIME));
      _ikePerfectForwardSecrecy = getText(ikeElement, AwsVpcEntity.XML_KEY_PERFECT_FORWARD_SECRECY);
      _ikeMode = getText(ikeElement, AwsVpcEntity.XML_KEY_MODE);
      _ikePreSharedKey = getText(ikeElement, AwsVpcEntity.XML_KEY_PRE_SHARED_KEY);

      Element ipsecElement = (Element) element.getElementsByTagName(AwsVpcEntity.XML_KEY_IPSEC).item(0);
      
      _ipsecProtocol = getText(ipsecElement, AwsVpcEntity.XML_KEY_PROTOCOL);
      _ipsecAuthProtocol = getText(ipsecElement, AwsVpcEntity.XML_KEY_AUTHENTICATION_PROTOCOL);
      _ipsecEncryptionProtocol = getText(ipsecElement, AwsVpcEntity.XML_KEY_ENCRYPTION_PROTOCOL);
      _ipsecLifetime = Integer.parseInt(getText(ipsecElement, AwsVpcEntity.XML_KEY_LIFETIME));
      _ipsecPerfectForwardSecrecy = getText(ipsecElement, AwsVpcEntity.XML_KEY_PERFECT_FORWARD_SECRECY);
      _ipsecMode = getText(ipsecElement, AwsVpcEntity.XML_KEY_MODE);
      
   }   
   
   private static String getText(Element element, String outerTag, String innerTag) {
      return getText((Element) element.getElementsByTagName(outerTag).item(0), innerTag);
   }
   
   private static String getText(Element element, String tag) {
      return element.getElementsByTagName(tag).item(0).getTextContent();
   }
}

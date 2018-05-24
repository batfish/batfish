package org.batfish.representation.aws;

import java.io.Serializable;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.Ip;
import org.w3c.dom.Element;

public class IpsecTunnel implements Serializable {

  private static final long serialVersionUID = 1L;

  private static String getText(Element element, String tag) {
    return element.getElementsByTagName(tag).item(0).getTextContent();
  }

  private static String getText(Element element, String outerTag, String innerTag) {
    return getText((Element) element.getElementsByTagName(outerTag).item(0), innerTag);
  }

  private long _cgwBgpAsn = -1L;

  private Ip _cgwInsideAddress;

  private int _cgwInsidePrefixLength;

  private Ip _cgwOutsideAddress;

  private String _ikeAuthProtocol;

  private String _ikeEncryptionProtocol;

  private int _ikeLifetime;

  private String _ikeMode;

  private String _ikePerfectForwardSecrecy;

  private String _ikePreSharedKeyHash;

  private String _ipsecAuthProtocol;

  private String _ipsecEncryptionProtocol;

  private int _ipsecLifetime;

  private String _ipsecMode;

  private String _ipsecPerfectForwardSecrecy;

  private String _ipsecProtocol;

  private long _vgwBgpAsn = -1L;

  private Ip _vgwInsideAddress;

  private int _vgwInsidePrefixLength;

  private Ip _vgwOutsideAddress;

  private String _vpnConnectionAttributes;

  public IpsecTunnel(Element ipsecTunnel, Element vpnConnection) {

    // this is an optional field
    if (vpnConnection
            .getElementsByTagName(AwsVpcEntity.XML_KEY_VPN_CONNECTION_ATTRIBUTES)
            .getLength()
        > 0) {
      _vpnConnectionAttributes =
          getText(vpnConnection, AwsVpcEntity.XML_KEY_VPN_CONNECTION_ATTRIBUTES);
    }
    Element cgwElement =
        (Element) ipsecTunnel.getElementsByTagName(AwsVpcEntity.XML_KEY_CUSTOMER_GATEWAY).item(0);

    _cgwOutsideAddress =
        new Ip(
            getText(
                cgwElement,
                AwsVpcEntity.XML_KEY_TUNNEL_OUTSIDE_ADDRESS,
                AwsVpcEntity.XML_KEY_IP_ADDRESS));

    _cgwInsideAddress =
        new Ip(
            getText(
                cgwElement,
                AwsVpcEntity.XML_KEY_TUNNEL_INSIDE_ADDRESS,
                AwsVpcEntity.XML_KEY_IP_ADDRESS));

    _cgwInsidePrefixLength =
        Integer.parseInt(
            getText(
                cgwElement,
                AwsVpcEntity.XML_KEY_TUNNEL_INSIDE_ADDRESS,
                AwsVpcEntity.XML_KEY_NETWORK_CIDR));

    // when vpnconnection attribute is 'NoBGPVPNConnection' we see no asn
    // configured
    if (_vpnConnectionAttributes == null || !_vpnConnectionAttributes.contains("NoBGP")) {
      _cgwBgpAsn =
          Integer.parseInt(getText(cgwElement, AwsVpcEntity.XML_KEY_BGP, AwsVpcEntity.XML_KEY_ASN));
    }
    Element vgwElement =
        (Element) ipsecTunnel.getElementsByTagName(AwsVpcEntity.XML_KEY_VPN_GATEWAY).item(0);

    _vgwOutsideAddress =
        new Ip(
            getText(
                vgwElement,
                AwsVpcEntity.XML_KEY_TUNNEL_OUTSIDE_ADDRESS,
                AwsVpcEntity.XML_KEY_IP_ADDRESS));

    _vgwInsideAddress =
        new Ip(
            getText(
                vgwElement,
                AwsVpcEntity.XML_KEY_TUNNEL_INSIDE_ADDRESS,
                AwsVpcEntity.XML_KEY_IP_ADDRESS));

    _vgwInsidePrefixLength =
        Integer.parseInt(
            getText(
                vgwElement,
                AwsVpcEntity.XML_KEY_TUNNEL_INSIDE_ADDRESS,
                AwsVpcEntity.XML_KEY_NETWORK_CIDR));

    // when vpnconnection attribute is 'NoBGPVPNConnection' we see no asn
    // configured
    if (_vpnConnectionAttributes == null || !_vpnConnectionAttributes.contains("NoBGP")) {
      _vgwBgpAsn =
          Integer.parseInt(getText(vgwElement, AwsVpcEntity.XML_KEY_BGP, AwsVpcEntity.XML_KEY_ASN));
    }
    Element ikeElement =
        (Element) ipsecTunnel.getElementsByTagName(AwsVpcEntity.XML_KEY_IKE).item(0);

    _ikeAuthProtocol = getText(ikeElement, AwsVpcEntity.XML_KEY_AUTHENTICATION_PROTOCOL);
    _ikeEncryptionProtocol = getText(ikeElement, AwsVpcEntity.XML_KEY_ENCRYPTION_PROTOCOL);
    _ikeLifetime = Integer.parseInt(getText(ikeElement, AwsVpcEntity.XML_KEY_LIFETIME));
    _ikePerfectForwardSecrecy = getText(ikeElement, AwsVpcEntity.XML_KEY_PERFECT_FORWARD_SECRECY);
    _ikeMode = getText(ikeElement, AwsVpcEntity.XML_KEY_MODE);
    _ikePreSharedKeyHash =
        CommonUtil.sha256Digest(
            getText(ikeElement, AwsVpcEntity.XML_KEY_PRE_SHARED_KEY) + CommonUtil.salt());

    Element ipsecElement =
        (Element) ipsecTunnel.getElementsByTagName(AwsVpcEntity.XML_KEY_IPSEC).item(0);

    _ipsecProtocol = getText(ipsecElement, AwsVpcEntity.XML_KEY_PROTOCOL);
    _ipsecAuthProtocol = getText(ipsecElement, AwsVpcEntity.XML_KEY_AUTHENTICATION_PROTOCOL);
    _ipsecEncryptionProtocol = getText(ipsecElement, AwsVpcEntity.XML_KEY_ENCRYPTION_PROTOCOL);
    _ipsecLifetime = Integer.parseInt(getText(ipsecElement, AwsVpcEntity.XML_KEY_LIFETIME));
    _ipsecPerfectForwardSecrecy =
        getText(ipsecElement, AwsVpcEntity.XML_KEY_PERFECT_FORWARD_SECRECY);
    _ipsecMode = getText(ipsecElement, AwsVpcEntity.XML_KEY_MODE);
  }

  public long getCgwBgpAsn() {
    return _cgwBgpAsn;
  }

  public Ip getCgwInsideAddress() {
    return _cgwInsideAddress;
  }

  public int getCgwInsidePrefixLength() {
    return _cgwInsidePrefixLength;
  }

  public Ip getCgwOutsideAddress() {
    return _cgwOutsideAddress;
  }

  public String getIkeAuthProtocol() {
    return _ikeAuthProtocol;
  }

  public String getIkeEncryptionProtocol() {
    return _ikeEncryptionProtocol;
  }

  public int getIkeLifetime() {
    return _ikeLifetime;
  }

  public String getIkeMode() {
    return _ikeMode;
  }

  public String getIkePerfectForwardSecrecy() {
    return _ikePerfectForwardSecrecy;
  }

  public String getIkePreSharedKeyHash() {
    return _ikePreSharedKeyHash;
  }

  public String getIpsecAuthProtocol() {
    return _ipsecAuthProtocol;
  }

  public String getIpsecEncryptionProtocol() {
    return _ipsecEncryptionProtocol;
  }

  public int getIpsecLifetime() {
    return _ipsecLifetime;
  }

  public String getIpsecMode() {
    return _ipsecMode;
  }

  public String getIpsecPerfectForwardSecrecy() {
    return _ipsecPerfectForwardSecrecy;
  }

  public String getIpsecProtocol() {
    return _ipsecProtocol;
  }

  public long getVgwBgpAsn() {
    return _vgwBgpAsn;
  }

  public Ip getVgwInsideAddress() {
    return _vgwInsideAddress;
  }

  public int getVgwInsidePrefixLength() {
    return _vgwInsidePrefixLength;
  }

  public Ip getVgwOutsideAddress() {
    return _vgwOutsideAddress;
  }

  public String getVpnConnectionAttributes() {
    return _vpnConnectionAttributes;
  }
}

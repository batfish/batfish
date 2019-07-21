package org.batfish.representation.aws;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.Ip;
import org.w3c.dom.Element;

/** Represents an AWs IPSec tunnel */
@ParametersAreNonnullByDefault
public class IpsecTunnel implements Serializable {

  private static String getText(Element element, String tag) {
    return element.getElementsByTagName(tag).item(0).getTextContent();
  }

  private static String getText(Element element, String outerTag, String innerTag) {
    return getText((Element) element.getElementsByTagName(outerTag).item(0), innerTag);
  }

  @Nullable private Long _cgwBgpAsn;

  @Nonnull private final Ip _cgwInsideAddress;

  private final int _cgwInsidePrefixLength;

  @Nonnull private final Ip _cgwOutsideAddress;

  @Nonnull private final String _ikeAuthProtocol;

  @Nonnull private final String _ikeEncryptionProtocol;

  private final int _ikeLifetime;

  @Nonnull private final String _ikeMode;

  @Nonnull private final String _ikePerfectForwardSecrecy;

  @Nonnull private final String _ikePreSharedKeyHash;

  @Nonnull private final String _ipsecAuthProtocol;

  @Nonnull private final String _ipsecEncryptionProtocol;

  private final int _ipsecLifetime;

  @Nonnull private final String _ipsecMode;

  @Nonnull private final String _ipsecPerfectForwardSecrecy;

  @Nonnull private final String _ipsecProtocol;

  @Nullable private final Long _vgwBgpAsn;

  @Nonnull private final Ip _vgwInsideAddress;

  private final int _vgwInsidePrefixLength;

  @Nonnull private final Ip _vgwOutsideAddress;

  @Nullable private final String _vpnConnectionAttributes;

  static IpsecTunnel create(Element ipsecTunnel, Element vpnConnection) {

    Builder builder = new Builder();

    // this is an optional field
    if (vpnConnection
            .getElementsByTagName(AwsVpcEntity.XML_KEY_VPN_CONNECTION_ATTRIBUTES)
            .getLength()
        > 0) {
      builder.setVpnConnectionAttributes(
          getText(vpnConnection, AwsVpcEntity.XML_KEY_VPN_CONNECTION_ATTRIBUTES));
    }
    Element cgwElement =
        (Element) ipsecTunnel.getElementsByTagName(AwsVpcEntity.XML_KEY_CUSTOMER_GATEWAY).item(0);

    builder.setcgwOutsideAddress(
        Ip.parse(
            getText(
                cgwElement,
                AwsVpcEntity.XML_KEY_TUNNEL_OUTSIDE_ADDRESS,
                AwsVpcEntity.XML_KEY_IP_ADDRESS)));

    builder.setcgwInsideAddress(
        Ip.parse(
            getText(
                cgwElement,
                AwsVpcEntity.XML_KEY_TUNNEL_INSIDE_ADDRESS,
                AwsVpcEntity.XML_KEY_IP_ADDRESS)));

    builder.setCgwInsidePrefixLength(
        Integer.parseInt(
            getText(
                cgwElement,
                AwsVpcEntity.XML_KEY_TUNNEL_INSIDE_ADDRESS,
                AwsVpcEntity.XML_KEY_NETWORK_CIDR)));

    // when vpnconnection attribute is 'NoBGPVPNConnection' we see no asn configured
    if (builder._vpnConnectionAttributes == null
        || !builder._vpnConnectionAttributes.contains("NoBGP")) {
      builder.setCgwBgpAsn(
          Long.parseLong(getText(cgwElement, AwsVpcEntity.XML_KEY_BGP, AwsVpcEntity.XML_KEY_ASN)));
    }
    Element vgwElement =
        (Element) ipsecTunnel.getElementsByTagName(AwsVpcEntity.XML_KEY_VPN_GATEWAY).item(0);

    builder.setVgwOutsideAddress(
        Ip.parse(
            getText(
                vgwElement,
                AwsVpcEntity.XML_KEY_TUNNEL_OUTSIDE_ADDRESS,
                AwsVpcEntity.XML_KEY_IP_ADDRESS)));

    builder.setVgwInsideAddress(
        Ip.parse(
            getText(
                vgwElement,
                AwsVpcEntity.XML_KEY_TUNNEL_INSIDE_ADDRESS,
                AwsVpcEntity.XML_KEY_IP_ADDRESS)));

    builder.setVgwInsidePrefixLength(
        Integer.parseInt(
            getText(
                vgwElement,
                AwsVpcEntity.XML_KEY_TUNNEL_INSIDE_ADDRESS,
                AwsVpcEntity.XML_KEY_NETWORK_CIDR)));

    // when vpnconnection attribute is 'NoBGPVPNConnection' we see no asn configured
    if (builder._vpnConnectionAttributes == null
        || !builder._vpnConnectionAttributes.contains("NoBGP")) {
      builder.setVgwBgpAsn(
          Long.parseLong(getText(vgwElement, AwsVpcEntity.XML_KEY_BGP, AwsVpcEntity.XML_KEY_ASN)));
    }
    Element ikeElement =
        (Element) ipsecTunnel.getElementsByTagName(AwsVpcEntity.XML_KEY_IKE).item(0);

    builder.setIkeAuthProtocol(getText(ikeElement, AwsVpcEntity.XML_KEY_AUTHENTICATION_PROTOCOL));
    builder.setIkeEncryptionProtocol(getText(ikeElement, AwsVpcEntity.XML_KEY_ENCRYPTION_PROTOCOL));
    builder.setIkeLifetime(Integer.parseInt(getText(ikeElement, AwsVpcEntity.XML_KEY_LIFETIME)));
    builder.setIkePerfectForwardSecrecy(
        getText(ikeElement, AwsVpcEntity.XML_KEY_PERFECT_FORWARD_SECRECY));
    builder.setIkeMode(getText(ikeElement, AwsVpcEntity.XML_KEY_MODE));
    builder.setIkePreSharedKeyHash(
        CommonUtil.sha256Digest(
            getText(ikeElement, AwsVpcEntity.XML_KEY_PRE_SHARED_KEY) + CommonUtil.salt()));

    Element ipsecElement =
        (Element) ipsecTunnel.getElementsByTagName(AwsVpcEntity.XML_KEY_IPSEC).item(0);

    builder.setIpsecProtocol(getText(ipsecElement, AwsVpcEntity.XML_KEY_PROTOCOL));
    builder.setIpsecAuthProtocol(
        getText(ipsecElement, AwsVpcEntity.XML_KEY_AUTHENTICATION_PROTOCOL));
    builder.setIpsecEncryptionProtocol(
        getText(ipsecElement, AwsVpcEntity.XML_KEY_ENCRYPTION_PROTOCOL));
    builder.setIpsecLifetime(
        Integer.parseInt(getText(ipsecElement, AwsVpcEntity.XML_KEY_LIFETIME)));
    builder.setIpsecPerfectForwardSecrecy(
        getText(ipsecElement, AwsVpcEntity.XML_KEY_PERFECT_FORWARD_SECRECY));
    builder.setIpsecMode(getText(ipsecElement, AwsVpcEntity.XML_KEY_MODE));

    return builder.build();
  }

  public IpsecTunnel(
      @Nullable Long cgwBgpAsn,
      Ip cgwInsideAddress,
      int cgwInsidePrefixLength,
      Ip cgwOutsideAddress,
      String ikeAuthProtocol,
      String ikeEncryptionProtocol,
      int ikeLifetime,
      String ikeMode,
      String ikePerfectForwardSecrecy,
      String ikePreSharedKeyHash,
      String ipsecAuthProtocol,
      String ipsecEncryptionProtocol,
      int ipsecLifetime,
      String ipsecMode,
      String ipsecPerfectForwardSecrecy,
      String ipsecProtocol,
      @Nullable Long vgwBgpAsn,
      Ip vgwInsideAddress,
      int vgwInsidePrefixLength,
      Ip vgwOutsideAddress,
      String vpnConnectionAttributes) {
    _cgwBgpAsn = cgwBgpAsn;
    _cgwInsideAddress = cgwInsideAddress;
    _cgwInsidePrefixLength = cgwInsidePrefixLength;
    _cgwOutsideAddress = cgwOutsideAddress;

    _ikeAuthProtocol = ikeAuthProtocol;
    _ikeEncryptionProtocol = ikeEncryptionProtocol;
    _ikeLifetime = ikeLifetime;
    _ikeMode = ikeMode;
    _ikePerfectForwardSecrecy = ikePerfectForwardSecrecy;
    _ikePreSharedKeyHash = ikePreSharedKeyHash;

    _ipsecAuthProtocol = ipsecAuthProtocol;
    _ipsecEncryptionProtocol = ipsecEncryptionProtocol;
    _ipsecLifetime = ipsecLifetime;
    _ipsecMode = ipsecMode;
    _ipsecPerfectForwardSecrecy = ipsecPerfectForwardSecrecy;
    _ipsecProtocol = ipsecProtocol;

    _vgwBgpAsn = vgwBgpAsn;
    _vgwInsidePrefixLength = vgwInsidePrefixLength;
    _vgwInsideAddress = vgwInsideAddress;
    _vgwOutsideAddress = vgwOutsideAddress;

    _vpnConnectionAttributes = vpnConnectionAttributes;
  }

  @Nullable
  public Long getCgwBgpAsn() {
    return _cgwBgpAsn;
  }

  @Nonnull
  public Ip getCgwInsideAddress() {
    return _cgwInsideAddress;
  }

  public int getCgwInsidePrefixLength() {
    return _cgwInsidePrefixLength;
  }

  @Nonnull
  public Ip getCgwOutsideAddress() {
    return _cgwOutsideAddress;
  }

  @Nonnull
  public String getIkeAuthProtocol() {
    return _ikeAuthProtocol;
  }

  @Nonnull
  public String getIkeEncryptionProtocol() {
    return _ikeEncryptionProtocol;
  }

  public int getIkeLifetime() {
    return _ikeLifetime;
  }

  @Nonnull
  public String getIkeMode() {
    return _ikeMode;
  }

  @Nonnull
  public String getIkePerfectForwardSecrecy() {
    return _ikePerfectForwardSecrecy;
  }

  @Nonnull
  public String getIkePreSharedKeyHash() {
    return _ikePreSharedKeyHash;
  }

  @Nonnull
  public String getIpsecAuthProtocol() {
    return _ipsecAuthProtocol;
  }

  @Nonnull
  public String getIpsecEncryptionProtocol() {
    return _ipsecEncryptionProtocol;
  }

  public int getIpsecLifetime() {
    return _ipsecLifetime;
  }

  @Nonnull
  public String getIpsecMode() {
    return _ipsecMode;
  }

  @Nonnull
  public String getIpsecPerfectForwardSecrecy() {
    return _ipsecPerfectForwardSecrecy;
  }

  @Nonnull
  public String getIpsecProtocol() {
    return _ipsecProtocol;
  }

  @Nullable
  public Long getVgwBgpAsn() {
    return _vgwBgpAsn;
  }

  @Nonnull
  public Ip getVgwInsideAddress() {
    return _vgwInsideAddress;
  }

  public int getVgwInsidePrefixLength() {
    return _vgwInsidePrefixLength;
  }

  @Nonnull
  public Ip getVgwOutsideAddress() {
    return _vgwOutsideAddress;
  }

  @Nullable
  public String getVpnConnectionAttributes() {
    return _vpnConnectionAttributes;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    IpsecTunnel that = (IpsecTunnel) o;
    return Objects.equal(_cgwBgpAsn, that._cgwBgpAsn)
        && _cgwInsidePrefixLength == that._cgwInsidePrefixLength
        && _ikeLifetime == that._ikeLifetime
        && _ipsecLifetime == that._ipsecLifetime
        && Objects.equal(_vgwBgpAsn, that._vgwBgpAsn)
        && _vgwInsidePrefixLength == that._vgwInsidePrefixLength
        && Objects.equal(_cgwInsideAddress, that._cgwInsideAddress)
        && Objects.equal(_cgwOutsideAddress, that._cgwOutsideAddress)
        && Objects.equal(_ikeAuthProtocol, that._ikeAuthProtocol)
        && Objects.equal(_ikeEncryptionProtocol, that._ikeEncryptionProtocol)
        && Objects.equal(_ikeMode, that._ikeMode)
        && Objects.equal(_ikePerfectForwardSecrecy, that._ikePerfectForwardSecrecy)
        && Objects.equal(_ikePreSharedKeyHash, that._ikePreSharedKeyHash)
        && Objects.equal(_ipsecAuthProtocol, that._ipsecAuthProtocol)
        && Objects.equal(_ipsecEncryptionProtocol, that._ipsecEncryptionProtocol)
        && Objects.equal(_ipsecMode, that._ipsecMode)
        && Objects.equal(_ipsecPerfectForwardSecrecy, that._ipsecPerfectForwardSecrecy)
        && Objects.equal(_ipsecProtocol, that._ipsecProtocol)
        && Objects.equal(_vgwInsideAddress, that._vgwInsideAddress)
        && Objects.equal(_vgwOutsideAddress, that._vgwOutsideAddress)
        && Objects.equal(_vpnConnectionAttributes, that._vpnConnectionAttributes);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        _cgwBgpAsn,
        _cgwInsideAddress,
        _cgwInsidePrefixLength,
        _cgwOutsideAddress,
        _ikeAuthProtocol,
        _ikeEncryptionProtocol,
        _ikeLifetime,
        _ikeMode,
        _ikePerfectForwardSecrecy,
        _ikePreSharedKeyHash,
        _ipsecAuthProtocol,
        _ipsecEncryptionProtocol,
        _ipsecLifetime,
        _ipsecMode,
        _ipsecPerfectForwardSecrecy,
        _ipsecProtocol,
        _vgwBgpAsn,
        _vgwInsideAddress,
        _vgwInsidePrefixLength,
        _vgwOutsideAddress,
        _vpnConnectionAttributes);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("_cgwBgpAsn", _cgwBgpAsn)
        .add("_cgwInsideAddress", _cgwInsideAddress)
        .add("_cgwInsidePrefixLength", _cgwInsidePrefixLength)
        .add("_cgwOutsideAddress", _cgwOutsideAddress)
        .add("_ikeAuthProtocol", _ikeAuthProtocol)
        .add("_ikeEncryptionProtocol", _ikeEncryptionProtocol)
        .add("_ikeLifetime", _ikeLifetime)
        .add("_ikeMode", _ikeMode)
        .add("_ikePerfectForwardSecrecy", _ikePerfectForwardSecrecy)
        .add("_ikePreSharedKeyHash", _ikePreSharedKeyHash)
        .add("_ipsecAuthProtocol", _ipsecAuthProtocol)
        .add("_ipsecEncryptionProtocol", _ipsecEncryptionProtocol)
        .add("_ipsecLifetime", _ipsecLifetime)
        .add("_ipsecMode", _ipsecMode)
        .add("_ipsecPerfectForwardSecrecy", _ipsecPerfectForwardSecrecy)
        .add("_ipsecProtocol", _ipsecProtocol)
        .add("_vgwBgpAsn", _vgwBgpAsn)
        .add("_vgwInsideAddress", _vgwInsideAddress)
        .add("_vgwInsidePrefixLength", _vgwInsidePrefixLength)
        .add("_vgwOutsideAddress", _vgwOutsideAddress)
        .add("_vpnConnectionAttributes", _vpnConnectionAttributes)
        .toString();
  }

  public static final class Builder {
    private Long _cgwBgpAsn;
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
    private Long _vgwBgpAsn;
    private Ip _vgwInsideAddress;
    private int _vgwInsidePrefixLength;
    private Ip _vgwOutsideAddress;
    private String _vpnConnectionAttributes;

    private Builder() {}

    public Builder setCgwBgpAsn(@Nullable Long cgwBgpAsn) {
      this._cgwBgpAsn = cgwBgpAsn;
      return this;
    }

    public Builder setcgwInsideAddress(Ip cgwInsideAddress) {
      this._cgwInsideAddress = cgwInsideAddress;
      return this;
    }

    public Builder setCgwInsidePrefixLength(int cgwInsidePrefixLength) {
      this._cgwInsidePrefixLength = cgwInsidePrefixLength;
      return this;
    }

    public Builder setcgwOutsideAddress(Ip cgwOutsideAddress) {
      this._cgwOutsideAddress = cgwOutsideAddress;
      return this;
    }

    public Builder setIkeAuthProtocol(String ikeAuthProtocol) {
      this._ikeAuthProtocol = ikeAuthProtocol;
      return this;
    }

    public Builder setIkeEncryptionProtocol(String ikeEncryptionProtocol) {
      this._ikeEncryptionProtocol = ikeEncryptionProtocol;
      return this;
    }

    public Builder setIkeLifetime(int ikeLifetime) {
      this._ikeLifetime = ikeLifetime;
      return this;
    }

    public Builder setIkeMode(String ikeMode) {
      this._ikeMode = ikeMode;
      return this;
    }

    public Builder setIkePerfectForwardSecrecy(String ikePerfectForwardSecrecy) {
      this._ikePerfectForwardSecrecy = ikePerfectForwardSecrecy;
      return this;
    }

    public Builder setIkePreSharedKeyHash(String ikePreSharedKeyHash) {
      this._ikePreSharedKeyHash = ikePreSharedKeyHash;
      return this;
    }

    public Builder setIpsecAuthProtocol(String ipsecAuthProtocol) {
      this._ipsecAuthProtocol = ipsecAuthProtocol;
      return this;
    }

    public Builder setIpsecEncryptionProtocol(String ipsecEncryptionProtocol) {
      this._ipsecEncryptionProtocol = ipsecEncryptionProtocol;
      return this;
    }

    public Builder setIpsecLifetime(int ipsecLifetime) {
      this._ipsecLifetime = ipsecLifetime;
      return this;
    }

    public Builder setIpsecMode(String ipsecMode) {
      this._ipsecMode = ipsecMode;
      return this;
    }

    public Builder setIpsecPerfectForwardSecrecy(String ipsecPerfectForwardSecrecy) {
      this._ipsecPerfectForwardSecrecy = ipsecPerfectForwardSecrecy;
      return this;
    }

    public Builder setIpsecProtocol(String ipsecProtocol) {
      this._ipsecProtocol = ipsecProtocol;
      return this;
    }

    public Builder setVgwBgpAsn(@Nullable Long vgwBgpAsn) {
      this._vgwBgpAsn = vgwBgpAsn;
      return this;
    }

    public Builder setVgwInsideAddress(Ip vgwInsideAddress) {
      this._vgwInsideAddress = vgwInsideAddress;
      return this;
    }

    public Builder setVgwInsidePrefixLength(int vgwInsidePrefixLength) {
      this._vgwInsidePrefixLength = vgwInsidePrefixLength;
      return this;
    }

    public Builder setVgwOutsideAddress(Ip vgwOutsideAddress) {
      this._vgwOutsideAddress = vgwOutsideAddress;
      return this;
    }

    public Builder setVpnConnectionAttributes(@Nullable String vpnConnectionAttributes) {
      this._vpnConnectionAttributes = vpnConnectionAttributes;
      return this;
    }

    public IpsecTunnel build() {
      return new IpsecTunnel(
          _cgwBgpAsn,
          _cgwInsideAddress,
          _cgwInsidePrefixLength,
          _cgwOutsideAddress,
          _ikeAuthProtocol,
          _ikeEncryptionProtocol,
          _ikeLifetime,
          _ikeMode,
          _ikePerfectForwardSecrecy,
          _ikePreSharedKeyHash,
          _ipsecAuthProtocol,
          _ipsecEncryptionProtocol,
          _ipsecLifetime,
          _ipsecMode,
          _ipsecPerfectForwardSecrecy,
          _ipsecProtocol,
          _vgwBgpAsn,
          _vgwInsideAddress,
          _vgwInsidePrefixLength,
          _vgwOutsideAddress,
          _vpnConnectionAttributes);
    }
  }
}

package org.batfish.vendor.cisco_aci.representation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Data model class representing an ACI Interface configuration.
 *
 * <p>Interfaces in ACI represent physical or logical ports on fabric nodes. They can be associated
 * with endpoint groups through path attachments (fvRsPathAtt) and have various policy bindings.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AciInterface implements Serializable {

  @JsonProperty("attributes")
  private @Nullable AciInterfaceAttributes _attributes;

  @JsonProperty("children")
  private @Nullable List<Object> _children;

  public @Nullable AciInterfaceAttributes getAttributes() {
    return _attributes;
  }

  public void setAttributes(@Nullable AciInterfaceAttributes attributes) {
    _attributes = attributes;
  }

  public @Nullable List<Object> getChildren() {
    return _children;
  }

  public void setChildren(@Nullable List<Object> children) {
    _children = children;
  }

  /** Attributes of an ACI Interface. */

  /** Attributes of an ACI Interface. */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class AciInterfaceAttributes implements Serializable {

    @JsonProperty("annotation")
    private @Nullable String _annotation;

    @JsonProperty("autoneg")
    private @Nullable String _autoNegotiation;

    @JsonProperty("descr")
    private @Nullable String _description;

    @JsonProperty("dn")
    private @Nullable String _distinguishedName;

    @JsonProperty("flowCtrl")
    private @Nullable String _flowControl;

    @JsonProperty("id")
    private @Nullable String _id;

    @JsonProperty("layer")
    private @Nullable String _layer;

    @JsonProperty("linkDebounce")
    private @Nullable String _linkDebounce;

    @JsonProperty("mdix")
    private @Nullable String _mdix;

    @JsonProperty("mode")
    private @Nullable String _mode;

    @JsonProperty("mtu")
    private @Nullable String _mtu;

    @JsonProperty("name")
    private @Nullable String _name;

    @JsonProperty("nameAlias")
    private @Nullable String _nameAlias;

    @JsonProperty("speed")
    private @Nullable String _speed;

    @JsonProperty("userdom")
    private @Nullable String _userDomain;

    public @Nullable String getAnnotation() {
      return _annotation;
    }

    public void setAnnotation(@Nullable String annotation) {
      _annotation = annotation;
    }

    public @Nullable String getAutoNegotiation() {
      return _autoNegotiation;
    }

    public void setAutoNegotiation(@Nullable String autoNegotiation) {
      _autoNegotiation = autoNegotiation;
    }

    public @Nullable String getDescription() {
      return _description;
    }

    public void setDescription(@Nullable String description) {
      _description = description;
    }

    public @Nullable String getDistinguishedName() {
      return _distinguishedName;
    }

    public void setDistinguishedName(@Nullable String distinguishedName) {
      _distinguishedName = distinguishedName;
    }

    public @Nullable String getFlowControl() {
      return _flowControl;
    }

    public void setFlowControl(@Nullable String flowControl) {
      _flowControl = flowControl;
    }

    public @Nullable String getId() {
      return _id;
    }

    public void setId(@Nullable String id) {
      _id = id;
    }

    public @Nullable String getLayer() {
      return _layer;
    }

    public void setLayer(@Nullable String layer) {
      _layer = layer;
    }

    public @Nullable String getLinkDebounce() {
      return _linkDebounce;
    }

    public void setLinkDebounce(@Nullable String linkDebounce) {
      _linkDebounce = linkDebounce;
    }

    public @Nullable String getMdix() {
      return _mdix;
    }

    public void setMdix(@Nullable String mdix) {
      _mdix = mdix;
    }

    public @Nullable String getMode() {
      return _mode;
    }

    public void setMode(@Nullable String mode) {
      _mode = mode;
    }

    public @Nullable String getMtu() {
      return _mtu;
    }

    public void setMtu(@Nullable String mtu) {
      _mtu = mtu;
    }

    public @Nullable String getName() {
      return _name;
    }

    public void setName(@Nullable String name) {
      _name = name;
    }

    public @Nullable String getNameAlias() {
      return _nameAlias;
    }

    public void setNameAlias(@Nullable String nameAlias) {
      _nameAlias = nameAlias;
    }

    public @Nullable String getSpeed() {
      return _speed;
    }

    public void setSpeed(@Nullable String speed) {
      _speed = speed;
    }

    public @Nullable String getUserDomain() {
      return _userDomain;
    }

    public void setUserDomain(@Nullable String userDomain) {
      _userDomain = userDomain;
    }
  }
}

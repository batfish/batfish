package org.batfish.representation.host;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Vrf;

public class HostInterface implements Serializable {

  private static final String PROP_BANDWIDTH = "bandwidth";

  private static final String PROP_GATEWAY = "gateway";

  private static final String PROP_NAME = "name";

  private static final String PROP_OTHER_PREFIXES = "otherPrefixes";

  private static final String PROP_PREFIX = "prefix";

  /** */
  private static final long serialVersionUID = 1L;

  private static final String PROP_VRF = "vrf";

  private Double _bandwidth = 1000 * 1000 * 1000.0; // default is 1 Gbps

  private Ip _gateway;

  private String _name;

  private Set<Prefix> _otherPrefixes;

  private Prefix _prefix;

  private Vrf _vrf;

  @JsonCreator
  public HostInterface(@JsonProperty(PROP_NAME) String name) {
    _name = name;
    _otherPrefixes = new TreeSet<>();
  }

  @JsonProperty(PROP_BANDWIDTH)
  public Double getBandwidth() {
    return _bandwidth;
  }

  @JsonProperty(PROP_GATEWAY)
  public Ip getGateway() {
    return _gateway;
  }

  @JsonProperty(PROP_NAME)
  public String getName() {
    return _name;
  }

  @JsonProperty(PROP_OTHER_PREFIXES)
  public Set<Prefix> getOtherPrefixes() {
    return _otherPrefixes;
  }

  @JsonProperty(PROP_PREFIX)
  public Prefix getPrefix() {
    return _prefix;
  }

  @JsonProperty(PROP_VRF)
  public Vrf getVrf() {
    return _vrf;
  }

  @JsonProperty(PROP_BANDWIDTH)
  public void setBandwidth(Double bandwidth) {
    _bandwidth = bandwidth;
  }

  @JsonProperty(PROP_GATEWAY)
  public void setGateway(Ip gateway) {
    _gateway = gateway;
  }

  @JsonProperty(PROP_OTHER_PREFIXES)
  public void setOtherPrefixes(Set<Prefix> otherPrefixes) {
    _otherPrefixes = otherPrefixes;
  }

  @JsonProperty(PROP_PREFIX)
  public void setPrefix(Prefix prefix) {
    _prefix = prefix;
  }

  @JsonProperty(PROP_VRF)
  public void setVrf(Vrf vrf) {
    _vrf = vrf;
  }

  public Interface toInterface(Configuration configuration, Warnings warnings) {
    Interface iface = new Interface(_name, configuration);
    iface.setBandwidth(_bandwidth);
    iface.setPrefix(_prefix);
    iface.getAllPrefixes().add(_prefix);
    iface.getAllPrefixes().addAll(_otherPrefixes);
    iface.setVrf(configuration.getDefaultVrf());
    return iface;
  }
}

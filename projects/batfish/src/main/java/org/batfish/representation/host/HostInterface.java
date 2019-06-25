package org.batfish.representation.host;

import static org.batfish.datamodel.transformation.Transformation.always;
import static org.batfish.datamodel.transformation.TransformationStep.assignSourceIp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSortedSet;
import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Vrf;

public class HostInterface implements Serializable {

  private static final String PROP_BANDWIDTH = "bandwidth";
  private static final String PROP_ENCAPSULATION_VLAN = "encapsulationVlan";
  private static final String PROP_GATEWAY = "gateway";
  private static final String PROP_NAME = "name";
  private static final String PROP_OTHER_PREFIXES = "otherPrefixes";
  private static final String PROP_PREFIX = "prefix";
  private static final String PROP_SHARED = "shared";
  private static final String PROP_VRF = "vrf";

  private Double _bandwidth = 1000 * 1000 * 1000.0; // default is 1 Gbps

  private transient String _canonicalName;

  private Ip _gateway;

  private String _name;

  private Set<InterfaceAddress> _otherAddresses;

  private ConcreteInterfaceAddress _address;

  private @Nullable Integer _encapsulationVlan;

  private boolean _shared;

  private Vrf _vrf;

  @JsonCreator
  public HostInterface(@JsonProperty(PROP_NAME) String name) {
    _name = name;
    _otherAddresses = new TreeSet<>();
  }

  @JsonProperty(PROP_PREFIX)
  public InterfaceAddress getAddress() {
    return _address;
  }

  @JsonProperty(PROP_BANDWIDTH)
  public Double getBandwidth() {
    return _bandwidth;
  }

  @JsonIgnore
  public String getCanonicalName() {
    return _canonicalName;
  }

  @JsonProperty(PROP_ENCAPSULATION_VLAN)
  public @Nullable Integer getEncapsulationVlan() {
    return _encapsulationVlan;
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
  public Set<InterfaceAddress> getOtherAddresses() {
    return _otherAddresses;
  }

  @JsonProperty(PROP_SHARED)
  public boolean getShared() {
    return _shared;
  }

  @JsonProperty(PROP_VRF)
  public Vrf getVrf() {
    return _vrf;
  }

  @JsonProperty(PROP_BANDWIDTH)
  public void setBandwidth(Double bandwidth) {
    _bandwidth = bandwidth;
  }

  @JsonIgnore
  public void setCanonicalName(String canonicalName) {
    _canonicalName = canonicalName;
  }

  @JsonProperty(PROP_ENCAPSULATION_VLAN)
  public void setEncapsulationVlan(@Nullable Integer encapsulationVlan) {
    _encapsulationVlan = encapsulationVlan;
  }

  @JsonProperty(PROP_GATEWAY)
  public void setGateway(Ip gateway) {
    _gateway = gateway;
  }

  @JsonProperty(PROP_OTHER_PREFIXES)
  public void setOtherAddresses(Set<InterfaceAddress> otherAddresses) {
    _otherAddresses = otherAddresses;
  }

  @JsonProperty(PROP_PREFIX)
  public void setAddress(ConcreteInterfaceAddress address) {
    _address = address;
  }

  @JsonProperty(PROP_SHARED)
  public void setShared(boolean shared) {
    _shared = shared;
  }

  @JsonProperty(PROP_VRF)
  public void setVrf(Vrf vrf) {
    _vrf = vrf;
  }

  public Interface toInterface(Configuration configuration, Warnings warnings) {
    String name = _canonicalName != null ? _canonicalName : _name;
    Interface.Builder iface =
        Interface.builder()
            .setName(name)
            .setOwner(configuration)
            .setAddresses(_address, _otherAddresses)
            .setBandwidth(_bandwidth)
            .setDeclaredNames(ImmutableSortedSet.of(_name))
            .setEncapsulationVlan(_encapsulationVlan)
            .setProxyArp(false)
            .setVrf(configuration.getDefaultVrf());
    if (_shared) {
      Ip publicIp = _address.getIp();
      iface.setOutgoingTransformation(always().apply(assignSourceIp(publicIp, publicIp)).build());
    }
    return iface.build();
  }
}

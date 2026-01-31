package org.batfish.vendor.arista.representation.eos;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

public class AristaEosVxlan implements Serializable {

  public static final Integer DEFAULT_UDP_PORT = 4789;

  private @Nullable Boolean _arpReplyRelay;
  private String _description;
  private SortedSet<Ip> _floodAddresses;
  private String _interfaceName;
  private Ip _multicastGroup;
  private String _sourceInterface;
  private Integer _udpPort;

  /** Map of VLAN number to VLAN-specific flood addresses */
  private SortedMap<Integer, SortedSet<Ip>> _vlanFloodAddresses;

  /** Map of VLAN number to VXLAN segment id (VNI) */
  private SortedMap<Integer, Integer> _vlanVnis;

  private final @Nonnull Map<String, Integer> _vrfToVni;

  public AristaEosVxlan(String interfaceName) {
    _floodAddresses = new TreeSet<>();
    _interfaceName = interfaceName;
    _vlanFloodAddresses = new TreeMap<>();
    _vlanVnis = new TreeMap<>();
    _vrfToVni = new HashMap<>(0);
  }

  public @Nullable Boolean getArpReplyRelay() {
    return _arpReplyRelay;
  }

  public void setArpReplyRelay(@Nullable Boolean arpReplyRelay) {
    this._arpReplyRelay = arpReplyRelay;
  }

  public String getDescription() {
    return _description;
  }

  public SortedSet<Ip> getFloodAddresses() {
    return _floodAddresses;
  }

  public String getInterfaceName() {
    return _interfaceName;
  }

  public Ip getMulticastGroup() {
    return _multicastGroup;
  }

  public String getSourceInterface() {
    return _sourceInterface;
  }

  public Integer getUdpPort() {
    return _udpPort;
  }

  public SortedMap<Integer, SortedSet<Ip>> getVlanFloodAddresses() {
    return _vlanFloodAddresses;
  }

  public SortedMap<Integer, Integer> getVlanVnis() {
    return _vlanVnis;
  }

  public void setDescription(String description) {
    _description = description;
  }

  public void setMulticastGroup(Ip multicastGroup) {
    _multicastGroup = multicastGroup;
  }

  public void setSourceInterface(String sourceInterface) {
    _sourceInterface = sourceInterface;
  }

  public void setUdpPort(Integer udpPort) {
    _udpPort = udpPort;
  }

  public @Nonnull Map<String, Integer> getVrfToVni() {
    return _vrfToVni;
  }
}

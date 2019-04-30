package org.batfish.representation.vyos;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.InterfaceAddress;

public class Interface implements Serializable {

  private static final long serialVersionUID = 1L;

  public static double getDefaultBandwidth(InterfaceType type) {
    // TODO: update with correct values
    switch (type) {
      case ETHERNET:
      case LOOPBACK:
      case VTI:
        return 1E12d;

      case BONDING:
      case BRIDGE:
      case DUMMY:
      case INPUT:
      case L2TPV3:
      case OPENVPN:
      case PSEUDO_ETHERNET:
      case TUNNEL:
      case VXLAN:
      case WIRELESS:
      case WIRELESSMODEM:
      default:
        throw new BatfishException("unsupported interface type");
    }
  }

  private InterfaceAddress _address;

  private final Set<InterfaceAddress> _allAddresses;

  private double _bandwidth;

  private String _description;

  private final String _name;

  private InterfaceType _type;

  public Interface(String name) {
    _allAddresses = new TreeSet<>();
    _name = name;
  }

  public Set<InterfaceAddress> getAllAddresses() {
    return _allAddresses;
  }

  public InterfaceAddress getAddress() {
    return _address;
  }

  public double getBandwidth() {
    return _bandwidth;
  }

  public String getDescription() {
    return _description;
  }

  public String getName() {
    return _name;
  }

  public InterfaceType getType() {
    return _type;
  }

  public void setBandwidth(double bandwidth) {
    _bandwidth = bandwidth;
  }

  public void setDescription(String description) {
    _description = description;
  }

  public void setAddress(InterfaceAddress address) {
    _address = address;
  }

  public void setType(InterfaceType type) {
    _type = type;
  }
}

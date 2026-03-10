package org.batfish.representation.vyos;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.ConcreteInterfaceAddress;

public class Interface implements Serializable {

  public static double getDefaultBandwidth(InterfaceType type) {
    // TODO: update with correct values
    return switch (type) {
      case ETHERNET, LOOPBACK, VTI -> 1E12d;
      case BONDING,
          BRIDGE,
          DUMMY,
          INPUT,
          L2TPV3,
          OPENVPN,
          PSEUDO_ETHERNET,
          TUNNEL,
          VXLAN,
          WIRELESS,
          WIRELESSMODEM ->
          throw new BatfishException("unsupported interface type");
    };
  }

  private ConcreteInterfaceAddress _address;

  private final Set<ConcreteInterfaceAddress> _allAddresses;

  private double _bandwidth;

  private String _description;

  private final String _name;

  private InterfaceType _type;

  public Interface(String name) {
    _allAddresses = new TreeSet<>();
    _name = name;
  }

  public Set<ConcreteInterfaceAddress> getAllAddresses() {
    return _allAddresses;
  }

  public ConcreteInterfaceAddress getAddress() {
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

  public void setAddress(ConcreteInterfaceAddress address) {
    _address = address;
  }

  public void setType(InterfaceType type) {
    _type = type;
  }
}

package org.batfish.specifier;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Interface;

/**
 * Identifies a single location in the network -- an VRF, an interface, the link of an interface,
 * etc. Locations are pure data -- they should have no behavior other than the accept method that
 * allows them to be inspected.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public interface Location extends Serializable {
  <T> T accept(LocationVisitor<T> visitor);

  @Nonnull
  String getNodeName();

  static InterfaceLocation interfaceLocation(Interface iface) {
    return new InterfaceLocation(iface.getOwner().getHostname(), iface.getName());
  }

  static InterfaceLinkLocation interfaceLinkLocation(Interface iface) {
    return new InterfaceLinkLocation(iface.getOwner().getHostname(), iface.getName());
  }
}

package org.batfish.representation.palo_alto;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ServiceOrServiceGroupReference
    implements Comparable<ServiceOrServiceGroupReference>, Reference {

  @Override
  public <T, U> T accept(ReferenceVisitor<T, U> visitor, U arg) {
    return visitor.visitServiceOrServiceGroupReference(this, arg);
  }

  public ServiceOrServiceGroupReference(String name) {
    _name = name;
  }

  @Override
  public int compareTo(@Nonnull ServiceOrServiceGroupReference o) {
    return _name.compareTo(o._name);
  }

  /** Return the name of the referenced Service or ServiceGroup */
  public String getName() {
    return _name;
  }

  /**
   * Return the name of the vsys this reference is attached to, or return null if no match is found
   */
  @Nullable
  @SuppressWarnings("fallthrough")
  String getVsysName(PaloAltoConfiguration pc, Vsys vsys) {
    if (vsys.getServices().containsKey(_name) || vsys.getServiceGroups().containsKey(_name)) {
      return vsys.getName();
    }
    switch (vsys.getNamespaceType()) {
      case LEAF:
        if (pc.getPanorama() != null) {
          return getVsysName(pc, pc.getPanorama());
        }
        // fall-through
      case PANORAMA:
        if (pc.getShared() != null) {
          return getVsysName(pc, pc.getShared());
        }
        // fall-through
      default:
        return null;
    }
  }

  private final String _name;
}

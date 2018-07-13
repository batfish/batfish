package org.batfish.representation.palo_alto;

import static org.batfish.representation.palo_alto.PaloAltoConfiguration.SHARED_VSYS_NAME;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ServiceOrServiceGroupReference
    implements Serializable, Comparable<ServiceOrServiceGroupReference> {
  private static final long serialVersionUID = 1L;

  private final String _name;

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
   * Return the resolved reference (ServiceGroupMember) from the specified vsys, or return null if
   * no match is found
   */
  private static @Nullable ServiceGroupMember resolveReference(
      Vsys vsysToSearch, String referenceName) {
    ServiceGroupMember member = vsysToSearch.getServices().get(referenceName);
    return (member != null) ? member : vsysToSearch.getServiceGroups().get(referenceName);
  }

  /**
   * Return the name of the vsys this reference is attached to, or return null if no match is found
   */
  @Nullable
  String getVsysName(PaloAltoConfiguration pc, Vsys vsys) {
    // Search for a matching member in the local then shared namespace, in that order
    for (Vsys currentVsys : ImmutableList.of(vsys, pc.getVirtualSystems().get(SHARED_VSYS_NAME))) {
      if (resolveReference(currentVsys, _name) != null) {
        return currentVsys.getName();
      }
    }
    return null;
  }
}

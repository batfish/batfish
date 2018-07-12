package org.batfish.representation.palo_alto;

import static org.batfish.representation.palo_alto.PaloAltoConfiguration.SHARED_VSYS_NAME;
import static org.batfish.representation.palo_alto.PaloAltoConfiguration.computeServiceGroupMemberAclName;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.acl.PermittedByAcl;

public class ServiceOrServiceGroupReference
    implements ServiceGroupMember, Comparable<ServiceOrServiceGroupReference> {
  private static final long serialVersionUID = 1L;

  private final String _name;

  public ServiceOrServiceGroupReference(String name) {
    _name = name;
  }

  @Override
  public int compareTo(@Nonnull ServiceOrServiceGroupReference o) {
    return _name.compareTo(o._name);
  }

  @Override
  public void addTo(
      List<IpAccessListLine> lines, LineAction action, PaloAltoConfiguration pc, Vsys vsys) {
    // Search for a matching member in the local then shared namespace, in that order
    for (Vsys currentVsys : ImmutableList.of(vsys, pc.getVirtualSystems().get(SHARED_VSYS_NAME))) {
      if (getServiceGroupMemberByName(currentVsys, _name) != null) {
        lines.add(
            IpAccessListLine.builder()
                .setAction(action)
                .setMatchCondition(
                    new PermittedByAcl(
                        computeServiceGroupMemberAclName(currentVsys.getName(), _name)))
                .build());
        return;
      }
    }
  }

  @Override
  public String getName() {
    return _name;
  }

  /**
   * Returns Service or ServiceGroup with the specified name in the specified vsys, or returns null
   * if no match is found
   */
  private static @Nullable ServiceGroupMember getServiceGroupMemberByName(Vsys vsys, String name) {
    ServiceGroupMember member = vsys.getServices().get(name);
    return (member != null) ? member : vsys.getServiceGroups().get(name);
  }
}

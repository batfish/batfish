package org.batfish.representation.palo_alto;

import static org.batfish.representation.palo_alto.PaloAltoConfiguration.computeServiceGroupMemberAclName;

import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.acl.PermittedByAcl;

public class ServiceOrServiceGroupReference implements ServiceGroupMember, Comparable {
  private static final long serialVersionUID = 1L;

  private final String _name;

  public ServiceOrServiceGroupReference(String name) {
    _name = name;
  }

  @Override
  public void applyTo(Vsys vsys, LineAction action, List<IpAccessListLine> lines) {
    // Reference members
    ServiceGroupMember member = vsys.getServices().get(_name);
    if (member == null) {
      member = vsys.getServiceGroups().get(_name);
    }
    if (member != null) {
      lines.add(
          IpAccessListLine.builder()
              .setAction(action)
              .setMatchCondition(
                  new PermittedByAcl(computeServiceGroupMemberAclName(vsys.getName(), _name)))
              .build());
    }

    /*
    // Unroll members
    ServiceGroupMember member = vsys.getServices().get(_name);
    if (member == null) {
      member = vsys.getServiceGroups().get(_name);
    }
    if (member != null) {
      member.applyTo(vsys, action, lines);
    }
    */
  }

  @Override
  public int compareTo(@Nonnull Object o) {
    return _name.compareTo(((ServiceOrServiceGroupReference) o)._name);
  }
}

package org.batfish.representation.palo_alto;

import static org.batfish.representation.palo_alto.PaloAltoConfiguration.computeServiceGroupMemberAclName;

import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import org.batfish.common.Warnings;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.acl.TrueExpr;

public final class ServiceGroup implements ServiceGroupMember {

  private final String _name;

  private final SortedSet<ServiceOrServiceGroupReference> _references;

  public ServiceGroup(String name) {
    _name = name;
    _references = new TreeSet<>();
  }

  @Override
  public String getName() {
    return _name;
  }

  public SortedSet<ServiceOrServiceGroupReference> getReferences() {
    return _references;
  }

  @Override
  public IpAccessList toIpAccessList(
      LineAction action, PaloAltoConfiguration pc, Vsys vsys, Warnings w) {
    List<AclLine> lines = new LinkedList<>();
    for (ServiceOrServiceGroupReference memberReference : _references) {
      // Check for matching object before using built-ins
      String vsysName = memberReference.getVsysName(pc, vsys);
      String serviceName = memberReference.getName();

      if (vsysName != null) {
        lines.add(
            new ExprAclLine(
                action,
                new PermittedByAcl(
                    computeServiceGroupMemberAclName(vsysName, memberReference.getName())),
                memberReference.getName()));
      } else if (serviceName.equals(ServiceBuiltIn.ANY.getName())) {
        lines.clear();
        lines.add(new ExprAclLine(action, TrueExpr.INSTANCE, _name));
        break;
      } else if (serviceName.equals(ServiceBuiltIn.SERVICE_HTTP.getName())) {
        lines.add(
            new ExprAclLine(
                action, new MatchHeaderSpace(ServiceBuiltIn.SERVICE_HTTP.getHeaderSpace()), _name));
      } else if (serviceName.equals(ServiceBuiltIn.SERVICE_HTTPS.getName())) {
        lines.add(
            new ExprAclLine(
                action,
                new MatchHeaderSpace(ServiceBuiltIn.SERVICE_HTTPS.getHeaderSpace()),
                _name));
      }
    }
    return IpAccessList.builder()
        .setName(computeServiceGroupMemberAclName(vsys.getName(), _name))
        .setLines(lines)
        .setSourceName(_name)
        .setSourceType(PaloAltoStructureType.SERVICE_GROUP.getDescription())
        .build();
  }
}

package org.batfish.representation.palo_alto;

import static org.batfish.representation.palo_alto.PaloAltoConfiguration.computeServiceGroupMemberAclName;
import static org.batfish.representation.palo_alto.PaloAltoTraceElementCreators.matchServiceGroupTraceElement;

import com.google.common.collect.ImmutableList;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import org.batfish.common.Warnings;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.batfish.datamodel.acl.PermittedByAcl;

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
      LineAction action, PaloAltoConfiguration pc, Vsys vsys, Warnings w, String filename) {
    List<AclLine> lines = new LinkedList<>();
    List<AclLineMatchExpr> exprLines = new LinkedList<>();
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
        exprLines.add(new PermittedByAcl(computeServiceGroupMemberAclName(vsysName, serviceName)));
      } else if (serviceName.equals(ServiceBuiltIn.ANY.getName())) {
        lines.clear();
        lines.add(new ExprAclLine(action, ServiceBuiltIn.ANY.toAclLineMatchExpr(), _name));
        exprLines.clear();
        exprLines.add(ServiceBuiltIn.ANY.toAclLineMatchExpr());
        break;
      } else if (serviceName.equals(ServiceBuiltIn.SERVICE_HTTP.getName())) {
        lines.add(new ExprAclLine(action, ServiceBuiltIn.SERVICE_HTTP.toAclLineMatchExpr(), _name));
        exprLines.add(ServiceBuiltIn.SERVICE_HTTP.toAclLineMatchExpr());
      } else if (serviceName.equals(ServiceBuiltIn.SERVICE_HTTPS.getName())) {
        lines.add(
            new ExprAclLine(action, ServiceBuiltIn.SERVICE_HTTPS.toAclLineMatchExpr(), _name));
        exprLines.add(ServiceBuiltIn.SERVICE_HTTPS.toAclLineMatchExpr());
      }
    }
    return IpAccessList.builder()
        .setName(computeServiceGroupMemberAclName(vsys.getName(), _name))
        // .setLines(lines)
        .setLines(
            ImmutableList.of(
                new ExprAclLine(
                    action,
                    new OrMatchExpr(exprLines),
                    _name,
                    matchServiceGroupTraceElement(_name, vsys.getName(), filename))))
        .setSourceName(_name)
        .setSourceType(PaloAltoStructureType.SERVICE_GROUP.getDescription())
        .build();
  }
}

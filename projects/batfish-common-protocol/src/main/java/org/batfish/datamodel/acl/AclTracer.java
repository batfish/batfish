package org.batfish.datamodel.acl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.IdentityHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.AclIpSpaceLine;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.visitors.IpSpaceTracer;

public class AclTracer extends Evaluator {

  private Builder<TraceEvent> _traceEvents;

  private final Map<IpSpace, String> _ipSpaceNames;

  public Map<IpSpace, String> getIpSpaceNames() {
    return _ipSpaceNames;
  }

  public AclTracer(
      @Nonnull Flow flow,
      @Nullable String srcInterface,
      @Nonnull Map<String, IpAccessList> availableAcls,
      @Nonnull Map<String, IpSpace> namedIpSpaces) {
    super(flow, srcInterface, availableAcls, namedIpSpaces);
    _ipSpaceNames = new IdentityHashMap<>();
    _traceEvents = ImmutableList.builder();
    namedIpSpaces.forEach((name, ipSpace) -> _ipSpaceNames.put(ipSpace, name));
  }

  public AclTrace getTrace() {
    return new AclTrace(_traceEvents.build());
  }

  @Override
  public Boolean visitAndMatchExpr(AndMatchExpr andMatchExpr) {
    return andMatchExpr.getConjuncts().stream().allMatch(conjunct -> conjunct.accept(this));
  }

  @Override
  public Boolean visitMatchHeaderSpace(MatchHeaderSpace matchHeaderSpace) {
    return matchHeaderSpace.getHeaderspace().trace(this);
  }

  @Override
  public Boolean visitNotMatchExpr(NotMatchExpr notMatchExpr) {
    return !notMatchExpr.getOperand().accept(this);
  }

  @Override
  public Boolean visitOrMatchExpr(OrMatchExpr orMatchExpr) {
    return orMatchExpr.getDisjuncts().stream().anyMatch(disjunct -> disjunct.accept(this));
  }

  @Override
  public Boolean visitPermittedByAcl(PermittedByAcl permittedByAcl) {
    return _availableAcls.get(permittedByAcl.getAclName()).trace(this);
  }

  public void recordAction(IpAccessList ipAccessList, int index, IpAccessListLine line) {
    if (line.getAction() == LineAction.ACCEPT) {
      _traceEvents.add(
          new PermittedByIpAccessListLine(ipAccessList.getName(), index, line.getName()));
    } else {
      _traceEvents.add(new DeniedByIpAccessListLine(ipAccessList.getName(), index, line.getName()));
    }
  }

  public void recordDefaultDeny(IpAccessList ipAccessList) {
    _traceEvents.add(new DefaultDeniedByIpAccessList(ipAccessList.getName()));
  }

  public Flow getFlow() {
    return _flow;
  }

  public Map<String, IpSpace> getNamedIpSpaces() {
    return _namedIpSpaces;
  }

  public void recordAction(String aclIpSpaceName, int index, AclIpSpaceLine line) {
    if (line.getAction() == LineAction.ACCEPT) {
      _traceEvents.add(
          new PermittedByAclIpSpaceLine(aclIpSpaceName, index, line.getIpSpace().toString()));
    } else {
      _traceEvents.add(
          new DeniedByAclIpSpaceLine(aclIpSpaceName, index, line.getIpSpace().toString()));
    }
  }

  public void recordDefaultDeny(String aclIpSpaceName) {
    _traceEvents.add(new DefaultDeniedByAclIpSpace(aclIpSpaceName));
  }

  public void recordNamedIpSpaceAction(String name, String description, boolean permit) {
    if (permit) {
      _traceEvents.add(new PermittedByNamedIpSpace(name, description));
    } else {
      _traceEvents.add(new DeniedByNamedIpSpace(name, description));
    }
  }

  public boolean trace(IpSpace ipSpace, Ip ip) {
    return ipSpace.accept(new IpSpaceTracer(this, ip));
  }
}

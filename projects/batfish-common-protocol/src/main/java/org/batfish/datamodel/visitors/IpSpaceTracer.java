package org.batfish.datamodel.visitors;

import java.util.List;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.AclIpSpaceLine;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpIpSpace;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.IpWildcardIpSpace;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.PrefixIpSpace;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.acl.AclTracer;

public class IpSpaceTracer implements GenericIpSpaceVisitor<Boolean> {

  private final Ip _ip;

  private final AclTracer _aclTracer;

  public IpSpaceTracer(AclTracer aclTracer, Ip ip) {
    _aclTracer = aclTracer;
    _ip = ip;
  }

  @Override
  public Boolean castToGenericIpSpaceVisitorReturnType(Object o) {
    return (Boolean) o;
  }

  @Override
  public Boolean visitAclIpSpace(AclIpSpace aclIpSpace) {
    String name = _aclTracer.getIpSpaceNames().get(aclIpSpace);
    List<AclIpSpaceLine> lines = aclIpSpace.getLines();
    for (int i = 0; i < lines.size(); i++) {
      AclIpSpaceLine line = lines.get(i);
      if (line.getIpSpace().accept(this)) {
        if (name != null) {
          _aclTracer.recordAction(name, i, line);
        }
        return line.getAction() == LineAction.ACCEPT;
      }
    }
    if (name != null) {
      _aclTracer.recordDefaultDeny(name);
    }
    return false;
  }

  @Override
  public Boolean visitEmptyIpSpace(EmptyIpSpace emptyIpSpace) {
    return reportIfNamed(emptyIpSpace);
  }

  @Override
  public Boolean visitIpIpSpace(IpIpSpace ipIpSpace) {
    return reportIfNamed(ipIpSpace);
  }

  @Override
  public Boolean visitIpSpaceReference(IpSpaceReference ipSpaceReference) {
    String name = ipSpaceReference.getName();
    IpSpace ipSpace = _aclTracer.getNamedIpSpaces().get(name);
    if (ipSpace != null) {
      return ipSpace.accept(this);
    } else {
      return false;
    }
  }

  private boolean reportIfNamed(IpSpace ipSpace) {
    boolean result = ipSpace.containsIp(_ip, _aclTracer.getNamedIpSpaces());
    String name = _aclTracer.getIpSpaceNames().get(ipSpace);
    if (name != null) {
      _aclTracer.recordNamedIpSpaceAction(name, ipSpace.toString(), result);
    }
    return result;
  }

  @Override
  public Boolean visitIpWildcardIpSpace(IpWildcardIpSpace ipWildcardIpSpace) {
    return reportIfNamed(ipWildcardIpSpace);
  }

  @Override
  public Boolean visitIpWildcardSetIpSpace(IpWildcardSetIpSpace ipWildcardSetIpSpace) {
    return reportIfNamed(ipWildcardSetIpSpace);
  }

  @Override
  public Boolean visitPrefixIpSpace(PrefixIpSpace prefixIpSpace) {
    return reportIfNamed(prefixIpSpace);
  }

  @Override
  public Boolean visitUniverseIpSpace(UniverseIpSpace universeIpSpace) {
    return reportIfNamed(universeIpSpace);
  }
}

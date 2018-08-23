package org.batfish.datamodel.visitors;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.IpIpSpace;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpWildcardIpSpace;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.PrefixIpSpace;
import org.batfish.datamodel.UniverseIpSpace;

/** False-negatives are ok, but false-positives are not. */
public class IpSpaceContainedInWildcard implements GenericIpSpaceVisitor<Boolean> {
  private final IpWildcard _ipWildcard;

  private final Map<String, IpSpace> _namedIpSpaces;

  public IpSpaceContainedInWildcard(IpWildcard ipWildcard, Map<String, IpSpace> namedIpSpaces) {
    _namedIpSpaces = ImmutableMap.copyOf(namedIpSpaces);
    _ipWildcard = ipWildcard;
  }

  @Override
  public Boolean castToGenericIpSpaceVisitorReturnType(Object o) {
    return (Boolean) o;
  }

  @Override
  public Boolean visitAclIpSpace(AclIpSpace aclIpSpace) {
    return aclIpSpace
        .getLines()
        .stream()
        .filter(line -> line.getAction() == LineAction.PERMIT)
        .allMatch(line -> line.getIpSpace().accept(this));
  }

  @Override
  public Boolean visitEmptyIpSpace(EmptyIpSpace emptyIpSpace) {
    return true;
  }

  @Override
  public Boolean visitIpIpSpace(IpIpSpace ipIpSpace) {
    return _ipWildcard.containsIp(ipIpSpace.getIp());
  }

  @Override
  public Boolean visitIpSpaceReference(IpSpaceReference ipSpaceReference) {
    return _namedIpSpaces.get(ipSpaceReference.getName()).accept(this);
  }

  @Override
  public Boolean visitIpWildcardIpSpace(IpWildcardIpSpace ipWildcardIpSpace) {
    return _ipWildcard.supersetOf(ipWildcardIpSpace.getIpWildcard());
  }

  @Override
  public Boolean visitIpWildcardSetIpSpace(IpWildcardSetIpSpace ipWildcardSetIpSpace) {
    return ipWildcardSetIpSpace.getWhitelist().stream().allMatch(_ipWildcard::supersetOf);
  }

  @Override
  public Boolean visitPrefixIpSpace(PrefixIpSpace prefixIpSpace) {
    return _ipWildcard.supersetOf(new IpWildcard(prefixIpSpace.getPrefix()));
  }

  @Override
  public Boolean visitUniverseIpSpace(UniverseIpSpace universeIpSpace) {
    return _ipWildcard.equals(IpWildcard.ANY);
  }
}

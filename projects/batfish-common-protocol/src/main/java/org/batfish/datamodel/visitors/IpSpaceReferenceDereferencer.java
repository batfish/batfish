package org.batfish.datamodel.visitors;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Nullable;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.IpIpSpace;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.IpWildcardIpSpace;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.PrefixIpSpace;
import org.batfish.datamodel.UniverseIpSpace;

/** Returns the non-IpSpaceReference IpSpace that the given IpSpace points to. */
public class IpSpaceReferenceDereferencer implements GenericIpSpaceVisitor<IpSpace> {

  private final Map<String, IpSpace> _namedIpSpaces;

  public IpSpaceReferenceDereferencer(Map<String, IpSpace> namedIpSpaces) {
    _namedIpSpaces = namedIpSpaces;
  }

  @Override
  public IpSpace castToGenericIpSpaceVisitorReturnType(Object o) {
    return (IpSpace) o;
  }

  @Override
  public IpSpace visitAclIpSpace(AclIpSpace aclIpSpace) {
    return aclIpSpace;
  }

  @Override
  public IpSpace visitEmptyIpSpace(EmptyIpSpace emptyIpSpace) {
    return emptyIpSpace;
  }

  @Override
  public IpSpace visitIpIpSpace(IpIpSpace ipIpSpace) {
    return ipIpSpace;
  }

  /**
   * @param ipSpaceReference {@link IpSpaceReference} to dereference.
   * @return The non-{@link IpSpaceReference} {@link IpSpace} referenced by the given {@link
   *     IpSpaceReference}, or {@code null} if the given {@link IpSpaceReference} is in a circular
   *     chain of references or ultimately references an undefined {@link IpSpace}.
   */
  @Override
  public @Nullable IpSpace visitIpSpaceReference(IpSpaceReference ipSpaceReference) {
    return visitIpSpaceReferenceHelper(ipSpaceReference, new TreeSet<>());
  }

  private IpSpace visitIpSpaceReferenceHelper(
      IpSpaceReference ipSpace, Set<String> referencedSoFar) {
    String name = ipSpace.getName();
    if (referencedSoFar.contains(name)) {
      // Circular reference; no base IpSpace to dereference.
      return null;
    }
    referencedSoFar.add(name);
    IpSpace referenced = _namedIpSpaces.get(name);
    if (referenced instanceof IpSpaceReference) {
      // The given reference references another reference. Keep going down the rabbit hole
      return visitIpSpaceReferenceHelper((IpSpaceReference) referenced, referencedSoFar);
    } else {
      // The given reference did not reference another reference. If it referenced a valid IP space,
      // this will return that IP space; if the referenced IP space is undefined, it returns null.
      return referenced;
    }
  }

  @Override
  public IpSpace visitIpWildcardIpSpace(IpWildcardIpSpace ipWildcardIpSpace) {
    return ipWildcardIpSpace;
  }

  @Override
  public IpSpace visitIpWildcardSetIpSpace(IpWildcardSetIpSpace ipWildcardSetIpSpace) {
    return ipWildcardSetIpSpace;
  }

  @Override
  public IpSpace visitPrefixIpSpace(PrefixIpSpace prefixIpSpace) {
    return prefixIpSpace;
  }

  @Override
  public IpSpace visitUniverseIpSpace(UniverseIpSpace universeIpSpace) {
    return universeIpSpace;
  }
}

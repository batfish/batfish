package org.batfish.datamodel.visitors;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
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
public class IpSpaceReferenceDereferencer implements GenericIpSpaceVisitor<Optional<IpSpace>> {

  private final Map<String, IpSpace> _namedIpSpaces;

  public IpSpaceReferenceDereferencer(Map<String, IpSpace> namedIpSpaces) {
    _namedIpSpaces = namedIpSpaces;
  }

  @Override
  public Optional<IpSpace> castToGenericIpSpaceVisitorReturnType(Object o) {
    return (Optional<IpSpace>) o;
  }

  @Override
  public Optional<IpSpace> visitAclIpSpace(AclIpSpace aclIpSpace) {
    return Optional.of(aclIpSpace);
  }

  @Override
  public Optional<IpSpace> visitEmptyIpSpace(EmptyIpSpace emptyIpSpace) {
    return Optional.of(emptyIpSpace);
  }

  @Override
  public Optional<IpSpace> visitIpIpSpace(IpIpSpace ipIpSpace) {
    return Optional.of(ipIpSpace);
  }

  /**
   * @param ipSpaceReference {@link IpSpaceReference} to dereference.
   * @return The non-{@link IpSpaceReference} {@link IpSpace} referenced by the given {@link
   *     IpSpaceReference}, or {@code null} if the given {@link IpSpaceReference} is in a circular
   *     chain of references or ultimately references an undefined {@link IpSpace}.
   */
  @Override
  public Optional<IpSpace> visitIpSpaceReference(IpSpaceReference ipSpaceReference) {
    Set<String> referencedIpSpaces = new TreeSet<>();
    IpSpace referenced = ipSpaceReference;
    while (referenced instanceof IpSpaceReference) {
      ipSpaceReference = (IpSpaceReference) referenced;
      String name = ipSpaceReference.getName();
      if (!referencedIpSpaces.add(name)) {
        // Reference cycle; no base IpSpace to dereference.
        return Optional.empty();
      }
      referenced = _namedIpSpaces.get(name);
      if (referenced == null) {
        // Undefined IP space referenced.
        return Optional.empty();
      }
    }
    return Optional.of(referenced);
  }

  @Override
  public Optional<IpSpace> visitIpWildcardIpSpace(IpWildcardIpSpace ipWildcardIpSpace) {
    return Optional.of(ipWildcardIpSpace);
  }

  @Override
  public Optional<IpSpace> visitIpWildcardSetIpSpace(IpWildcardSetIpSpace ipWildcardSetIpSpace) {
    return Optional.of(ipWildcardSetIpSpace);
  }

  @Override
  public Optional<IpSpace> visitPrefixIpSpace(PrefixIpSpace prefixIpSpace) {
    return Optional.of(prefixIpSpace);
  }

  @Override
  public Optional<IpSpace> visitUniverseIpSpace(UniverseIpSpace universeIpSpace) {
    return Optional.of(universeIpSpace);
  }
}

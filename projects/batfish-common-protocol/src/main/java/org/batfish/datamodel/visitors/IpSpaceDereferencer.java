package org.batfish.datamodel.visitors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.AclIpSpaceLine;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.IpIpSpace;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.IpWildcardIpSpace;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.PrefixIpSpace;
import org.batfish.datamodel.UniverseIpSpace;

/** Returns the TODO non-IpSpaceReference IpSpace that the given IpSpace points to. */
public class IpSpaceDereferencer implements GenericIpSpaceVisitor<Optional<IpSpace>> {

  private final Map<String, IpSpace> _namedIpSpaces;
  private final Set<String> _referencedIpSpaces = new TreeSet<>();

  public IpSpaceDereferencer(Map<String, IpSpace> namedIpSpaces) {
    _namedIpSpaces = namedIpSpaces;
  }

  @Override
  public Optional<IpSpace> castToGenericIpSpaceVisitorReturnType(Object o) {
    return o == null ? Optional.empty() : Optional.of((IpSpace) o);
  }

  /**
   * @param aclIpSpace The {@link AclIpSpace} to sanitize.
   * @return An {@link Optional} containing an {@link AclIpSpace} identical to the original but with
   *     all uses of {@link IpSpaceReference} replaced with the dereferenced {@link IpSpace} they
   *     represent; or an empty {@link Optional} if original {@link AclIpSpace} points to any
   *     cyclical or undefined reference.
   */
  @Override
  public Optional<IpSpace> visitAclIpSpace(AclIpSpace aclIpSpace) {
    List<AclIpSpaceLine> sanitizedLines = new ArrayList<>();
    for (AclIpSpaceLine line : aclIpSpace.getLines()) {
      Optional<IpSpace> ipSpace = line.getIpSpace().accept(this);
      if (!ipSpace.isPresent()) {
        // Found an IpSpaceReference or AclIpSpace that contained a cycle or undefined ref.
        return Optional.empty();
      }
      sanitizedLines.add(AclIpSpaceLine.builder().setIpSpace(ipSpace.get()).build());
    }
    // No cycles/undefined references in this AclIpSpace. Return reference-free version.
    return Optional.of(AclIpSpace.builder().setLines(sanitizedLines).build());
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
   * @return An {@link Optional} containing the dereferenced {@link IpSpace} referenced by the given
   *     {@link IpSpaceReference}, or an empty {@link Optional} if the given {@link
   *     IpSpaceReference} points to a circular chain of references or an undefined {@link IpSpace}.
   */
  @Override
  public Optional<IpSpace> visitIpSpaceReference(IpSpaceReference ipSpaceReference) {
    String name = ipSpaceReference.getName();
    if (!_referencedIpSpaces.add(name)) {
      // This is a reference to an already-referenced IP space. Line is in a cycle.
      return Optional.empty();
    }
    IpSpace referenced = _namedIpSpaces.get(name);
    if (referenced == null) {
      // This reference is to an undefined IP space.
      return Optional.empty();
    }
    // Current IpSpace references another valid and as yet unreferenced IP space. Visit that.
    return referenced.accept(this);
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

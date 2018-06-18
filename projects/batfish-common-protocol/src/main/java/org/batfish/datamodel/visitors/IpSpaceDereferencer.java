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
    return sanitize(aclIpSpace, new TreeSet<>());
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
    return sanitize(ipSpaceReference, new TreeSet<>());
  }

  private Optional<IpSpace> sanitize(IpSpace current, Set<String> referencedIpSpaces) {
    if (current instanceof IpSpaceReference) {
      IpSpaceReference reference = (IpSpaceReference) current;
      String name = reference.getName();
      if (!referencedIpSpaces.add(name)) {
        // Hit a reference to an already-referenced IP space. Line is a cycle.
        return Optional.empty();
      }
      IpSpace referenced = _namedIpSpaces.get(name);
      if (referenced == null) {
        // Hit a reference to an undefined IP space.
        return Optional.empty();
      }
      // Current IpSpace references another valid and as yet unreferenced IP space. Sanitize that.
      return sanitize(referenced, referencedIpSpaces);
    } else if (current instanceof AclIpSpace) {
      List<AclIpSpaceLine> sanitizedLines = new ArrayList<>();
      for (AclIpSpaceLine line : ((AclIpSpace) current).getLines()) {
        Optional<IpSpace> ipSpace = sanitize(line.getIpSpace(), referencedIpSpaces);
        if (!ipSpace.isPresent()) {
          // Found an IpSpaceReference or AclIpSpace that contained a cycle or undefined ref.
          return Optional.empty();
        }
        sanitizedLines.add(AclIpSpaceLine.builder().setIpSpace(ipSpace.get()).build());
      }
      // All lines of AclIpSpace proved free of cycles/undefined references. Finalize sane version.
      return Optional.of(AclIpSpace.builder().setLines(sanitizedLines).build());
    } else {
      // IpSpace was not
      return Optional.of(current);
    }
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

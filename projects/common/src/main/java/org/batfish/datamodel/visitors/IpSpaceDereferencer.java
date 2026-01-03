package org.batfish.datamodel.visitors;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.AclIpSpaceLine;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpIpSpace;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.IpWildcardIpSpace;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.PrefixIpSpace;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.acl.CircularReferenceException;
import org.batfish.datamodel.acl.UndefinedReferenceException;

/** Returns the non-IpSpaceReference IpSpace that the given IpSpace points to. */
public class IpSpaceDereferencer implements GenericIpSpaceVisitor<IpSpace> {

  private final Map<String, IpSpace> _namedIpSpaces;
  private final Set<String> _referencedIpSpaces = new TreeSet<>();

  public IpSpaceDereferencer(Map<String, IpSpace> namedIpSpaces) {
    _namedIpSpaces = namedIpSpaces;
  }

  /**
   * Constructs a sanitized {@link HeaderSpace} where all {@link IpSpaceReference}s are replaced
   * with their dereferenced {@link IpSpace}s.
   *
   * @param headerSpace The header space whose IpSpaces to dereference
   * @param namedIpSpaces Named IP spaces in the context of the given header space.
   * @return A version of the given header space with all {@link IpSpace} fields dereferenced.
   * @throws CircularReferenceException if any {@link IpSpace} in the header space points to a
   *     cyclical reference.
   * @throws UndefinedReferenceException if any {@link IpSpace} in the header space points to an
   *     undefined reference.
   */
  public static HeaderSpace dereferenceHeaderSpace(
      HeaderSpace headerSpace, Map<String, IpSpace> namedIpSpaces)
      throws CircularReferenceException, UndefinedReferenceException {
    HeaderSpace.Builder hsb = headerSpace.toBuilder();
    IpSpace original = headerSpace.getSrcIps();
    if (original != null) {
      hsb.setSrcIps(original.accept(new IpSpaceDereferencer(namedIpSpaces)));
    }
    original = headerSpace.getDstIps();
    if (original != null) {
      hsb.setDstIps(original.accept(new IpSpaceDereferencer(namedIpSpaces)));
    }
    original = headerSpace.getNotSrcIps();
    if (original != null) {
      hsb.setNotSrcIps(original.accept(new IpSpaceDereferencer(namedIpSpaces)));
    }
    original = headerSpace.getNotDstIps();
    if (original != null) {
      hsb.setNotDstIps(original.accept(new IpSpaceDereferencer(namedIpSpaces)));
    }
    original = headerSpace.getSrcOrDstIps();
    if (original != null) {
      hsb.setSrcOrDstIps(original.accept(new IpSpaceDereferencer(namedIpSpaces)));
    }
    return hsb.build();
  }

  /**
   * @param aclIpSpace The {@link AclIpSpace} to dereference
   * @return An {@link AclIpSpace} identical to the original but with all uses of {@link
   *     IpSpaceReference} replaced with the dereferenced {@link IpSpace} they represent
   * @throws CircularReferenceException if original {@link AclIpSpace} points to a cyclical
   *     reference.
   * @throws UndefinedReferenceException if original {@link AclIpSpace} points to an undefined
   *     reference.
   */
  @Override
  public IpSpace visitAclIpSpace(AclIpSpace aclIpSpace)
      throws CircularReferenceException, UndefinedReferenceException {
    AclIpSpace.Builder sanitizedSpace = AclIpSpace.builder();
    for (AclIpSpaceLine line : aclIpSpace.getLines()) {
      IpSpace ipSpace = line.getIpSpace().accept(this);
      sanitizedSpace.thenAction(line.getAction(), ipSpace);
    }
    // No cycles/undefined references in this AclIpSpace. Return reference-free version.
    return sanitizedSpace.build();
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
   * @param ipSpaceReference {@link IpSpaceReference} to dereference
   * @return The dereferenced {@link IpSpace} referenced by the given {@link IpSpaceReference}
   * @throws CircularReferenceException if the given {@link IpSpaceReference} points to a cyclical
   *     reference.
   * @throws UndefinedReferenceException if the given {@link IpSpaceReference} points to an
   *     undefined reference.
   */
  @Override
  public IpSpace visitIpSpaceReference(IpSpaceReference ipSpaceReference)
      throws CircularReferenceException, UndefinedReferenceException {
    String name = ipSpaceReference.getName();
    if (!_referencedIpSpaces.add(name)) {
      // This is a reference to an already-referenced IP space. Line is in a cycle.
      throw new CircularReferenceException(
          String.format(
              "Cannot dereference IpSpaceReference to IP space %s because it is a circular"
                  + " reference.",
              name));
    }
    IpSpace referenced = _namedIpSpaces.get(name);
    if (referenced == null) {
      // This reference is to an undefined IP space.
      throw new UndefinedReferenceException(
          String.format(
              "Cannot dereference IpSpaceReference to IP space %s because there is no such IP"
                  + " space.",
              name));
    }
    // Current IpSpace references another valid and as yet unreferenced IP space. Visit that.
    IpSpace dereferenced = referenced.accept(this);
    _referencedIpSpaces.remove(name);
    return dereferenced;
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

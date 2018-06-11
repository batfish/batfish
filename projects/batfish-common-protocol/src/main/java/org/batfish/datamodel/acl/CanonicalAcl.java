package org.batfish.datamodel.acl;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import org.batfish.datamodel.IpAccessList;

/** Represents an ACL with all its dependencies for the purpose of detecting identical ACLs. */
public final class CanonicalAcl {

  private final IpAccessList _sanitizedAcl;
  private final ImmutableSortedMap<String, IpAccessList> _dependencies;
  private final int _hashCode;
  private final Set<Integer> _linesInCycles;
  private final Set<Integer> _linesWithUndefinedReferences;
  private final IpAccessList _acl;

  /**
   * @param sanitizedAcl {@link IpAccessList} represented by this CanonicalAcl with lines containing
   *     undefined refs and cycles sanitized to have match condition {@link FalseExpr}
   * @param acl {@link IpAccessList} represented by this CanonicalAcl
   * @param dependencies Map of names to {@link IpAccessList}s of ACLs upon which this ACL depends
   * @param linesWithUndefinedRefs Set of line numbers of lines that refer to undefined ACLs
   * @param linesInCycles Set of line numbers of lines that make circular references to another ACL
   */
  public CanonicalAcl(
      IpAccessList sanitizedAcl,
      IpAccessList acl,
      Map<String, IpAccessList> dependencies,
      Set<Integer> linesWithUndefinedRefs,
      Set<Integer> linesInCycles) {
    _acl = acl;
    _sanitizedAcl = sanitizedAcl;
    _linesWithUndefinedReferences = ImmutableSet.copyOf(linesWithUndefinedRefs);
    _linesInCycles = ImmutableSet.copyOf(linesInCycles);

    // _dependencies is a map of aclName to ACL all ACLs it upon which this ACL depends
    _dependencies = ImmutableSortedMap.copyOf(dependencies);

    // Build hashcode. This hashcode will match another CanonicalAcl hashcode if that CanonicalAcl's
    // _acl and dependencies all match this one syntactically. Ignores the acl name (though
    // not the dependencies' names, since the ACL text will refer to those).
    // Ignores other fields (_sanitizedAcl, _linesInCycles, _linesWithUndefinedReferences) because
    // those fields are all determined by _acl and _dependencies.
    Map<Integer, IpAccessList> relatedAclsHashCodeMap = new TreeMap<>();
    for (IpAccessList ipAccessList : dependencies.values()) {
      relatedAclsHashCodeMap.put(ipAccessList.getLines().hashCode(), ipAccessList);
    }
    _hashCode = _acl.getLines().hashCode() + relatedAclsHashCodeMap.hashCode();
  }

  /** @return The sanitized version of the ACL represented by this {@link CanonicalAcl} */
  public IpAccessList getSanitizedAcl() {
    return _sanitizedAcl;
  }

  public String getAclName() {
    return _sanitizedAcl.getName();
  }

  /**
   * @return Map of names to {@link IpAccessList} objects containing main ACL and its dependencies.
   */
  public NavigableMap<String, IpAccessList> getDependencies() {
    return _dependencies;
  }

  /**
   * @return Original version of this ACL, with no modifications to sanitize cycles/undefined refs.
   */
  public IpAccessList getOriginalAcl() {
    return _acl;
  }

  public boolean inCycle(int lineNum) {
    return _linesInCycles.contains(lineNum);
  }

  public boolean hasUndefinedRef(int lineNum) {
    return _linesWithUndefinedReferences.contains(lineNum);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof CanonicalAcl) || hashCode() != o.hashCode()) {
      return false;
    }
    CanonicalAcl otherAcl = (CanonicalAcl) o;
    return _acl.equals(otherAcl._acl) && _dependencies.equals(otherAcl._dependencies);
  }

  @Override
  public int hashCode() {
    return _hashCode;
  }
}

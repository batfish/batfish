package org.batfish.datamodel.acl;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.IpAccessList;

/** Represents an ACL with all its dependencies for the purpose of detecting identical ACLs. */
public final class CanonicalAcl {

  private final IpAccessList _sanitizedAcl;
  private final ImmutableSortedMap<String, IpAccessList> _dependencies;
  private final ImmutableSortedMap<String, Interface> _interfaces;
  private final Set<Integer> _linesInCycles;
  private final Set<Integer> _linesWithUndefinedReferences;
  private final IpAccessList _acl;
  private int _hashCode;

  /**
   * @param sanitizedAcl {@link IpAccessList} represented by this CanonicalAcl with lines containing
   *     undefined refs and cycles sanitized to have match condition {@link FalseExpr}
   * @param acl {@link IpAccessList} represented by this CanonicalAcl
   * @param dependencies Map of names to {@link IpAccessList}s of ACLs upon which this ACL depends
   * @param interfaces Map of interface names to {@link Interface}s of all interfaces referenced by
   *     this ACL
   * @param linesWithUndefinedRefs Set of line numbers of lines that refer to undefined ACLs
   * @param linesInCycles Set of line numbers of lines that make circular references to another ACL
   */
  public CanonicalAcl(
      IpAccessList sanitizedAcl,
      IpAccessList acl,
      Map<String, IpAccessList> dependencies,
      Map<String, Interface> interfaces,
      Set<Integer> linesWithUndefinedRefs,
      Set<Integer> linesInCycles) {
    _acl = acl;
    _sanitizedAcl = sanitizedAcl;
    _linesWithUndefinedReferences = ImmutableSet.copyOf(linesWithUndefinedRefs);
    _linesInCycles = ImmutableSet.copyOf(linesInCycles);

    // ACL and interface dependencies
    _dependencies = ImmutableSortedMap.copyOf(dependencies);
    _interfaces = ImmutableSortedMap.copyOf(interfaces);
  }

  /** @return The sanitized version of the ACL represented by this {@link CanonicalAcl} */
  public IpAccessList getSanitizedAcl() {
    return _sanitizedAcl;
  }

  public String getAclName() {
    return _sanitizedAcl.getName();
  }

  /**
   * @return Map of names to {@link IpAccessList} objects representing ACLs referenced by this ACL.
   */
  public NavigableMap<String, IpAccessList> getDependencies() {
    return _dependencies;
  }

  /** @return Map of names to {@link Interface} objects referenced by this ACL. */
  public NavigableMap<String, Interface> getInterfaces() {
    return _interfaces;
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
    if (hashCode() != o.hashCode()) {
      return false;
    }
    CanonicalAcl otherAcl = (CanonicalAcl) o;
    return _acl.equals(otherAcl._acl)
        && _dependencies.equals(otherAcl._dependencies)
        && _interfaces.equals(otherAcl._interfaces);
  }

  @Override
  public int hashCode() {
    if (_hashCode == 0) {
      _hashCode = _acl.getLines().hashCode() + _interfaces.hashCode();
      for (IpAccessList dependency : _dependencies.values()) {
        _hashCode += dependency.getLines().hashCode();
      }
    }
    return _hashCode;
  }
}

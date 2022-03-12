package org.batfish.datamodel.acl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.batfish.datamodel.IpAccessList;

/** Represents an ACL with all its dependencies for the purpose of detecting identical ACLs. */
public final class CanonicalAcl {

  private final IpAccessList _sanitizedAcl;
  private final Map<String, IpAccessList> _dependencies;
  private final Set<String> _interfaces;
  private final Set<Integer> _linesInCycles;
  private final Set<Integer> _linesWithUndefinedReferences;
  private final IpAccessList _acl;
  private int _hashCode;

  /**
   * @param sanitizedAcl {@link IpAccessList} represented by this CanonicalAcl with lines containing
   *     undefined refs and cycles sanitized to have match condition {@link FalseExpr}
   * @param acl {@link IpAccessList} represented by this CanonicalAcl
   * @param dependencies Map of names to {@link IpAccessList}s of ACLs upon which this ACL depends
   * @param interfaces Set of interface names of all interfaces referenced by this ACL
   * @param linesWithUndefinedRefs Set of line numbers of lines that refer to undefined ACLs
   * @param linesInCycles Set of line numbers of lines that make circular references to another ACL
   */
  public CanonicalAcl(
      IpAccessList sanitizedAcl,
      IpAccessList acl,
      Map<String, IpAccessList> dependencies,
      Set<String> interfaces,
      Set<Integer> linesWithUndefinedRefs,
      Set<Integer> linesInCycles) {
    _acl = acl;
    _sanitizedAcl = sanitizedAcl;
    _linesWithUndefinedReferences = ImmutableSet.copyOf(linesWithUndefinedRefs);
    _linesInCycles = ImmutableSet.copyOf(linesInCycles);

    // ACL and interface dependencies
    _dependencies = ImmutableMap.copyOf(dependencies);
    _interfaces = ImmutableSet.copyOf(interfaces);
  }

  /**
   * @return The sanitized version of the ACL represented by this {@link CanonicalAcl}
   */
  public IpAccessList getSanitizedAcl() {
    return _sanitizedAcl;
  }

  public String getAclName() {
    return _sanitizedAcl.getName();
  }

  /**
   * @return Map of names to {@link IpAccessList} objects representing ACLs referenced by this ACL.
   */
  public Map<String, IpAccessList> getDependencies() {
    return _dependencies;
  }

  /**
   * @return Set of interface names referenced by this ACL.
   */
  public Set<String> getInterfaces() {
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
    } else if (!(o instanceof CanonicalAcl)) {
      return false;
    } else if (hashCode() != o.hashCode()) {
      return false;
    }
    CanonicalAcl otherAcl = (CanonicalAcl) o;
    // Field equality, except that for ACLs only the lines are considered.
    return _acl.getLines().equals(otherAcl._acl.getLines())
        && _dependencies.equals(otherAcl._dependencies)
        && _interfaces.equals(otherAcl._interfaces)
        && _sanitizedAcl.getLines().equals(otherAcl._sanitizedAcl.getLines())
        && _linesWithUndefinedReferences.equals(otherAcl._linesWithUndefinedReferences)
        && _linesInCycles.equals(otherAcl._linesInCycles);
  }

  @Override
  public int hashCode() {
    int hashCode = _hashCode;
    if (hashCode == 0) {
      // cache hashCode
      hashCode =
          Objects.hash(
              _acl.getLines(),
              _sanitizedAcl.getLines(),
              _interfaces,
              _linesInCycles,
              _linesWithUndefinedReferences,
              _dependencies.entrySet().stream()
                  .collect(Collectors.toMap(Entry::getKey, e -> e.getValue().getLines())));
      _hashCode = hashCode;
    }
    return hashCode;
  }
}

package org.batfish.datamodel.acl;

import com.google.common.collect.ImmutableSortedMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import org.batfish.datamodel.IpAccessList;

/** Represents an ACL with all its dependencies for the purpose of detecting identical ACLs. */
public final class CanonicalAcl {

  private final IpAccessList _acl;
  private final ImmutableSortedMap<String, IpAccessList> _dependencies;
  private final int _hashCode;
  private final IpAccessList _originalAcl;

  /**
   * @param acl {@link IpAccessList} represented by this CanonicalAcl
   * @param dependencies Map of names to {@link IpAccessList}s of ACLs upon which this ACL depends
   */
  public CanonicalAcl(
      IpAccessList acl, IpAccessList originalAcl, Map<String, IpAccessList> dependencies) {
    _acl = acl;
    _originalAcl = originalAcl;

    // _dependencies is a map of aclName to ACL all ACLs it upon which this ACL depends
    _dependencies = ImmutableSortedMap.copyOf(dependencies);

    // Build hashcode. This hashcode will match another CanonicalAcl hashcode if that CanonicalAcl's
    // _acl and dependencies all match this one syntactically. Ignores the acl name (though not the
    // dependencies' names, since the ACL text will refer to those).
    Map<Integer, IpAccessList> relatedAclsHashCodeMap = new TreeMap<>();
    for (IpAccessList ipAccessList : dependencies.values()) {
      relatedAclsHashCodeMap.put(ipAccessList.getLines().hashCode(), ipAccessList);
    }
    _hashCode = _acl.getLines().hashCode() + relatedAclsHashCodeMap.hashCode();
  }

  /** @return The ACL represented by this {@link CanonicalAcl} */
  public IpAccessList getAcl() {
    return _acl;
  }

  public String getAclName() {
    return _acl.getName();
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
  public IpAccessList getOriginal() {
    return _originalAcl;
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

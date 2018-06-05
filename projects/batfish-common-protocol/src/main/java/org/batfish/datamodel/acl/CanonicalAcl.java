package org.batfish.datamodel.acl;

import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import org.batfish.datamodel.IpAccessList;

/** Represents an ACL with all its dependencies for the purpose of detecting identical ACLs. */
public final class CanonicalAcl {

  private final String _reprAclName;
  private final String _reprHostname;
  private final IpAccessList _acl;
  private final int _hashCode;
  private final SortedMap<String, Set<String>> _sources = new TreeMap<>();

  /**
   * @param aclName Name of ACL represented by this CanonicalAcl
   * @param acl {@link IpAccessList} represented by this CanonicalAcl
   * @param dependencies Map of names to {@link IpAccessList}s of ACLs upon which this ACL depends
   * @param hostname Name of node on which this ACL was initially found
   */
  public CanonicalAcl(
      String aclName, IpAccessList acl, Map<String, IpAccessList> dependencies, String hostname) {
    _acl = acl;
    _sources.computeIfAbsent(hostname, h -> new TreeSet<>()).add(aclName);
    _reprAclName = aclName;
    _reprHostname = hostname;

    // Build hashcode. This hashcode will match another CanonicalAcl hashcode if that CanonicalAcl's
    // _acl and dependencies all match this one syntactically. Ignores the acl name (though not the
    // dependencies' names, since the ACL text will refer to those).
    Map<Integer, IpAccessList> relatedAclsHashCodeMap = new TreeMap<>();
    for (IpAccessList ipAccessList : dependencies.values()) {
      relatedAclsHashCodeMap.put(ipAccessList.getLines().hashCode(), ipAccessList);
    }
    _hashCode = _acl.getLines().hashCode() + relatedAclsHashCodeMap.hashCode();
  }

  /**
   * Adds a hostname/ACL pair to the map of ACLs that this {@link CanonicalAcl} represents.
   *
   * @param hostname Name of node containing a copy of this ACL
   * @param aclName Name of ACL identical to this ACL
   */
  public void addSource(String hostname, String aclName) {
    _sources.computeIfAbsent(hostname, h -> new TreeSet<>()).add(aclName);
  }

  /** @return {@link IpAccessList} of the ACL represented by this {@link CanonicalAcl} */
  public IpAccessList getAcl() {
    return _acl;
  }

  /**
   * Returns the hostname/ACL pairs represented by this {@link CanonicalAcl}.
   *
   * @return Mapping of hostnames to ACL names of the identical ACLs represented by this {@link
   *     CanonicalAcl}.
   */
  public Map<String, Set<String>> getSources() {
    return _sources;
  }

  /** @return Name of any one of the identical ACLs represented by this {@link CanonicalAcl}. */
  public String getRepresentativeAclName() {
    return _reprAclName;
  }

  /**
   * @return Name of the node on which the representative ACL given by {@link
   *     CanonicalAcl#getRepresentativeAclName()} appears.
   */
  public String getRepresentativeHostname() {
    return _reprHostname;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof CanonicalAcl)) {
      return false;
    }
    return hashCode() == o.hashCode();
  }

  @Override
  public int hashCode() {
    return _hashCode;
  }
}

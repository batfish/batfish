package org.batfish.datamodel.acl;

import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import org.batfish.datamodel.IpAccessList;

public class CanonicalAcl {

  // Mapping of ACL name to ACL; contains this ACL and any ACLs it depends on
  private final String _reprAclName;
  private final String _reprHostname;
  private final IpAccessList _acl;
  private final int _hashCode;
  private final SortedMap<String, Set<String>> _sources = new TreeMap<>();

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

  public void addSource(String hostname, String aclName) {
    _sources.computeIfAbsent(hostname, h -> new TreeSet<>()).add(aclName);
  }

  public IpAccessList getAcl() {
    return _acl;
  }

  public Map<String, Set<String>> getSources() {
    return _sources;
  }

  public String getRepresentativeAclName() {
    return _reprAclName;
  }

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

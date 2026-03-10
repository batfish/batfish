package org.batfish.datamodel.acl;

import java.util.Comparator;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.batfish.datamodel.IpAccessList;

/** This class identifies a particular line within an ACL by its index. */
public class AclLineIndex implements Comparable<AclLineIndex> {

  private @Nonnull IpAccessList _acl;
  private int _index;

  public AclLineIndex(@Nonnull IpAccessList acl, int index) {
    _acl = acl;
    _index = index;
  }

  @Override
  public int compareTo(AclLineIndex lineIndex) {
    if (this == lineIndex) {
      return 0;
    }
    return Comparator.comparing((AclLineIndex index) -> index.getAcl().getName())
        .thenComparingInt(AclLineIndex::getIndex)
        .compare(this, lineIndex);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(getClass() == o.getClass())) {
      return false;
    }
    AclLineIndex lineIndex = (AclLineIndex) o;
    return _acl.equals(lineIndex._acl) && _index == lineIndex._index;
  }

  public @Nonnull IpAccessList getAcl() {
    return _acl;
  }

  public int getIndex() {
    return _index;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_acl, _index);
  }
}

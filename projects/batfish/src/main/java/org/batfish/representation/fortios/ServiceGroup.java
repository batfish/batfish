package org.batfish.representation.fortios;

import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** FortiOS datamodel component containing firewall service group */
public final class ServiceGroup extends ServiceGroupMember implements Serializable {

  @Override
  @Nonnull
  public String getName() {
    return _name;
  }

  @Override
  public BatfishUUID getBatfishUUID() {
    return _uuid;
  }

  /**
   * Names of service members asssociated with this service group. Should be derived from {@link
   * this#getMemberUUIDs} when finishing building the VS model.
   */
  @Nullable
  public Set<String> getMember() {
    return _member;
  }

  /** Set of Batfish-internal UUIDs associated with member references. */
  @Nonnull
  public Set<BatfishUUID> getMemberUUIDs() {
    return _memberUuids;
  }

  @Override
  public void setName(String name) {
    _name = name;
  }

  public void setMember(Set<String> member) {
    _member = ImmutableSet.copyOf(member);
  }

  public ServiceGroup(String name, BatfishUUID uuid) {
    _name = name;
    _uuid = uuid;

    _memberUuids = new HashSet<>();
  }

  @Nonnull private String _name;
  @Nonnull private final BatfishUUID _uuid;
  @Nullable private Set<String> _member;
  @Nonnull private final Set<BatfishUUID> _memberUuids;
}

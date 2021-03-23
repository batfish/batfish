package org.batfish.representation.fortios;

import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.HeaderSpace;

/** FortiOS datamodel component containing firewall service group */
public final class ServiceGroup extends ServiceGroupMember implements Serializable {

  @Override
  public Stream<HeaderSpace> toHeaderSpaces(Map<String, ServiceGroupMember> serviceGroupMembers) {
    // Guaranteed once extraction is complete
    assert _member != null;

    return _member.stream()
        .flatMap(m -> serviceGroupMembers.get(m).toHeaderSpaces(serviceGroupMembers));
  }

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

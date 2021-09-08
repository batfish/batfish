package org.batfish.vendor.check_point_management;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ServiceGroup extends TypedManagementObject implements Service {
  @Override
  public <T> T accept(ServiceVisitor<T> visitor) {
    return visitor.visitServiceGroup(this);
  }

  @Override
  public boolean equals(Object o) {
    if (!baseEquals(o)) {
      return false;
    }
    ServiceGroup serviceGroup = (ServiceGroup) o;
    return _members.equals(serviceGroup._members);
  }

  @Override
  public int hashCode() {
    return Objects.hash(baseHashcode(), _members);
  }

  @Override
  public String toString() {
    return baseToStringHelper().add(PROP_MEMBERS, _members).toString();
  }

  @JsonCreator
  private static @Nonnull ServiceGroup create(
      @JsonProperty(PROP_NAME) @Nullable String name,
      @JsonProperty(PROP_MEMBERS) @Nullable List<Uid> members,
      @JsonProperty(PROP_UID) @Nullable Uid uid) {
    checkArgument(name != null, "Missing %s", PROP_NAME);
    checkArgument(members != null, "Missing %s", PROP_MEMBERS);
    checkArgument(uid != null, "Missing %s", PROP_UID);
    return new ServiceGroup(name, members, uid);
  }

  @VisibleForTesting
  public ServiceGroup(String name, List<Uid> members, Uid uid) {
    super(name, uid);
    _members = members;
  }

  private static final String PROP_MEMBERS = "members";

  @Nonnull private final List<Uid> _members;
}

package org.batfish.vendor.check_point_management;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * When assigned to {@code original-destination}, {@code original-service}, or {@code
 * original-source} field of a {@link NatRule}, indicates that any value shall be matched for that
 * field when applying the rule.
 */
public final class CpmiAnyObject extends TypedManagementObject implements Service, AddressSpace {

  @Override
  public <T> T accept(AddressSpaceVisitor<T> visitor) {
    return visitor.visitCpmiAnyObject(this);
  }

  @Override
  public <T> T accept(ServiceVisitor<T> visitor) {
    return visitor.visitCpmiAnyObject(this);
  }

  @Override
  public boolean equals(Object o) {
    return baseEquals(o);
  }

  @Override
  public int hashCode() {
    return baseHashcode();
  }

  @Override
  public String toString() {
    return baseToStringHelper().toString();
  }

  @VisibleForTesting
  public CpmiAnyObject(Uid uid) {
    super(NAME_ANY, uid);
  }

  private static final String NAME_ANY = "Any";

  @JsonCreator
  private static @Nonnull CpmiAnyObject create(@JsonProperty(PROP_UID) @Nullable Uid uid) {
    checkArgument(uid != null, "Missing %s", PROP_UID);
    return new CpmiAnyObject(uid);
  }
}

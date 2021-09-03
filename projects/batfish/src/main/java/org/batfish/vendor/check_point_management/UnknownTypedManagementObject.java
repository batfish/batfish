package org.batfish.vendor.check_point_management;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Catch-all for unknown and unhandled object types, so Jackson deserialization can work with object
 * types that aren't supported yet.
 */
public class UnknownTypedManagementObject extends TypedManagementObject {

  @Nonnull
  public String getType() {
    return _type;
  }

  @Override
  public boolean equals(Object o) {
    if (!baseEquals(o)) {
      return false;
    }
    UnknownTypedManagementObject that = (UnknownTypedManagementObject) o;
    return _type.equals(that._type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(baseHashcode(), _type);
  }

  @JsonCreator
  private static @Nonnull UnknownTypedManagementObject create(
      @JsonProperty(PROP_NAME) @Nullable String name,
      @JsonProperty(PROP_UID) @Nullable Uid uid,
      @JsonProperty(PROP_TYPE) @Nullable String type) {
    checkArgument(name != null, "Missing %s", PROP_NAME);
    checkArgument(uid != null, "Missing %s", PROP_UID);
    checkArgument(type != null, "Missing %s", PROP_TYPE);
    return new UnknownTypedManagementObject(name, uid, type);
  }

  @VisibleForTesting
  public UnknownTypedManagementObject(String name, Uid uid, String type) {
    super(name, uid);
    _type = type;
  }

  private static final String PROP_TYPE = "type";

  private final String _type;
}

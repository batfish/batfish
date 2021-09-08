package org.batfish.vendor.check_point_management;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ServiceIcmp extends TypedManagementObject implements Service {
  @Override
  public <T> T accept(ServiceVisitor<T> visitor) {
    return visitor.visitServiceIcmp(this);
  }

  /** Docs: A number with no fractional part (integer). */
  public @Nullable Integer getIcmpCode() {
    return _code;
  }

  /** Docs: A number with no fractional part (integer). */
  public int getIcmpType() {
    return _type;
  }

  @Override
  public boolean equals(Object o) {
    if (!baseEquals(o)) {
      return false;
    }
    ServiceIcmp other = (ServiceIcmp) o;
    return _type == other._type && Objects.equals(_code, other._code);
  }

  @Override
  public int hashCode() {
    return Objects.hash(baseHashcode(), _type, _code);
  }

  @Override
  public String toString() {
    return baseToStringHelper()
        .omitNullValues()
        .add(PROP_ICMP_TYPE, _type)
        .add(PROP_ICMP_CODE, _code)
        .toString();
  }

  @JsonCreator
  private static @Nonnull ServiceIcmp create(
      @JsonProperty(PROP_NAME) @Nullable String name,
      @JsonProperty(PROP_ICMP_TYPE) @Nullable Integer type,
      @JsonProperty(PROP_ICMP_CODE) @Nullable Integer code,
      @JsonProperty(PROP_UID) @Nullable Uid uid) {
    checkArgument(name != null, "Missing %s", PROP_NAME);
    checkArgument(type != null, "Missing %s", PROP_ICMP_TYPE);
    checkArgument(uid != null, "Missing %s", PROP_UID);
    return new ServiceIcmp(name, type, code, uid);
  }

  @VisibleForTesting
  public ServiceIcmp(String name, int type, @Nullable Integer code, Uid uid) {
    super(name, uid);
    _type = type;
    _code = code;
  }

  private static final String PROP_ICMP_CODE = "icmp-code";
  private static final String PROP_ICMP_TYPE = "icmp-type";

  private final int _type;
  private final @Nullable Integer _code;
}

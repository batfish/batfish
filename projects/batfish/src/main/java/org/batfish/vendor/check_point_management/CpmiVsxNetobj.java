package org.batfish.vendor.check_point_management;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** Data model for an object of type {@code CpmiVsxNetobj}. */
public final class CpmiVsxNetobj extends GatewayOrServer {

  @JsonCreator
  private static @Nonnull CpmiVsxNetobj create(
      @JsonProperty(PROP_INTERFACES) @Nullable List<Interface> interfaces,
      @JsonProperty(PROP_IPV4_ADDRESS) @Nullable Ip ipv4Address,
      @JsonProperty(PROP_NAME) @Nullable String name,
      @JsonProperty(PROP_POLICY) @Nullable GatewayOrServerPolicy policy,
      @JsonProperty(PROP_UID) @Nullable Uid uid) {
    checkArgument(interfaces != null, "Missing %s", PROP_INTERFACES);
    checkArgument(name != null, "Missing %s", PROP_NAME);
    checkArgument(policy != null, "Missing %s", PROP_POLICY);
    checkArgument(uid != null, "Missing %s", PROP_UID);
    return new CpmiVsxNetobj(ipv4Address, name, interfaces, policy, uid);
  }

  @VisibleForTesting
  CpmiVsxNetobj(
      @Nullable Ip ipv4Address,
      String name,
      List<Interface> interfaces,
      GatewayOrServerPolicy policy,
      Uid uid) {
    super(ipv4Address, name, interfaces, policy, uid);
  }

  @Override
  public boolean equals(Object obj) {
    return baseEquals(obj);
  }

  @Override
  public int hashCode() {
    return baseHashcode();
  }

  @Override
  public String toString() {
    return baseToStringHelper().toString();
  }
}

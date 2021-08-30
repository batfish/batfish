package org.batfish.vendor.check_point_management;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** Data model for an object of type {@code CpmiVsxClusterNetobj}. */
public final class CpmiVsxClusterNetobj extends Cluster {

  @JsonCreator
  private static @Nonnull CpmiVsxClusterNetobj create(
      @JsonProperty(PROP_CLUSTER_MEMBER_NAMES) @Nullable List<String> clusterMemberNames,
      @JsonProperty(PROP_INTERFACES) @Nullable List<Interface> interfaces,
      @JsonProperty(PROP_IPV4_ADDRESS) @Nullable Ip ipv4Address,
      @JsonProperty(PROP_NAME) @Nullable String name,
      @JsonProperty(PROP_POLICY) @Nullable GatewayOrServerPolicy policy,
      @JsonProperty(PROP_UID) @Nullable Uid uid) {
    checkArgument(clusterMemberNames != null, "Missing %s", PROP_CLUSTER_MEMBER_NAMES);
    checkArgument(interfaces != null, "Missing %s", PROP_INTERFACES);
    checkArgument(ipv4Address != null, "Missing %s", PROP_IPV4_ADDRESS);
    checkArgument(name != null, "Missing %s", PROP_NAME);
    checkArgument(policy != null, "Missing %s", PROP_POLICY);
    checkArgument(uid != null, "Missing %s", PROP_UID);
    return new CpmiVsxClusterNetobj(clusterMemberNames, ipv4Address, name, interfaces, policy, uid);
  }

  @VisibleForTesting
  CpmiVsxClusterNetobj(
      List<String> clusterMemberNames,
      Ip ipv4Address,
      String name,
      List<Interface> interfaces,
      GatewayOrServerPolicy policy,
      Uid uid) {
    super(clusterMemberNames, ipv4Address, name, interfaces, policy, uid);
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

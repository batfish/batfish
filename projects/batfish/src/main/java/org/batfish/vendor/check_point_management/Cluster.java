package org.batfish.vendor.check_point_management;

import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;

/** A gateway or server cluster with members. */
public abstract class Cluster extends GatewayOrServer {

  @Nonnull
  public List<String> getClusterMemberNames() {
    return _clusterMemberNames;
  }

  protected Cluster(List<String> clusterMemberNames, Ip ipv4Address, String name, Uid uid) {
    super(ipv4Address, name, uid);
    _clusterMemberNames = clusterMemberNames;
  }

  @Override
  protected boolean baseEquals(Object o) {
    if (!super.baseEquals(o)) {
      return false;
    }
    Cluster that = (Cluster) o;
    return _clusterMemberNames.equals(that._clusterMemberNames);
  }

  @Override
  protected int baseHashcode() {
    return Objects.hash(super.baseHashcode(), _clusterMemberNames);
  }

  @Override
  protected @Nonnull ToStringHelper baseToStringHelper() {
    return super.baseToStringHelper().add(PROP_CLUSTER_MEMBER_NAMES, _clusterMemberNames);
  }

  protected static final String PROP_CLUSTER_MEMBER_NAMES = "cluster-member-names";

  private final @Nonnull List<String> _clusterMemberNames;
}

package org.batfish.datamodel.collections;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;

@ParametersAreNonnullByDefault
public class IpEdge implements Serializable, Comparable<IpEdge> {
  private static final String PROP_IP1 = "ip1";
  private static final String PROP_IP2 = "ip2";
  private static final String PROP_NODE1 = "node1";
  private static final String PROP_NODE2 = "node2";

  private static final long serialVersionUID = 1L;

  @Nonnull private final String _node1;
  @Nonnull private final Ip _ip1;
  @Nonnull private final String _node2;
  @Nonnull private final Ip _ip2;

  public IpEdge(String node1, Ip ip1, String node2, Ip ip2) {
    _node1 = node1;
    _ip1 = ip1;
    _node2 = node2;
    _ip2 = ip2;
  }

  @JsonCreator
  private static IpEdge jsonCreator(
      @Nullable @JsonProperty(PROP_NODE1) String node1,
      @Nullable @JsonProperty(PROP_IP1) Ip ip1,
      @Nullable @JsonProperty(PROP_NODE2) String node2,
      @Nullable @JsonProperty(PROP_IP2) Ip ip2) {
    checkArgument(node1 != null, "Missing %s", PROP_NODE1);
    checkArgument(node2 != null, "Missing %s", PROP_NODE2);
    checkArgument(ip1 != null, "Missing %s", PROP_IP1);
    checkArgument(ip2 != null, "Missing %s", PROP_IP2);
    return new IpEdge(node1, ip1, node2, ip2);
  }

  @JsonProperty(PROP_IP1)
  @Nonnull
  public Ip getIp1() {
    return _ip1;
  }

  @JsonProperty(PROP_IP2)
  @Nonnull
  public Ip getIp2() {
    return _ip2;
  }

  @JsonProperty(PROP_NODE1)
  @Nonnull
  public String getNode1() {
    return _node1;
  }

  @JsonProperty(PROP_NODE2)
  @Nonnull
  public String getNode2() {
    return _node2;
  }

  @Override
  public String toString() {
    return "<" + getNode1() + ":" + getIp1() + ", " + getNode2() + ":" + getIp2() + ">";
  }

  @Override
  public int compareTo(IpEdge o) {
    return Comparator.comparing(IpEdge::getNode1)
        .thenComparing(IpEdge::getIp1)
        .thenComparing(IpEdge::getNode2)
        .thenComparing(IpEdge::getIp2)
        .compare(this, o);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof IpEdge)) {
      return false;
    }
    IpEdge e = (IpEdge) o;
    return _node1.equals(e._node1)
        && _ip1.equals(e._ip1)
        && _node2.equals(e._node2)
        && _ip2.equals(e._ip2);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_node1, _ip1, _node2, _ip2);
  }
}

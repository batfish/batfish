package org.batfish.datamodel.ospf;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Comparator;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkConfigurations;

/** Represents one side of an OSPF session, where the session is represented by {@link OspfEdge}. */
@ParametersAreNonnullByDefault
public final class OspfNode implements Comparable<OspfNode> {

  private static final String PROP_NODE = "node";
  private static final String PROP_INTERFACE = "interface";
  private static final String PROP_LOCAL_IP = "localIp";

  @Nonnull private final String _node;
  @Nonnull private final String _interfaceName;
  @Nonnull private final Ip _localIp;

  public OspfNode(String node, String interfaceName, Ip localIp) {
    _node = node;
    _interfaceName = interfaceName;
    _localIp = localIp;
  }

  @JsonCreator
  private static @Nonnull OspfNode create(
      @Nullable @JsonProperty(PROP_NODE) String node,
      @Nullable @JsonProperty(PROP_INTERFACE) String interfaceName,
      @Nullable @JsonProperty(PROP_LOCAL_IP) Ip localIp) {
    checkArgument(node != null, "Missing %s", PROP_NODE);
    checkArgument(interfaceName != null, "Missing %s", PROP_INTERFACE);
    checkArgument(localIp != null, "Missing %s", PROP_LOCAL_IP);
    return new OspfNode(node, interfaceName, localIp);
  }

  @JsonProperty(PROP_NODE)
  public @Nonnull String getNode() {
    return _node;
  }

  @JsonIgnore
  public @Nullable Interface getInterface(@Nonnull NetworkConfigurations nc) {
    return nc.getInterface(_node, _interfaceName).orElse(null);
  }

  @JsonProperty(PROP_INTERFACE)
  public @Nonnull String getInterfaceName() {
    return _interfaceName;
  }

  @JsonProperty(PROP_LOCAL_IP)
  public @Nonnull Ip getLocalIp() {
    return _localIp;
  }

  @Override
  public int compareTo(OspfNode o) {
    return Comparator.comparing(OspfNode::getNode)
        .thenComparing(OspfNode::getInterfaceName)
        .thenComparing(OspfNode::getLocalIp)
        .compare(this, o);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof OspfNode)) {
      return false;
    }
    OspfNode rhs = (OspfNode) o;
    return _node.equals(rhs._node)
        && _interfaceName.equals(rhs._interfaceName)
        && _localIp.equals(rhs._localIp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_node, _interfaceName, _localIp);
  }

  @Override
  public String toString() {
    return toStringHelper(getClass())
        .add(PROP_NODE, _node)
        .add(PROP_INTERFACE, _interfaceName)
        .add(PROP_LOCAL_IP, _localIp)
        .toString();
  }
}

package org.batfish.datamodel.vxlan;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** A VXLAN endpoint. */
public final class VxlanNode {

  private static final String PROP_HOSTNAME = "hostname";
  private static final String PROP_VNI = "vni";
  private static final String PROP_VNI_LAYER = "vniLayer";

  public static final class Builder {

    private @Nullable String _hostname;
    private @Nullable Integer _vni;
    private @Nullable VniLayer _vniLayer;

    private Builder() {}

    public @Nonnull VxlanNode build() {
      checkArgument(_hostname != null, "Missing %s", "hostname");
      checkArgument(_vni != null, "Missing %s", "vni");
      checkArgument(_vniLayer != null, "Missing %s", "vni layer");
      return new VxlanNode(_hostname, _vni, _vniLayer);
    }

    public @Nonnull Builder setHostname(String hostname) {
      _hostname = hostname;
      return this;
    }

    public @Nonnull Builder setVni(int vni) {
      _vni = vni;
      return this;
    }

    public @Nonnull Builder setVniLayer(VniLayer vniLayer) {
      _vniLayer = vniLayer;
      return this;
    }
  }

  @JsonCreator
  private static @Nonnull VxlanNode create(
      @JsonProperty(PROP_HOSTNAME) @Nullable String hostname,
      @JsonProperty(PROP_VNI) @Nullable Integer vni,
      @JsonProperty(PROP_VNI_LAYER) @Nullable VniLayer vniLayer) {
    checkArgument(hostname != null, "Missing %s", PROP_HOSTNAME);
    checkArgument(vni != null, "Missing %s", PROP_VNI);
    checkArgument(vniLayer != null, "Missing %s", PROP_VNI_LAYER);
    return new VxlanNode(hostname, vni, vniLayer);
  }

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  private final @Nonnull String _hostname;
  private final int _vni;
  private final @Nonnull VniLayer _vniLayer;

  public VxlanNode(String hostname, int vni, VniLayer vniLayer) {
    _hostname = hostname;
    _vni = vni;
    _vniLayer = vniLayer;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof VxlanNode)) {
      return false;
    }
    VxlanNode rhs = (VxlanNode) obj;
    return _hostname.equals(rhs._hostname) && _vni == rhs._vni && _vniLayer == rhs._vniLayer;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_hostname, _vni, _vniLayer.ordinal());
  }

  @Override
  public String toString() {
    return toStringHelper(getClass())
        .add("hostname", _hostname)
        .add("vni", _vni)
        .add("vniLayer", _vniLayer)
        .toString();
  }

  /** Hostname of the endpoint. */
  @JsonProperty(PROP_HOSTNAME)
  public @Nonnull String getHostname() {
    return _hostname;
  }

  /** VNI number of the endpoint. */
  @JsonProperty(PROP_VNI)
  public int getVni() {
    return _vni;
  }

  /** VNI layer for the VNI number on the endpoint. */
  @JsonProperty(PROP_VNI_LAYER)
  public @Nonnull VniLayer getVniLayer() {
    return _vniLayer;
  }
}

package org.batfish.datamodel.topology;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.topology.bridge_domain.edge.L1ToL2;
import org.batfish.common.topology.bridge_domain.edge.L2ToL1;
import org.batfish.datamodel.Interface;

/** Configuration for the layer-2 aspect of an {@link Interface}. */
public final class Layer2Settings implements Serializable {

  public static @Nonnull Layer2Settings of(
      String l1Interface, L2ToL1 l2ToL1, L1ToL2 l1ToL2, Set<Layer2BridgeSettings> bridgeSettings) {
    checkArgument(!bridgeSettings.isEmpty(), "Must attach to at least one bridge domain");
    return new Layer2Settings(l1Interface, l2ToL1, l1ToL2, bridgeSettings);
  }

  /**
   * The filter/transformation to apply when traversing from the layer-1 interface to this layer-2
   * interface.
   */
  public @Nonnull L1ToL2 getFromL1() {
    return _fromL1;
  }

  /**
   * The filter/transformation to apply when traversing from this interface to the layer-1 layer-2
   * interface.
   */
  public @Nonnull L2ToL1 getToL1() {
    return _toL1;
  }

  /** The name of the layer-1 interface to which this interface is attached (possibly itself). */
  public @Nonnull String getL1Interface() {
    return _l1Interface;
  }

  /**
   * All bridge settings for this interface.
   *
   * <p>Usually returns a singleton for the one bridge-domain to which this interface connects.
   * Juniper devices may attach a trunk-mode interface to multiple bridge domains, though these
   * connections must be parititioned by input tag space.
   */
  public @Nonnull Set<Layer2BridgeSettings> getBridgeSettings() {
    return _bridgeSettings;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof Layer2Settings)) {
      return false;
    }
    Layer2Settings that = (Layer2Settings) o;
    return _l1Interface.equals(that._l1Interface)
        && _toL1.equals(that._toL1)
        && _fromL1.equals(that._fromL1)
        && _bridgeSettings.equals(that._bridgeSettings);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_l1Interface, _toL1, _fromL1, _bridgeSettings);
  }

  private Layer2Settings(
      String l1Interface, L2ToL1 toL1, L1ToL2 fromL1, Set<Layer2BridgeSettings> bridgeSettings) {
    _l1Interface = l1Interface;
    _toL1 = toL1;
    _fromL1 = fromL1;
    _bridgeSettings = bridgeSettings;
  }

  private final @Nonnull String _l1Interface;
  private final @Nonnull L2ToL1 _toL1;
  private final @Nonnull L1ToL2 _fromL1;
  private final @Nonnull Set<Layer2BridgeSettings> _bridgeSettings;
}

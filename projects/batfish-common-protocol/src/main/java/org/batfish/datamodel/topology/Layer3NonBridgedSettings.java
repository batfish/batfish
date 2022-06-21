package org.batfish.datamodel.topology;

import static org.batfish.common.topology.bridge_domain.edge.L1ToL3.l1ToL3NonBridged;
import static org.batfish.common.topology.bridge_domain.edge.L3ToL1.l3NonBridgedToL1;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.topology.bridge_domain.edge.L1ToL3;
import org.batfish.common.topology.bridge_domain.edge.L3ToL1;

/** Configuration for a non-bridged layer-3 interface, i.e. physical or subinterface thereof. */
public final class Layer3NonBridgedSettings implements Layer3Settings {

  public static @Nonnull Layer3NonBridgedSettings of(
      String l1Interface, L1ToL3 fromL1, L3ToL1 toL1) {
    return new Layer3NonBridgedSettings(l1Interface, fromL1, toL1);
  }

  /**
   * Helper for generating {@link Layer3NonBridgedSettings} for a layer-3 interface without
   * encapsulation.
   */
  public static @Nonnull Layer3NonBridgedSettings noEncapsulation(String l1Interface) {
    return new Layer3NonBridgedSettings(
        l1Interface, l1ToL3NonBridged(null), l3NonBridgedToL1(null));
  }

  /**
   * Helper for generating {@link Layer3NonBridgedSettings} for a layer-3 interface accepting
   * traffic with the given tag.
   */
  public static @Nonnull Layer3NonBridgedSettings encapsulation(String l1Interface, int tag) {
    return Layer3NonBridgedSettings.of(
        l1Interface, L1ToL3.l1ToL3NonBridged(tag), L3ToL1.l3NonBridgedToL1(tag));
  }

  @Override
  public <T> T accept(Layer3SettingsVisitor<T> visitor) {
    return visitor.visitLayer3NonBridgedSettings(this);
  }

  @Override
  public <T, U> T accept(Layer3SettingsArgVisitor<T, U> visitor, U arg) {
    return visitor.visitLayer3NonBridgedSettings(this, arg);
  }

  /** The name of the layer-1 interface to which this interface is attached (possibly itself). */
  public @Nonnull String getL1Interface() {
    return _l1Interface;
  }

  /**
   * The filter/transformation to apply when traversing from the layer-1 interface to this layer-3
   * interface.
   */
  public @Nonnull L1ToL3 getFromL1() {
    return _fromL1;
  }

  /**
   * The filter/transformation to apply when traversing from this layer-3 interface to the layer-1
   * interface.
   */
  public @Nonnull L3ToL1 getToL1() {
    return _toL1;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof Layer3NonBridgedSettings)) {
      return false;
    }
    Layer3NonBridgedSettings that = (Layer3NonBridgedSettings) o;
    return _l1Interface.equals(that._l1Interface)
        && _fromL1.equals(that._fromL1)
        && _toL1.equals(that._toL1);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_l1Interface, _fromL1, _toL1);
  }

  private Layer3NonBridgedSettings(String l1Interface, L1ToL3 fromL1, L3ToL1 toL1) {
    _l1Interface = l1Interface;
    _fromL1 = fromL1;
    _toL1 = toL1;
  }

  private final @Nonnull String _l1Interface;
  private final @Nonnull L1ToL3 _fromL1;
  private final @Nonnull L3ToL1 _toL1;
}

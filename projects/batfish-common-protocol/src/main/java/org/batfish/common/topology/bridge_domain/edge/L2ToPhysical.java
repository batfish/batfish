package org.batfish.common.topology.bridge_domain.edge;

import static org.batfish.common.topology.bridge_domain.function.StateFunctions.clearVlanId;
import static org.batfish.common.topology.bridge_domain.function.StateFunctions.identity;
import static org.batfish.common.topology.bridge_domain.function.StateFunctions.pushTag;
import static org.batfish.common.topology.bridge_domain.function.StateFunctions.pushVlanId;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.topology.bridge_domain.function.ComposeBaseImpl;
import org.batfish.common.topology.bridge_domain.function.StateFunction;

/**
 * An edge between an {@link org.batfish.common.topology.bridge_domain.node.L2Interface} and a
 * {@link org.batfish.common.topology.bridge_domain.node.PhysicalInterface}.
 */
public final class L2ToPhysical extends Edge {
  public interface Function extends StateFunction {}

  /**
   * Helper for creating an edge from a traditional access-mode switchport to its physical
   * interface.
   */
  public static @Nonnull L2ToPhysical accessToPhysical() {
    return of(clearVlanId());
  }

  /**
   * Helper for creating an edge from an IOS-XR-style l2transport interface to a physical interface.
   *
   * <p>The API may evolve as new features are added, e.g. pushing more than one tag.
   */
  public static @Nonnull L2ToPhysical l2TransportToPhysical(@Nullable Integer tagToPush) {
    return of(tagToPush == null ? identity() : pushTag(tagToPush));
  }

  /**
   * Helper for creating an edge from a traditional trunk-mode switchport to its physical interface.
   */
  public static @Nonnull L2ToPhysical trunkToPhysical(@Nullable Integer exceptVlan) {
    return of(compose(pushVlanId(exceptVlan), clearVlanId()));
  }

  @VisibleForTesting
  public static @Nonnull Function compose(Function func1, Function func2) {
    return func1.equals(identity())
        ? func2
        : func2.equals(identity()) ? func1 : new Compose(func1, func2);
  }

  @VisibleForTesting
  public static @Nonnull L2ToPhysical of(Function stateFunction) {
    return new L2ToPhysical(stateFunction);
  }

  private L2ToPhysical(Function stateFunction) {
    super(stateFunction);
  }

  private static final class Compose extends ComposeBaseImpl<Function> implements Function {

    private Compose(Function func1, Function func2) {
      super(func1, func2);
    }
  }
}

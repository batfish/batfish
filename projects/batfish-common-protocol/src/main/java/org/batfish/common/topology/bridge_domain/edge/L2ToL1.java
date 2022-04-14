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
import org.batfish.common.topology.bridge_domain.node.L1Interface;

/**
 * An edge between an {@link org.batfish.common.topology.bridge_domain.node.L2Interface} and a
 * {@link L1Interface}.
 */
public final class L2ToL1 extends Edge {
  public interface Function extends StateFunction {}

  /** Helper for creating an edge from a traditional access-mode switchport to its l1 interface. */
  public static @Nonnull L2ToL1 accessToL1() {
    return of(clearVlanId());
  }

  /**
   * Helper for creating an edge from an IOS-XR-style l2transport interface to an l1 interface.
   *
   * <p>The API may evolve as new features are added, e.g. pushing more than one tag.
   */
  public static @Nonnull L2ToL1 l2TransportToL1(@Nullable Integer tagToPush) {
    return of(tagToPush == null ? identity() : pushTag(tagToPush));
  }

  /** Helper for creating an edge from a traditional trunk-mode switchport to its l1 interface. */
  public static @Nonnull L2ToL1 trunkToL1(@Nullable Integer exceptVlan) {
    return of(compose(pushVlanId(exceptVlan), clearVlanId()));
  }

  @VisibleForTesting
  public static @Nonnull Function compose(Function func1, Function func2) {
    return func1.equals(identity())
        ? func2
        : func2.equals(identity()) ? func1 : new Compose(func1, func2);
  }

  @VisibleForTesting
  public static @Nonnull L2ToL1 of(Function stateFunction) {
    return new L2ToL1(stateFunction);
  }

  private L2ToL1(Function stateFunction) {
    super(stateFunction);
  }

  private static final class Compose extends ComposeBaseImpl<Function> implements Function {

    private Compose(Function func1, Function func2) {
      super(func1, func2);
    }
  }
}

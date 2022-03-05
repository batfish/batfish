package org.batfish.common.topology.bridge_domain.edge;

import static org.batfish.common.topology.bridge_domain.function.StateFunctions.filterByVlanId;
import static org.batfish.common.topology.bridge_domain.function.StateFunctions.identity;
import static org.batfish.common.topology.bridge_domain.function.StateFunctions.translateVlan;

import com.google.common.annotations.VisibleForTesting;
import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.common.topology.bridge_domain.function.ComposeBaseImpl;
import org.batfish.common.topology.bridge_domain.function.StateFunction;
import org.batfish.datamodel.IntegerSpace;

/**
 * An edge from a {@link org.batfish.common.topology.bridge_domain.node.BridgeDomain} to an {@link
 * org.batfish.common.topology.bridge_domain.node.L2Interface}.
 */
public final class BridgeDomainToL2 extends Edge {

  public interface Function extends StateFunction {}

  /**
   * Helper for creating an edge from a vlan-aware bridge domain to a traditional access-mode
   * switchport.
   */
  public static @Nonnull BridgeDomainToL2 bridgeDomainToAccess(int accessVlan) {
    return new BridgeDomainToL2(filterByVlanId(IntegerSpace.of(accessVlan)));
  }

  /**
   * Helper for creating an edge from a vlan-aware bridge domain to a traditional trunk-mode
   * switchport.
   */
  public static @Nonnull BridgeDomainToL2 bridgeDomainToTrunk(
      Map<Integer, Integer> translations, IntegerSpace allowedVlans) {
    Function filter = filterByVlanId(allowedVlans);
    return new BridgeDomainToL2(
        translations.isEmpty() ? filter : compose(filter, translateVlan(translations)));
  }

  /**
   * Helper for creating an edge from a vlan-aware bridge domain to an IOS-XR-style l2transport
   * interface.
   */
  public static @Nonnull BridgeDomainToL2 bridgeDomainToL2Transport() {
    return new BridgeDomainToL2(identity());
  }

  @VisibleForTesting
  public static @Nonnull Function compose(Function func1, Function func2) {
    return func1.equals(identity())
        ? func2
        : func2.equals(identity()) ? func1 : new Compose(func1, func2);
  }

  @VisibleForTesting
  public static @Nonnull BridgeDomainToL2 of(Function stateFunction) {
    return new BridgeDomainToL2(stateFunction);
  }

  private BridgeDomainToL2(Function stateFunction) {
    super(stateFunction);
  }

  private static final class Compose extends ComposeBaseImpl<Function> implements Function {

    private Compose(Function func1, Function func2) {
      super(func1, func2);
    }
  }
}

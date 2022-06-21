package org.batfish.common.topology.bridge_domain.function;

import static org.batfish.common.topology.bridge_domain.function.StateFunctions.identity;

import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.topology.bridge_domain.edge.L2ToVlanAwareBridgeDomain;
import org.batfish.common.topology.bridge_domain.edge.VlanAwareBridgeDomainToL2;

/** A {@link StateFunction} that maps the state's VLAN ID to another VLAN ID. */
public interface TranslateVlan
    extends VlanAwareBridgeDomainToL2.Function, L2ToVlanAwareBridgeDomain.Function {

  static @Nonnull TranslateVlan of(Map<Integer, Integer> translations) {
    return translations.isEmpty() ? identity() : new TranslateVlanImpl(translations);
  }

  final class TranslateVlanImpl implements TranslateVlan {

    @Override
    public <T, U> T accept(StateFunctionVisitor<T, U> visitor, U arg) {
      return visitor.visitTranslateVlan(this, arg);
    }

    /**
     * A map of concrete VLAN ID translations. If the state's set VLAN ID is not in the map, it
     * remains unmodified.
     *
     * <p>The result is undefined if no VLAN ID is set in the state.
     */
    public @Nonnull Map<Integer, Integer> getTranslations() {
      return _translations;
    }

    @Override
    public boolean equals(@Nullable Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof TranslateVlanImpl)) {
        return false;
      }
      TranslateVlanImpl that = (TranslateVlanImpl) o;
      return _translations.equals(that._translations);
    }

    @Override
    public int hashCode() {
      return _translations.hashCode();
    }

    private TranslateVlanImpl(Map<Integer, Integer> translations) {
      _translations = translations;
    }

    private final @Nonnull Map<Integer, Integer> _translations;
  }
}

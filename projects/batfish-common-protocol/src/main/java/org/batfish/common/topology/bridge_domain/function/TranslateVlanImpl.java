package org.batfish.common.topology.bridge_domain.function;

import static org.batfish.common.topology.bridge_domain.function.StateFunctions.identity;

import java.util.Map;
import javax.annotation.Nonnull;

/** A {@link StateFunction} that maps the state's VLAN ID to another VLAN ID. */
public final class TranslateVlanImpl implements TranslateVlan {

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

  static @Nonnull TranslateVlan of(Map<Integer, Integer> translations) {
    return translations.isEmpty() ? identity() : new TranslateVlanImpl(translations);
  }

  private TranslateVlanImpl(Map<Integer, Integer> translations) {
    _translations = translations;
  }

  private final @Nonnull Map<Integer, Integer> _translations;
}

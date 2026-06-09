package org.batfish.vendor.sros.representation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An SR-OS line card provisioned in a chassis slot. Keyed by slot number (e.g. {@code card 1}). The
 * card type and its MDAs must be provisioned in config before datapath ports exist.
 */
public final class Card implements Serializable {

  public Card(int slot) {
    _slot = slot;
    _mdas = new HashMap<>();
  }

  public int getSlot() {
    return _slot;
  }

  /** The provisioned card type (e.g. {@code iom-1}), or {@code null} if unset. */
  public @Nullable String getCardType() {
    return _cardType;
  }

  public void setCardType(@Nullable String cardType) {
    _cardType = cardType;
  }

  /** MDAs provisioned on this card, keyed by mda slot. */
  public @Nonnull Map<Integer, Mda> getMdas() {
    return _mdas;
  }

  private final int _slot;
  private @Nullable String _cardType;
  private final @Nonnull Map<Integer, Mda> _mdas;
}

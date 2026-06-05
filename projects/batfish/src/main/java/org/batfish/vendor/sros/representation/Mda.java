package org.batfish.vendor.sros.representation;

import java.io.Serializable;
import javax.annotation.Nullable;

/**
 * An SR-OS MDA (Media Dependent Adapter) provisioned within a {@link Card}. Keyed by its numeric
 * mda slot within the card (e.g. {@code mda 1}).
 */
public final class Mda implements Serializable {

  public Mda(int slot) {
    _slot = slot;
  }

  public int getSlot() {
    return _slot;
  }

  /** The provisioned MDA type (e.g. {@code me6-100gb-qsfp28}), or {@code null} if unset. */
  public @Nullable String getMdaType() {
    return _mdaType;
  }

  public void setMdaType(@Nullable String mdaType) {
    _mdaType = mdaType;
  }

  private final int _slot;
  private @Nullable String _mdaType;
}

package org.batfish.datamodel.tracking;

import com.fasterxml.jackson.annotation.JsonCreator;
import javax.annotation.Nonnull;

/** Disable HSRP/VRRP group when the tracked object is not available */
public final class DisableGroup implements TrackAction {

  private static final DisableGroup INSTANCE = new DisableGroup();

  @JsonCreator
  public static @Nonnull DisableGroup instance() {
    return INSTANCE;
  }

  @Override
  public boolean equals(Object obj) {
    return this == obj || obj instanceof DisableGroup;
  }

  @Override
  public int hashCode() {
    return 0;
  }
}

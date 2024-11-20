package org.batfish.representation.juniper;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An MPLS Shared Risk Link Group.
 *
 * <p>See:
 * https://www.juniper.net/documentation/us/en/software/junos/mpls/topics/topic-map/srlg-for-mpls.html
 */
public class Srlg implements Serializable {

  private final @Nonnull String _name;
  private @Nullable Integer _cost;
  private @Nullable Long _value;

  public Srlg(@Nonnull String name) {
    _name = name;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable Integer getCost() {
    return _cost;
  }

  public void setCost(@Nullable Integer cost) {
    _cost = cost;
  }

  public @Nullable Long getValue() {
    return _value;
  }

  public void setValue(@Nullable Long value) {
    _value = value;
  }
}

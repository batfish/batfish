package org.batfish.vendor.a10.representation;

import static com.google.common.base.MoreObjects.firstNonNull;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Datamodel class representing a configured {@code trunk-group} (also called {@code trunk} in some
 * contexts) under an {@link Interface}.
 */
public final class TrunkGroup implements Serializable {
  public enum Mode {
    ACTIVE,
    PASSIVE,
  }

  public enum Timeout {
    LONG,
    SHORT,
  }

  public enum Type {
    LACP,
    LACP_UDLD,
    STATIC,
  }

  public static final Type DEFAULT_TRUNK_TYPE = TrunkGroup.Type.STATIC;

  public int getNumber() {
    return _number;
  }

  @Nullable
  public Mode getMode() {
    return _mode;
  }

  @Nullable
  public Timeout getTimeout() {
    return _timeout;
  }

  /**
   * The type for this trunk. This is the effective type for this trunk, even if not explicitly
   * configured.
   */
  @Nonnull
  public Type getTypeEffective() {
    return getTypeEffective(_type);
  }

  /** Get the effective type, given the specified, possibly {@code Null} {@link Type}. */
  @Nonnull
  public static Type getTypeEffective(@Nullable Type type) {
    return firstNonNull(type, DEFAULT_TRUNK_TYPE);
  }

  @Nullable
  public String getUserTag() {
    return _userTag;
  }

  public void setMode(Mode mode) {
    _mode = mode;
  }

  public void setTimeout(Timeout timeout) {
    _timeout = timeout;
  }

  public void setUserTag(String userTag) {
    _userTag = userTag;
  }

  public TrunkGroup(int number, @Nullable Type type) {
    _number = number;
    _type = type;
  }

  @Nullable private Mode _mode;
  private final int _number;
  @Nullable private Timeout _timeout;
  @Nullable private final Type _type;
  @Nullable private String _userTag;
}

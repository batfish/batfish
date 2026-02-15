package org.batfish.representation.fortios;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Prefix;

/** FortiOS datamodel component containing prefix-list rule configuration */
public class PrefixListRule implements Serializable {

  public static final int DEFAULT_GE = -1; // -1 means not set
  public static final int DEFAULT_LE = -1; // -1 means not set

  public @Nonnull String getNumber() {
    return _number;
  }

  public @Nullable Prefix getPrefix() {
    return _prefix;
  }

  /** Returns the minimum prefix length, or -1 if not set. */
  public int getGe() {
    return _ge;
  }

  /** Returns the maximum prefix length, or -1 if not set. */
  public int getLe() {
    return _le;
  }

  public void setPrefix(Prefix prefix) {
    _prefix = prefix;
  }

  public void setGe(int ge) {
    _ge = ge;
  }

  public void setLe(int le) {
    _le = le;
  }

  /** Clears the ge value (sets to -1). */
  public void unsetGe() {
    _ge = DEFAULT_GE;
  }

  /** Clears the le value (sets to -1). */
  public void unsetLe() {
    _le = DEFAULT_LE;
  }

  public PrefixListRule(String number) {
    _number = number;
    _ge = DEFAULT_GE;
    _le = DEFAULT_LE;
  }

  private final @Nonnull String _number;
  private @Nullable Prefix _prefix;
  private int _ge;
  private int _le;
}

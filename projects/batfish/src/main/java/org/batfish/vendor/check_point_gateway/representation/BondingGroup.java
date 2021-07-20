package org.batfish.vendor.check_point_gateway.representation;

import static com.google.common.base.MoreObjects.firstNonNull;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Check Point gateway datamodel component containing bonding group configuration */
public class BondingGroup implements Serializable {
  public enum LacpRate {
    FAST,
    SLOW,
  }

  public enum Mode {
    ACTIVE_BACKUP,
    EIGHT_ZERO_TWO_THREE_AD,
    ROUND_ROBIN,
    XOR,
  }

  public enum XmitHashPolicy {
    LAYER2,
    LAYER3_4,
  }

  public static Mode DEFAULT_MODE = Mode.ROUND_ROBIN;

  @Nonnull
  public Set<String> getInterfaces() {
    return _interfaces;
  }

  @Nullable
  public LacpRate getLacpRate() {
    return _lacpRate;
  }

  @Nullable
  public Mode getMode() {
    return _mode;
  }

  @Nonnull
  public Mode getModeEffective() {
    return firstNonNull(_mode, DEFAULT_MODE);
  }

  public int getNumber() {
    return _number;
  }

  @Nullable
  public XmitHashPolicy getXmitHashPolicy() {
    return _xmitHashPolicy;
  }

  public void setLacpRate(LacpRate lacpRate) {
    _lacpRate = lacpRate;
  }

  public void setMode(Mode mode) {
    _mode = mode;
  }

  public void setXmitHashPolicy(XmitHashPolicy xmitHashPolicy) {
    _xmitHashPolicy = xmitHashPolicy;
  }

  public BondingGroup(int number) {
    _number = number;
    _interfaces = new HashSet<>();
  }

  @Nonnull private final Set<String> _interfaces;
  private final int _number;
  @Nullable private LacpRate _lacpRate;
  @Nullable private Mode _mode;
  @Nullable private XmitHashPolicy _xmitHashPolicy;
}

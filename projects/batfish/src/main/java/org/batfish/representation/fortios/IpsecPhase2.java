package org.batfish.representation.fortios;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** FortiOS datamodel component containing IPsec Phase 2 configuration */
public final class IpsecPhase2 implements Serializable {
  public IpsecPhase2(@Nonnull String name) {
    _name = name;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable String getPhase1Name() {
    return _phase1Name;
  }

  public @Nullable String getProposal() {
    return _proposal;
  }

  /** Name of the firewall address object for source selector */
  public @Nullable String getSrcName() {
    return _srcName;
  }

  /** Name of the firewall address object for destination selector */
  public @Nullable String getDstName() {
    return _dstName;
  }

  public void setPhase1Name(String phase1Name) {
    _phase1Name = phase1Name;
  }

  public void setProposal(String proposal) {
    _proposal = proposal;
  }

  public void setSrcName(String srcName) {
    _srcName = srcName;
  }

  public void setDstName(String dstName) {
    _dstName = dstName;
  }

  private final @Nonnull String _name;
  private @Nullable String _phase1Name;
  private @Nullable String _proposal;
  private @Nullable String _srcName;
  private @Nullable String _dstName;
}

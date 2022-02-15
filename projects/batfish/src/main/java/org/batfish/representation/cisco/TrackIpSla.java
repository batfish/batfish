package org.batfish.representation.cisco;

import javax.annotation.ParametersAreNonnullByDefault;

/** Track whether an SLA is active and its check will succeed. */
@ParametersAreNonnullByDefault
public class TrackIpSla implements Track {

  public TrackIpSla(int sla) {
    _sla = sla;
  }

  @Override
  public <T> T accept(TrackVisitor<T> visitor) {
    return visitor.visitTrackIpSla(this);
  }

  /**
   * If {@code true}, then this track is of type {@code track ip sla reachability}. Else, this track
   * is of type {@code track ip sla state}.
   */
  public boolean getReachability() {
    return _reachability;
  }

  public void setReachability(boolean reachability) {
    _reachability = reachability;
  }

  public int getIpSla() {
    return _sla;
  }

  public void setIpSla(int sla) {
    _sla = sla;
  }

  private boolean _reachability;
  private int _sla;
}

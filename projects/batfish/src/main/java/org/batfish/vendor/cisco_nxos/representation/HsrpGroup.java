package org.batfish.vendor.cisco_nxos.representation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** HSRP group within {@link InterfaceHsrp} settings for an {@link Interface}. */
public abstract class HsrpGroup implements Serializable {

  public HsrpGroup(int group) {
    _group = group;
    _preempt = false;
    _tracks = new HashMap<>();
  }

  public @Nullable Integer getHelloIntervalMs() {
    return _helloIntervalMs;
  }

  public void setHelloIntervalMs(@Nullable Integer helloIntervalMs) {
    _helloIntervalMs = helloIntervalMs;
  }

  public @Nullable Integer getHoldTimeMs() {
    return _holdTimeMs;
  }

  public void setHoldTimeMs(@Nullable Integer holdTimeMs) {
    _holdTimeMs = holdTimeMs;
  }

  public @Nullable String getName() {
    return _name;
  }

  public void setName(@Nullable String name) {
    _name = name;
  }

  public boolean getPreempt() {
    return _preempt;
  }

  public void setPreempt(boolean preempt) {
    _preempt = preempt;
  }

  public @Nullable Integer getPreemptDelayMinimumSeconds() {
    return _preemptDelayMinimumSeconds;
  }

  public void setPreemptDelayMinimumSeconds(@Nullable Integer preemptDelayMinimumSeconds) {
    _preemptDelayMinimumSeconds = preemptDelayMinimumSeconds;
  }

  public @Nullable Integer getPreemptDelayReloadSeconds() {
    return _preemptDelayReloadSeconds;
  }

  public void setPreemptDelayReloadSeconds(@Nullable Integer preemptDelayReloadSeconds) {
    _preemptDelayReloadSeconds = preemptDelayReloadSeconds;
  }

  public @Nullable Integer getPreemptDelaySyncSeconds() {
    return _preemptDelaySyncSeconds;
  }

  public void setPreemptDelaySyncSeconds(@Nullable Integer preemptDelaySyncSeconds) {
    _preemptDelaySyncSeconds = preemptDelaySyncSeconds;
  }

  public @Nullable Integer getPriority() {
    return _priority;
  }

  public void setPriority(@Nullable Integer priority) {
    _priority = priority;
  }

  public int getGroup() {
    return _group;
  }

  public @Nonnull Map<Integer, HsrpTrack> getTracks() {
    return _tracks;
  }

  //////////////////////////////////////////
  ///// Private implementation details /////
  //////////////////////////////////////////

  private final int _group;
  private @Nullable Integer _helloIntervalMs;
  private @Nullable Integer _holdTimeMs;
  private @Nullable String _name;
  private boolean _preempt;
  private @Nullable Integer _preemptDelayMinimumSeconds;
  private @Nullable Integer _preemptDelayReloadSeconds;
  private @Nullable Integer _preemptDelaySyncSeconds;
  private @Nullable Integer _priority;
  private final @Nonnull Map<Integer, HsrpTrack> _tracks;
}

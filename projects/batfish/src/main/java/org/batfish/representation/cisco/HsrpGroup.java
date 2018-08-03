package org.batfish.representation.cisco;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.TrackAction;

public class HsrpGroup implements Serializable {

  private static final long serialVersionUID = 1L;

  private String _authentication;

  private int _helloTime;

  private int _holdTime;

  private Ip _ip;

  private final int _number;

  private boolean _preempt;

  private int _priority;

  private final SortedMap<String, TrackAction> _trackActions;

  public HsrpGroup(int number) {
    _helloTime = org.batfish.datamodel.HsrpGroup.DEFAULT_HELLO_TIME;
    _holdTime = org.batfish.datamodel.HsrpGroup.DEFAULT_HOLD_TIME;
    _number = number;
    _priority = org.batfish.datamodel.HsrpGroup.DEFAULT_PRIORITY;
    _trackActions = new TreeMap<>();
  }

  public String getAuthentication() {
    return _authentication;
  }

  public int getHelloTime() {
    return _helloTime;
  }

  public int getHoldTime() {
    return _holdTime;
  }

  public Ip getIp() {
    return _ip;
  }

  public int getNumber() {
    return _number;
  }

  public boolean getPreempt() {
    return _preempt;
  }

  public int getPriority() {
    return _priority;
  }

  public @Nonnull SortedMap<String, TrackAction> getTrackActions() {
    return _trackActions;
  }

  public void setAuthentication(String authentication) {
    _authentication = authentication;
  }

  public void setHelloTime(int helloTime) {
    _helloTime = helloTime;
  }

  public void setHoldTime(int holdTime) {
    _holdTime = holdTime;
  }

  public void setIp(Ip ip) {
    _ip = ip;
  }

  public void setPreempt(boolean preempt) {
    _preempt = preempt;
  }

  public void setPriority(int priority) {
    _priority = priority;
  }
}

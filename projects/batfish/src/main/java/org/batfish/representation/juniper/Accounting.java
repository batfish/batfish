package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Vendor-specific model of a Junos {@code [edit system accounting]} block: the audited {@code
 * events} and the {@code destination}.
 *
 * <p>Only the {@code tacplus} destination is modeled (reusing {@link TacplusServer} for any
 * explicit {@code server} sub-blocks); the {@code radius} destination and {@code enhanced-avs-max}
 * are parsed but not modeled.
 */
public class Accounting implements Serializable {

  /** Types of events audited by {@code accounting events}. */
  public enum Event {
    CHANGE_LOG,
    INTERACTIVE_COMMANDS,
    LOGIN,
  }

  private final Set<Event> _events;

  private boolean _tacplusDestination;

  private final Map<String, TacplusServer> _tacplusServers;

  public Accounting() {
    _events = EnumSet.noneOf(Event.class);
    _tacplusServers = new TreeMap<>();
  }

  /** Event types configured under {@code accounting events}. */
  public Set<Event> getEvents() {
    return _events;
  }

  /** Whether {@code accounting destination tacplus} is configured. */
  public boolean getTacplusDestination() {
    return _tacplusDestination;
  }

  public void setTacplusDestination(boolean tacplusDestination) {
    _tacplusDestination = tacplusDestination;
  }

  /**
   * Servers configured under {@code accounting destination tacplus server}, keyed by server
   * address.
   *
   * <p>These reference the top-level {@code [edit system tacplus-server]} servers and typically
   * only add accounting-specific options (e.g. {@code routing-instance}); unset {@link
   * TacplusServer} fields are inherited from the corresponding top-level server.
   */
  public Map<String, TacplusServer> getTacplusServers() {
    return _tacplusServers;
  }
}

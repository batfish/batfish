package org.batfish.datamodel.vendor_family.cisco_xr;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;

public class Logging implements Serializable {
  private static final String PROP_BUFFERED = "buffered";
  private static final String PROP_CONSOLE = "console";
  private static final String PROP_HOSTS = "hosts";
  private static final String PROP_ON = "on";
  private static final String PROP_SOURCE_INTERFACE = "sourceInterface";
  private static final String PROP_TRAP = "trap";

  public static final int MAX_LOGGING_SEVERITY = 7;

  public static final String SEVERITY_ALERTS = "alerts";

  public static final String SEVERITY_CRITICAL = "critical";

  public static final String SEVERITY_DEBUGGING = "debugging";

  public static final String SEVERITY_EMERGENCIES = "emergencies";

  public static final String SEVERITY_ERRORS = "errors";

  public static final String SEVERITY_INFORMATIONAL = "informational";

  public static final String SEVERITY_NOTIFICATIONS = "notifications";

  public static final String SEVERITY_WARNINGS = "warnings";

  private Buffered _buffered;

  private LoggingType _console;

  private SortedMap<String, LoggingHost> _hosts;

  private boolean _on;

  private String _sourceInterface;

  private LoggingType _trap;

  public Logging() {
    _hosts = new TreeMap<>();
    _on = true;
  }

  @JsonProperty(PROP_BUFFERED)
  public Buffered getBuffered() {
    return _buffered;
  }

  @JsonProperty(PROP_CONSOLE)
  public LoggingType getConsole() {
    return _console;
  }

  @JsonProperty(PROP_HOSTS)
  public SortedMap<String, LoggingHost> getHosts() {
    return _hosts;
  }

  @JsonProperty(PROP_ON)
  public boolean getOn() {
    return _on;
  }

  @JsonProperty(PROP_SOURCE_INTERFACE)
  public String getSourceInterface() {
    return _sourceInterface;
  }

  @JsonProperty(PROP_TRAP)
  public LoggingType getTrap() {
    return _trap;
  }

  @JsonProperty(PROP_BUFFERED)
  public void setBuffered(Buffered buffered) {
    _buffered = buffered;
  }

  @JsonProperty(PROP_CONSOLE)
  public void setConsole(LoggingType console) {
    _console = console;
  }

  @JsonProperty(PROP_HOSTS)
  public void setHosts(SortedMap<String, LoggingHost> hosts) {
    _hosts = hosts;
  }

  @JsonProperty(PROP_ON)
  public void setOn(boolean on) {
    _on = on;
  }

  @JsonProperty(PROP_SOURCE_INTERFACE)
  public void setSourceInterface(String sourceInterface) {
    _sourceInterface = sourceInterface;
  }

  @JsonProperty(PROP_TRAP)
  public void setTrap(LoggingType trap) {
    _trap = trap;
  }
}

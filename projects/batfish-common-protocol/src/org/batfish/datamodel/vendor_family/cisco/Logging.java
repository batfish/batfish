package org.batfish.datamodel.vendor_family.cisco;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;

public class Logging implements Serializable {

   public static final int MAX_LOGGING_SEVERITY = 7;

   /**
    *
    */
   private static final long serialVersionUID = 1L;

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

   public Buffered getBuffered() {
      return _buffered;
   }

   public LoggingType getConsole() {
      return _console;
   }

   public SortedMap<String, LoggingHost> getHosts() {
      return _hosts;
   }

   public boolean getOn() {
      return _on;
   }

   public String getSourceInterface() {
      return _sourceInterface;
   }

   public LoggingType getTrap() {
      return _trap;
   }

   public void setBuffered(Buffered buffered) {
      _buffered = buffered;
   }

   public void setConsole(LoggingType console) {
      _console = console;
   }

   public void setHosts(SortedMap<String, LoggingHost> hosts) {
      _hosts = hosts;
   }

   public void setOn(boolean on) {
      _on = on;
   }

   public void setSourceInterface(String sourceInterface) {
      _sourceInterface = sourceInterface;
   }

   public void setTrap(LoggingType trap) {
      _trap = trap;
   }

}

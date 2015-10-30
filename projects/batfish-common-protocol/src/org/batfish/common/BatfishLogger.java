package org.batfish.common;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BatfishLogger {

   public class BatfishLoggerHistory extends ArrayList<HistoryItem> {
      /**
       *
       */
      private static final long serialVersionUID = 1L;
   }

   private class HistoryItem extends Pair<Integer, String> {
      /**
       *
       */
      private static final long serialVersionUID = 1L;

      private HistoryItem(int i, String s) {
         super(i, s);
      }

      public int getLevel() {
         return _t1;
      }

      public String getMessage() {
         return _t2;
      }
   }

   public static final int LEVEL_DEBUG = 500;

   public static final int LEVEL_ERROR = 200;

   public static final int LEVEL_FATAL = 100;

   public static final int LEVEL_INFO = 400;

   public static final int LEVEL_OUTPUT = 220;

   public static final int LEVEL_PEDANTIC = 550;

   public static final int LEVEL_REDFLAG = 250;

   public static final int LEVEL_UNIMPLEMENTED = 270;

   public static final int LEVEL_WARN = 300;

   private static final String LEVELSTR_DEBUG = "debug";

   private static final String LEVELSTR_ERROR = "error";

   private static final String LEVELSTR_FATAL = "fatal";

   private static final String LEVELSTR_INFO = "info";

   private static final String LEVELSTR_OUTPUT = "output";

   private static final String LEVELSTR_PEDANTIC = "pedantic";

   private static final String LEVELSTR_REDFLAG = "redflag";

   private static final String LEVELSTR_UNIMPLEMENTED = "unimplemented";

   private static final String LEVELSTR_WARN = "warn";

   private static final Map<String, Integer> LOG_LEVELS = initializeLogLevels();

   private static final Map<Integer, String> LOG_LEVELSTRS = initializeLogLevelStrs();

   public static String getLogLevelStr(int level) {
      return LOG_LEVELSTRS.get(level);
   }

   private static Map<String, Integer> initializeLogLevels() {
      Map<String, Integer> levels = new HashMap<String, Integer>();
      levels.put(LEVELSTR_DEBUG, LEVEL_DEBUG);
      levels.put(LEVELSTR_ERROR, LEVEL_ERROR);
      levels.put(LEVELSTR_FATAL, LEVEL_FATAL);
      levels.put(LEVELSTR_INFO, LEVEL_INFO);
      levels.put(LEVELSTR_OUTPUT, LEVEL_OUTPUT);
      levels.put(LEVELSTR_PEDANTIC, LEVEL_PEDANTIC);
      levels.put(LEVELSTR_REDFLAG, LEVEL_REDFLAG);
      levels.put(LEVELSTR_UNIMPLEMENTED, LEVEL_UNIMPLEMENTED);
      levels.put(LEVELSTR_WARN, LEVEL_WARN);
      return levels;
   }

   private static Map<Integer, String> initializeLogLevelStrs() {
      Map<Integer, String> levels = new HashMap<Integer, String>();
      levels.put(LEVEL_DEBUG, LEVELSTR_DEBUG);
      levels.put(LEVEL_ERROR, LEVELSTR_ERROR);
      levels.put(LEVEL_FATAL, LEVELSTR_FATAL);
      levels.put(LEVEL_INFO, LEVELSTR_INFO);
      levels.put(LEVEL_OUTPUT, LEVELSTR_OUTPUT);
      levels.put(LEVEL_PEDANTIC, LEVELSTR_PEDANTIC);
      levels.put(LEVEL_REDFLAG, LEVELSTR_REDFLAG);
      levels.put(LEVEL_UNIMPLEMENTED, LEVELSTR_UNIMPLEMENTED);
      levels.put(LEVEL_WARN, LEVELSTR_WARN);
      return levels;
   }

   private final BatfishLoggerHistory _history;

   private int _level;

   private String _logFile;

   private PrintStream _ps;

   private boolean _timestamp;

   public BatfishLogger(String logLevel, boolean timestamp) {
      _timestamp = timestamp;
      setLogLevel(logLevel);
      _history = new BatfishLoggerHistory();
   }

   public BatfishLogger(String logLevel, boolean timestamp, PrintStream stream) {
      _history = null;
      _timestamp = timestamp;
      String levelStr = logLevel;
      setLogLevel(levelStr);
      _ps = stream;
   }

   public BatfishLogger(String logLevel, boolean timestamp, String logFile,
         boolean logTee) {
      _history = null;
      _timestamp = timestamp;
      String levelStr = logLevel;
      setLogLevel(levelStr);
      _logFile = logFile;
      if (_logFile != null) {
         PrintStream filePrintStream = null;
         try {
            filePrintStream = new PrintStream(_logFile);
         }
         catch (FileNotFoundException e) {
            throw new BatfishException("Could not create logfile", e);
         }
         if (logTee) {
            _ps = new CompositePrintStream(System.out, filePrintStream);
         }
         else {
            _ps = filePrintStream;
         }

      }
      else {
         _ps = System.out;
      }
   }

   public void append(BatfishLoggerHistory history) {
      append(history, "");
   }

   public void append(BatfishLoggerHistory history, String prefix) {
      for (HistoryItem item : history) {
         int level = item.getLevel();
         String msg = prefix + item.getMessage();
         write(level, msg);
      }
   }

   public void close() {
      if (_logFile != null) {
         _ps.close();
      }
   }

   public void debug(String msg) {
      write(LEVEL_DEBUG, msg);
   }

   public void debugf(String format, Object... args) {
      debug(String.format(format, args));
   }

   public void error(String msg) {
      write(LEVEL_ERROR, msg);
   }

   public void errorf(String format, Object... args) {
      error(String.format(format, args));
   }

   public void fatal(String msg) {
      write(LEVEL_FATAL, msg);
   }

   public BatfishLoggerHistory getHistory() {
      return _history;
   }

   public String getLogLevelStr() {
      return LOG_LEVELSTRS.get(_level);
   }

   public PrintStream getPrintStream() {
      return _ps;
   }

   public void info(String msg) {
      write(LEVEL_INFO, msg);
   }

   public void infof(String format, Object... args) {
      info(String.format(format, args));
   }

   public boolean isActive(int level) {
      return level <= _level;
   }

   public void output(String msg) {
      write(LEVEL_OUTPUT, msg);
   }

   public void outputf(String format, Object... args) {
      output(String.format(format, args));
   }

   public void pedantic(String msg) {
      write(LEVEL_PEDANTIC, msg);
   }

   public void redflag(String msg) {
      write(LEVEL_REDFLAG, msg);
   }

   public void setLogLevel(String levelStr) {
      String canonicalLevelStr = levelStr.toLowerCase();
      _level = LOG_LEVELS.get(canonicalLevelStr);
   }

   public void unimplemented(String msg) {
      write(LEVEL_UNIMPLEMENTED, msg);
   }

   public void warn(String msg) {
      write(LEVEL_WARN, msg);
   }

   private void write(int level, String msg) {
      if (isActive(level)) {
         String outputMsg;
         if (_timestamp) {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateStr = df.format(new Date());
            outputMsg = String.format("%s: %s", dateStr, msg);
         }
         else {
            outputMsg = msg;
         }
         if (_ps != null) {
            _ps.print(outputMsg);
         }
         else {
            _history.add(new HistoryItem(level, msg));
         }
      }
   }
}

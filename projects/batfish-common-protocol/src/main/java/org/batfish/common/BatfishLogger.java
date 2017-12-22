package org.batfish.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BatfishLogger {

  public static class BatfishLoggerHistory extends ArrayList<HistoryItem> {
    /** */
    private static final long serialVersionUID = 1L;

    public String toString(int logLevel) {
      StringBuilder sb = new StringBuilder();
      for (HistoryItem item : this) {
        if (item.getLevel() <= logLevel) {
          sb.append(item.getMessage());
        }
      }
      return sb.toString();
    }
  }

  private static class HistoryItem extends Pair<Integer, String> {
    /** */
    private static final long serialVersionUID = 1L;

    private HistoryItem(int i, String s) {
      super(i, s);
    }

    public int getLevel() {
      return _first;
    }

    public String getMessage() {
      return _second;
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

  public static final String LEVELSTR_DEBUG = "debug";

  public static final String LEVELSTR_ERROR = "error";

  public static final String LEVELSTR_FATAL = "fatal";

  public static final String LEVELSTR_INFO = "info";

  public static final String LEVELSTR_OUTPUT = "output";

  public static final String LEVELSTR_PEDANTIC = "pedantic";

  public static final String LEVELSTR_REDFLAG = "redflag";

  public static final String LEVELSTR_UNIMPLEMENTED = "unimplemented";

  public static final String LEVELSTR_WARN = "warn";

  private static final Map<String, Integer> LOG_LEVELS = initializeLogLevels();

  private static final Map<Integer, String> LOG_LEVELSTRS = initializeLogLevelStrs();

  private static final int LOG_ROTATION_THRESHOLD = 10000;

  public static int getLogLevel(String levelStr) {
    String canonicalLevelStr = levelStr.toLowerCase();
    return LOG_LEVELS.get(canonicalLevelStr);
  }

  public static String getLogLevelStr(int level) {
    return LOG_LEVELSTRS.get(level);
  }

  private static String getRotatedLogFilename(String logFilename) {
    DateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmss.SSS");
    String baseRotatedLogFilename = logFilename + '-' + df.format(new Date());
    File rotatedLogFile = new File(baseRotatedLogFilename);
    String returnFilename = baseRotatedLogFilename;

    int index = 0;
    while (rotatedLogFile.exists()) {
      returnFilename = baseRotatedLogFilename + "." + index;
      rotatedLogFile = new File(returnFilename);
      index++;
    }

    return returnFilename;
  }

  private static Map<String, Integer> initializeLogLevels() {
    Map<String, Integer> levels = new HashMap<>();
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
    Map<Integer, String> levels = new HashMap<>();
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

  public static boolean isValidLogLevel(String levelStr) {
    return LOG_LEVELS.containsKey(levelStr);
  }

  private final BatfishLoggerHistory _history;

  private int _level;

  private String _logFile;

  private int _numLinesSinceRotation = 0;

  private PrintStream _ps;

  private boolean _rotateLog = false;

  private long _timerCount;

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

  public BatfishLogger(
      String logLevel, boolean timestamp, String logFile, boolean logTee, boolean rotateLog) {
    _history = null;
    _timestamp = timestamp;
    String levelStr = logLevel;
    setLogLevel(levelStr);
    _logFile = logFile;
    if (_logFile != null) {

      // if the file already exists, archive it
      File logFileFile = new File(_logFile);
      if (logFileFile.exists()) {
        String rotatedLog = getRotatedLogFilename(_logFile);
        if (!logFileFile.renameTo(new File(rotatedLog))) {
          throw new BatfishException(
              String.format("Failed to rename %s to %s", _logFile, rotatedLog));
        }
      }

      PrintStream filePrintStream = null;
      try {
        filePrintStream = new PrintStream(_logFile, "UTF-8");
        _rotateLog = rotateLog;
      } catch (FileNotFoundException | UnsupportedEncodingException e) {
        throw new BatfishException("Could not create logfile", e);
      }
      if (logTee) {
        _ps = new CompositePrintStream(System.out, filePrintStream);
      } else {
        _ps = filePrintStream;
      }

    } else {
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

  private double getElapsedTime(long beforeTime) {
    long difference = System.currentTimeMillis() - beforeTime;
    double seconds = difference / 1000d;
    return seconds;
  }

  public BatfishLoggerHistory getHistory() {
    return _history;
  }

  public int getLogLevel() {
    return _level;
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

  public void printElapsedTime() {
    double seconds = getElapsedTime(_timerCount);
    info("Time taken for this task: " + seconds + " seconds\n");
  }

  public void redflag(String msg) {
    write(LEVEL_REDFLAG, msg);
  }

  public void resetTimer() {
    _timerCount = System.currentTimeMillis();
  }

  private synchronized void rotateLog() {
    if (_logFile != null && _ps != null) {

      _ps.close();

      String rotatedLog = getRotatedLogFilename(_logFile);

      File logFile = new File(_logFile);
      if (!logFile.renameTo(new File(rotatedLog))) {
        throw new BatfishException(
            String.format("Failed to rename %s to %s", _logFile, rotatedLog));
      }

      try {
        PrintStream filePrintStream = new PrintStream(_logFile, "UTF-8");

        if (_ps instanceof CompositePrintStream) {
          _ps = new CompositePrintStream(System.out, filePrintStream);
        } else {
          _ps = filePrintStream;
        }
      } catch (Exception e) {
        // we have this try catch because new PrintStream throws
        // FileNotFoundException
        // this should not happen since we know that logFile can be created
        // in case it does happen, we cannot log this error to the log :)
        System.err.print("Could not rotate log" + e.getMessage());
      }
    }
  }

  public void setLogLevel(String levelStr) {
    _level = getLogLevel(levelStr);
  }

  public void unimplemented(String msg) {
    write(LEVEL_UNIMPLEMENTED, msg);
  }

  public void warn(String msg) {
    write(LEVEL_WARN, msg);
  }

  public void warnf(String format, Object... args) {
    warn(String.format(format, args));
  }

  private synchronized void write(int level, String msg) {
    if (isActive(level)) {
      String outputMsg;
      if (_timestamp) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateStr = df.format(new Date());
        outputMsg = String.format("%s: %s", dateStr, msg);
      } else {
        outputMsg = msg;
      }
      if (_ps != null) {
        _ps.print(outputMsg);

        // logic for rotating log
        if (_rotateLog) {
          _numLinesSinceRotation++;

          if (_numLinesSinceRotation > LOG_ROTATION_THRESHOLD) {
            rotateLog();
            _numLinesSinceRotation = 0;
          }
        }
      } else {
        _history.add(new HistoryItem(level, msg));
      }
    }
  }
}

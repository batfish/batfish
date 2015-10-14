package org.batfish.common;

import java.util.HashMap;
import java.util.Map;

public class LogHelper {

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

   public static final Map<String, Integer> LOG_LEVELS = initializeLogLevels();

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

   public static String toString(int level) {
      switch (level) {

      case LEVEL_DEBUG: 
         return LEVELSTR_DEBUG;

      case LEVEL_ERROR:
         return LEVELSTR_ERROR;

      case LEVEL_FATAL:
         return LEVELSTR_FATAL;

      case LEVEL_INFO:
         return LEVELSTR_INFO;

      case LEVEL_OUTPUT:
         return LEVELSTR_OUTPUT;

      case LEVEL_PEDANTIC:
         return LEVELSTR_PEDANTIC;

      case LEVEL_REDFLAG:
         return LEVELSTR_REDFLAG;

      case LEVEL_UNIMPLEMENTED:
         return LEVELSTR_UNIMPLEMENTED;

      case LEVEL_WARN:
         return LEVELSTR_WARN;

      default:
         return "UnknownLogLevel";
      }
   }
}
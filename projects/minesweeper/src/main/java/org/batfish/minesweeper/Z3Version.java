package org.batfish.minesweeper;

import com.google.auto.service.AutoService;
import org.batfish.version.Versioned;

@AutoService(Versioned.class)
public class Z3Version implements Versioned {
  public static final String NAME = "Z3";
  public static final String VERSION = staticGetVersion();

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String getVersion() {
    return VERSION;
  }

  private static String staticGetVersion() {
    try {
      return com.microsoft.z3.Version.getString();
    } catch (Throwable e) {
      return UNKNOWN_VERSION;
    }
  }
}

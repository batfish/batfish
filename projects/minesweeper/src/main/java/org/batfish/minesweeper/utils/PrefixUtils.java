package org.batfish.minesweeper.utils;

import java.util.Collection;
import javax.annotation.Nullable;
import org.batfish.datamodel.Prefix;

public class PrefixUtils {

  public static boolean isContainedBy(Prefix p, @Nullable Collection<Prefix> ps) {
    if (ps == null) {
      return false;
    }
    for (Prefix p2 : ps) {
      if (p2.containsPrefix(p)) {
        return true;
      }
    }
    return false;
  }
}

package org.batfish.minesweeper;

import com.google.common.collect.ImmutableList;
import java.util.List;

/**
 * The OSPF type of a message. This is either intra area (O), inter area (OIA), external type 1
 * (E1), or external type 2 (E2)
 */
public enum OspfType {
  O,
  OIA,
  E1,
  E2;

  public static final List<OspfType> values = ImmutableList.of(O, OIA, E1, E2);

  @Override
  public String toString() {
    return switch (this) {
      case O -> "O";
      case OIA -> "O IA";
      case E1 -> "O E1";
      case E2 -> "O E2";
    };
  }
}

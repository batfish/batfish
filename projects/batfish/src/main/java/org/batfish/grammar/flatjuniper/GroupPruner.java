package org.batfish.grammar.flatjuniper;

import java.util.stream.Collectors;
import org.antlr.v4.runtime.tree.ParseTree;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Flat_juniper_configurationContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Set_lineContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Set_line_tailContext;

/**
 * Removes all {@code set groups} lines in the given configuration.
 *
 * <p>Note that this is specialized to the Juniper grammar, and needs to be kept in sync with
 * changes to set.
 */
public final class GroupPruner {
  public static void prune(Flat_juniper_configurationContext ctx) {
    ctx.children = ctx.children.stream().filter(c -> !isGroupLine(c)).collect(Collectors.toList());
  }

  private static boolean isGroupLine(ParseTree tree) {
    if (!(tree instanceof Set_lineContext)) {
      return false;
    }
    Set_lineContext set = (Set_lineContext) tree;
    Set_line_tailContext tail = set.set_line_tail();
    if (tail == null) {
      return false;
    }
    // The only place that s_groups is located in the configuration is set_line > set_line_tail.
    return tail.s_groups() != null;
  }
}

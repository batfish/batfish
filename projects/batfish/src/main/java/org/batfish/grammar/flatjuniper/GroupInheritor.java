package org.batfish.grammar.flatjuniper;

import org.batfish.grammar.flatjuniper.FlatJuniperParser.Flat_juniper_configurationContext;

/**
 * Preprocessing stage that merges in lines from applied groups into the {@link Hierarchy}'s main
 * tree according to the rules of Juniper inheritance.
 */
public final class GroupInheritor {

  /**
   * Inherit applied group lines from the group trees in hierachy {@code h} into the main tree
   * therein. Return {@code true} iff the hierarchy was modified.
   */
  public static boolean inheritGroups(Hierarchy h, Flat_juniper_configurationContext ctx) {
    boolean modified = h.inheritGroups(ctx);
    if (modified) {
      ctx.children = h.extractParseTrees();
    }
    return modified;
  }

  private GroupInheritor() {}
}

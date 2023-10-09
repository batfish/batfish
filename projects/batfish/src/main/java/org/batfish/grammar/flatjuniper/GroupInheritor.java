package org.batfish.grammar.flatjuniper;

import org.batfish.grammar.flatjuniper.FlatJuniperParser.Flat_juniper_configurationContext;

public final class GroupInheritor {

  public static boolean inheritGroups(Hierarchy h, Flat_juniper_configurationContext ctx) {
    boolean modified = h.inheritGroups(ctx);
    if (modified) {
      ctx.children = h.extractParseTrees();
    }
    return modified;
  }

  private GroupInheritor() {}
}

package org.batfish.grammar.flatjuniper;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.*;

public class GroupPruner extends FlatJuniperParserBaseListener {

   private Flat_juniper_configurationContext _configurationContext;

   private boolean _isGroupsLine;

   private List<ParseTree> _newConfigurationLines;

   @Override
   public void enterFlat_juniper_configuration(
         Flat_juniper_configurationContext ctx) {
      _configurationContext = ctx;
      _newConfigurationLines = new ArrayList<ParseTree>();
      _newConfigurationLines.addAll(ctx.children);
   }

   @Override
   public void exitFlat_juniper_configuration(
         Flat_juniper_configurationContext ctx) {
      _configurationContext.children = _newConfigurationLines;
   }

   public void exitS_groups(S_groupsContext ctx) {
      _isGroupsLine = true;
   }

   @Override
   public void exitSet_line(Set_lineContext ctx) {
      if (_isGroupsLine) {
         _newConfigurationLines.remove(ctx);
      }
      _isGroupsLine = false;
   }

}

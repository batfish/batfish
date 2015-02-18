package org.batfish.grammar.flatjuniper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.grammar.ControlPlaneExtractor;
import org.batfish.representation.VendorConfiguration;
import org.batfish.representation.juniper.JuniperVendorConfiguration;

public class FlatJuniperControlPlaneExtractor implements ControlPlaneExtractor {

   private JuniperVendorConfiguration _configuration;
   private final FlatJuniperCombinedParser _parser;
   private final boolean _pedanticAsError;
   private final boolean _pedanticRecord;
   private final List<String> _pedanticWarnings;
   private final boolean _printParseTree;
   private final boolean _redFlagAsError;
   private final boolean _redFlagRecord;
   private final List<String> _redFlagWarnings;
   private final Set<String> _rulesWithSuppressedWarnings;
   private final String _text;
   private final boolean _unimplementedAsError;
   private final boolean _unimplementedRecord;
   private final List<String> _unimplementedWarnings;

   public FlatJuniperControlPlaneExtractor(String fileText,
         FlatJuniperCombinedParser combinedParser,
         Set<String> rulesWithSuppressedWarnings, boolean redFlagRecord,
         boolean redFlagAsError, boolean unimplementedRecord,
         boolean unimplementedAsError, boolean pedanticRecord,
         boolean pedanticAsError, boolean printParseTree) {
      _text = fileText;
      _parser = combinedParser;
      _rulesWithSuppressedWarnings = rulesWithSuppressedWarnings;
      _pedanticAsError = pedanticAsError;
      _pedanticRecord = pedanticRecord;
      _pedanticWarnings = new ArrayList<String>();
      _redFlagAsError = redFlagAsError;
      _redFlagRecord = redFlagRecord;
      _redFlagWarnings = new ArrayList<String>();
      _unimplementedAsError = unimplementedAsError;
      _unimplementedRecord = unimplementedRecord;
      _unimplementedWarnings = new ArrayList<String>();
      _printParseTree = printParseTree;
   }

   @Override
   public List<String> getPedanticWarnings() {
      return _pedanticWarnings;
   }

   @Override
   public List<String> getRedFlagWarnings() {
      return _redFlagWarnings;
   }

   @Override
   public List<String> getUnimplementedWarnings() {
      return _unimplementedWarnings;
   }

   @Override
   public VendorConfiguration getVendorConfiguration() {
      return _configuration;
   }

   @Override
   public void processParseTree(ParserRuleContext tree) {
      Hierarchy hierarchy = new Hierarchy();
      ParseTreeWalker walker = new ParseTreeWalker();
      InitialTreeBuilder tb = new InitialTreeBuilder(hierarchy);
      walker.walk(tb, tree);
      ApplyGroupsApplicator hb = new ApplyGroupsApplicator(_parser, hierarchy,
            _redFlagWarnings, _redFlagRecord, _redFlagAsError);
      walker.walk(hb, tree);
      GroupPruner gp = new GroupPruner();
      walker.walk(gp, tree);
      WildcardApplicator wa = new WildcardApplicator(hierarchy);
      walker.walk(wa, tree);
      WildcardPruner wp = new WildcardPruner();
      walker.walk(wp, tree);
      ApplyPathApplicator ap = new ApplyPathApplicator(hierarchy);
      walker.walk(ap, tree);
      ConfigurationBuilder cb = new ConfigurationBuilder(_parser, _text,
            _rulesWithSuppressedWarnings, _redFlagRecord, _redFlagAsError,
            _unimplementedRecord, _unimplementedAsError, _pedanticRecord,
            _pedanticAsError, _printParseTree);
      walker.walk(cb, tree);
      _configuration = cb.getConfiguration();
   }

}

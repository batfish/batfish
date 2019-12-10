package org.batfish.grammar.mrv;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.Warnings;
import org.batfish.grammar.BatfishParseTreeWalker;
import org.batfish.grammar.ControlPlaneExtractor;
import org.batfish.grammar.mrv.MrvParser.A_system_systemnameContext;
import org.batfish.grammar.mrv.MrvParser.Mrv_configurationContext;
import org.batfish.grammar.mrv.MrvParser.NsdeclContext;
import org.batfish.grammar.mrv.MrvParser.Quoted_stringContext;
import org.batfish.representation.mrv.MrvConfiguration;
import org.batfish.vendor.VendorConfiguration;

public class MrvControlPlaneExtractor extends MrvParserBaseListener
    implements ControlPlaneExtractor {

  private MrvConfiguration _configuration;

  private MrvCombinedParser _parser;

  private String _text;

  private Warnings _w;

  public MrvControlPlaneExtractor(String fileText, MrvCombinedParser mrvParser, Warnings warnings) {
    _text = fileText;
    _parser = mrvParser;
    _w = warnings;
  }

  @Override
  public void enterMrv_configuration(Mrv_configurationContext ctx) {
    _configuration = new MrvConfiguration();
  }

  @Override
  public void exitA_system_systemname(A_system_systemnameContext ctx) {
    String hostname = toString(ctx.nsdecl());
    _configuration.setHostname(hostname);
  }

  private String getFullText(ParserRuleContext ctx) {
    int start = ctx.getStart().getStartIndex();
    int end = ctx.getStop().getStopIndex();
    return _text.substring(start, end + 1);
  }

  private String getText(Quoted_stringContext ctx) {
    if (ctx.text != null) {
      return ctx.text.getText();
    } else {
      return "";
    }
  }

  @Override
  public VendorConfiguration getVendorConfiguration() {
    return _configuration;
  }

  @Override
  public void processParseTree(NetworkSnapshot snapshot, ParserRuleContext tree) {
    ParseTreeWalker walker = new BatfishParseTreeWalker(_parser);
    walker.walk(this, tree);
  }

  @SuppressWarnings("unused")
  private void todo(ParserRuleContext ctx) {
    _w.todo(ctx, getFullText(ctx), _parser);
  }

  private String toString(NsdeclContext ctx) {
    String text = getText(ctx.quoted_string());
    return text;
  }
}

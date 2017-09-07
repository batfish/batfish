package org.batfish.grammar.netscreen;

import java.util.Set;
import java.util.TreeSet;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.common.Warnings;
import org.batfish.grammar.ControlPlaneExtractor;
import org.batfish.grammar.netscreen.NetscreenParser.Netscreen_configurationContext;
import org.batfish.grammar.netscreen.NetscreenParser.S_hostnameContext;
import org.batfish.representation.netscreen.NetscreenConfiguration;
import org.batfish.vendor.VendorConfiguration;

public class NetscreenControlPlaneExtractor extends NetscreenParserBaseListener
    implements ControlPlaneExtractor {

  private NetscreenConfiguration _configuration;

  private NetscreenCombinedParser _parser;

  private String _text;

  private final Set<String> _unimplementedFeatures;

  private Warnings _w;

  public NetscreenControlPlaneExtractor(String fileText, NetscreenCombinedParser mrvParser,
      Warnings warnings) {
    _text = fileText;
    _parser = mrvParser;
    _w = warnings;
    _unimplementedFeatures = new TreeSet<>();
  }

  @Override
  public void enterNetscreen_configuration(Netscreen_configurationContext ctx) {
    _configuration = new NetscreenConfiguration();
  }

  @Override
  public void exitS_hostname(S_hostnameContext ctx) {
    String hostname = (ctx.variable().VARIABLE() != null)
        ? ctx.variable().VARIABLE().getText()
        : ctx.variable().QUOTED_TEXT().getText();
    _configuration.setHostname(hostname);
  }

  @Override
  public Set<String> getUnimplementedFeatures() {
    return _unimplementedFeatures;
  }

  @Override
  public VendorConfiguration getVendorConfiguration() {
    return _configuration;
  }

  @Override
  public void processParseTree(ParserRuleContext tree) {
    ParseTreeWalker walker = new ParseTreeWalker();
    walker.walk(this, tree);
  }

  @SuppressWarnings("unused")
  private void todo(ParserRuleContext ctx, String feature) {
    _w.todo(ctx, feature, _parser, _text);
    _unimplementedFeatures.add("Netscreen: " + feature);
  }
}

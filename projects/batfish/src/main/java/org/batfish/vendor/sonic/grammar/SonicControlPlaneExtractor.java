package org.batfish.vendor.sonic.grammar;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Set;
import javax.annotation.Nonnull;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.ErrorDetails;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.Warnings;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.grammar.BatfishParseException;
import org.batfish.grammar.BatfishParseTreeWalker;
import org.batfish.grammar.ControlPlaneExtractor;
import org.batfish.grammar.ImplementedRules;
import org.batfish.grammar.frr.FrrCombinedParser;
import org.batfish.grammar.frr.FrrConfigurationBuilder;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.vendor.VendorConfiguration;
import org.batfish.vendor.sonic.representation.ConfigDb;
import org.batfish.vendor.sonic.representation.SonicConfiguration;

public class SonicControlPlaneExtractor implements ControlPlaneExtractor {
  private @Nonnull final String _configDbText;
  private @Nonnull final String _frrFileText;
  private @Nonnull final FrrCombinedParser _frrParser;
  private @Nonnull final Warnings _frrWarnings;
  private @Nonnull final SilentSyntaxCollection _frrSilentSyntax;

  private @Nonnull final SonicConfiguration _configuration;

  public SonicControlPlaneExtractor(
      String configDbText,
      String frrText,
      FrrCombinedParser frrParser,
      Warnings frrWarnings,
      SilentSyntaxCollection frrSilentSyntax) {
    _configDbText = configDbText;
    _frrFileText = frrText;
    _frrParser = frrParser;
    _frrWarnings = frrWarnings;
    _frrSilentSyntax = frrSilentSyntax;
    _configuration = new SonicConfiguration();
  }

  @Override
  public VendorConfiguration getVendorConfiguration() {
    return _configuration;
  }

  @Override
  public Set<String> implementedRuleNames() {
    return ImplementedRules.getImplementedRules(FrrConfigurationBuilder.class);
  }

  @Override
  public void processParseTree(NetworkSnapshot snapshot, ParserRuleContext tree) {

    try {
      ConfigDb configDb =
          BatfishObjectMapper.ignoreUnknownMapper().readValue(_configDbText, ConfigDb.class);
      _configuration.setConfigDb(configDb);
      configDb.getHostname().ifPresent(_configuration::setHostname);
    } catch (JsonProcessingException exception) {
      // completely fail now, instead of trying making sense of frr file. if configdb.json parsing
      // failed, the generated node will be useless anyway. this way the user will notice something
      // is amiss more easily.
      throw new BatfishParseException(
          "Error parsing configdb file",
          exception,
          new ErrorDetails("File could not be deserialized as a configdb object"));
    }

    FrrConfigurationBuilder cb =
        new FrrConfigurationBuilder(
            _configuration, _frrParser, _frrWarnings, _frrFileText, _frrSilentSyntax);
    new BatfishParseTreeWalker(_frrParser).walk(cb, tree);
  }
}

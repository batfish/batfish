package org.batfish.grammar.cumulus_ports;

import org.batfish.representation.cumulus.CumulusNcluConfiguration;

/**
 * Populates a {@link CumulusNcluConfiguration} from the data in a {@link
 * org.batfish.grammar.cumulus_ports.CumulusPortsCombinedParser cumulus ports file parse tree}.
 */
public class CumulusPortsConfigurationBuilder extends CumulusPortsParserBaseListener {
  private final CumulusNcluConfiguration _config;

  public CumulusPortsConfigurationBuilder(CumulusNcluConfiguration config) {
    _config = config;
  }
}

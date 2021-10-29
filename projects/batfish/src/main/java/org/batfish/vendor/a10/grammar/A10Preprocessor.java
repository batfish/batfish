package org.batfish.vendor.a10.grammar;

import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.vendor.a10.representation.A10Configuration;

/**
 * Given a parse tree, extracts metadata for an {@link A10Configuration}.
 *
 * <p>This includes extracting things like ethernet interface default enable/disable status.
 */
public final class A10Preprocessor extends A10ParserBaseListener {

  @Override
  public void exitA10_configuration(A10Parser.A10_configurationContext ctx) {
    _c.setEthernetDefaultEnable(inferEthernetDefaultEnable());
  }

  @Override
  public void enterSid_ethernet(A10Parser.Sid_ethernetContext ctx) {
    _currentInterfaceEthernet = true;
  }

  @Override
  public void exitSid_ethernet(A10Parser.Sid_ethernetContext ctx) {
    _currentInterfaceEthernet = false;
  }

  @Override
  public void exitSid_enable(A10Parser.Sid_enableContext ctx) {
    if (_currentInterfaceEthernet) {
      _ethernetEnableCount++;
    }
  }

  @Override
  public void exitSid_disable(A10Parser.Sid_disableContext ctx) {
    if (_currentInterfaceEthernet) {
      _ethernetDisableCount++;
    }
  }

  /**
   * Infer ethernet interface default enable status. Returns {@code false} if ethernet interfaces
   * appear to default to disable, otherwise returns {@code true}.
   */
  private boolean inferEthernetDefaultEnable() {
    // If there are explicit enables and no explicit disables, then assume the default is disable
    return !(_ethernetEnableCount > 0 && _ethernetDisableCount == 0);
  }

  public A10Preprocessor(
      A10CombinedParser parser, String text, Warnings warnings, A10Configuration configuration) {
    _parser = parser;
    _text = text;
    _w = warnings;
    _c = configuration;
  }

  /** Number of ethernet interfaces that are explicitly 'disable' */
  private int _ethernetDisableCount;

  /** Number of ethernet interfaces that are explicitly 'enable' */
  private int _ethernetEnableCount;

  /** Indicates if the current interface is an ethernet interface */
  private boolean _currentInterfaceEthernet;

  @Nonnull private A10Configuration _c;

  @Nonnull private A10CombinedParser _parser;

  @Nonnull private final String _text;

  @Nonnull private final Warnings _w;
}

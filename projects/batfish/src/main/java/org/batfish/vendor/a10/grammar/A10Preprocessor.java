package org.batfish.vendor.a10.grammar;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.batfish.vendor.a10.representation.A10Configuration;

/**
 * Given a parse tree, extracts metadata for an {@link A10Configuration}.
 *
 * <p>This includes extracting things like ethernet interface default enable/disable status.
 */
public final class A10Preprocessor extends A10ParserBaseListener {

  @Override
  public void exitA10_configuration(A10Parser.A10_configurationContext ctx) {
    _c.setMajorVersionNumber(getAcosMajorVersionNumber());
  }

  /** Infer ACOS version from the configuration text. */
  private Integer getAcosMajorVersionNumber() {
    Matcher matcher = ACOS_VERSION_PATTERN.matcher(_text);
    if (!matcher.find()) {
      return null;
    }
    return Integer.parseUnsignedInt(matcher.group(1));
  }

  public A10Preprocessor(String text, A10Configuration configuration) {
    _text = text;
    _c = configuration;
  }

  /**
   * Pattern matching ACOS version strings, in leading comments in config dumps. Extracts the major
   * version number in match group 1.
   */
  private static Pattern ACOS_VERSION_PATTERN = Pattern.compile("version (\\d+).\\d+.\\d+");

  @Nonnull private A10Configuration _c;

  @Nonnull private String _text;
}

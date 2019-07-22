package org.batfish.grammar;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

@SuppressWarnings("unused") // In case we decide to bring back extraction of "show ip route" output
public final class RoutingTableFormatDetector {

  public static RoutingTableFormat identifyRoutingTableFormat(String fileText) {
    return new RoutingTableFormatDetector(fileText).identifyRoutingTableFormat();
  }

  private String _fileText;

  @SuppressWarnings("unused")
  private char _firstChar;

  private RoutingTableFormatDetector(String fileText) {
    _fileText = fileText;
  }

  @Nullable
  private RoutingTableFormat checkEmpty() {
    String trimmedText = _fileText.trim();
    if (trimmedText.length() == 0) {
      return RoutingTableFormat.EMPTY;
    }
    _firstChar = trimmedText.charAt(0);
    return null;
  }

  @Nullable
  private RoutingTableFormat checkEos() {
    Matcher eosMatcher =
        Pattern.compile("(?m)Codes: C - connected, S - static, K - kernel,").matcher(_fileText);
    if (eosMatcher.find()) {
      return RoutingTableFormat.EOS;
    }
    return null;
  }

  @Nullable
  private RoutingTableFormat checkIos() {
    Matcher iosMatcher =
        Pattern.compile(
                "(?m)Codes: L - local, C - connected, S - static, R - RIP, M - mobile, B - BGP")
            .matcher(_fileText);
    if (iosMatcher.find()) {
      return RoutingTableFormat.IOS;
    }
    return null;
  }

  @Nullable
  private RoutingTableFormat checkNxos() {
    Matcher nxosMatcher = Pattern.compile("(?m)IP Route Table for VRF \"").matcher(_fileText);
    if (nxosMatcher.find()) {
      return RoutingTableFormat.NXOS;
    }
    return null;
  }

  private RoutingTableFormat identifyRoutingTableFormat() {
    RoutingTableFormat format;
    format = checkEmpty();
    if (format != null) {
      return format;
    }
    format = checkEos();
    if (format != null) {
      return format;
    }
    format = checkIos();
    if (format != null) {
      return format;
    }
    format = checkNxos();
    if (format != null) {
      return format;
    }
    return RoutingTableFormat.UNKNOWN;
  }
}

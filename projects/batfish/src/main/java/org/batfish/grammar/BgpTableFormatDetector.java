package org.batfish.grammar;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.collections.BgpAdvertisementsByVrf;

public final class BgpTableFormatDetector {

  public static BgpTableFormat identifyBgpTableFormat(String fileText) {
    return new BgpTableFormatDetector(fileText).identifyBgpTableFormat();
  }

  private String _fileText;

  @SuppressWarnings("unused")
  private char _firstChar;

  private BgpTableFormatDetector(String fileText) {
    _fileText = fileText;
  }

  @Nullable
  private BgpTableFormat checkEmpty() {
    String trimmedText = _fileText.trim();
    if (trimmedText.length() == 0) {
      return BgpTableFormat.EMPTY;
    }
    _firstChar = trimmedText.charAt(0);
    return null;
  }

  @Nullable
  private BgpTableFormat checkEos() {
    Matcher eosMatcher =
        Pattern.compile("(?m)BGP routing table information for VRF").matcher(_fileText);
    Matcher detailMatcher = Pattern.compile("(?m)BGP routing table entry for").matcher(_fileText);
    if (eosMatcher.find()) {
      if (detailMatcher.find()) {
        return BgpTableFormat.EOS_DETAIL;
      } else {
        return BgpTableFormat.EOS;
      }
    }
    return null;
  }

  private BgpTableFormat identifyBgpTableFormat() {
    BgpTableFormat format;
    format = checkEmpty();
    if (format != null) {
      return format;
    }
    format = checkEos();
    if (format != null) {
      return format;
    }
    try {
      BatfishObjectMapper.mapper().readValue(_fileText, BgpAdvertisementsByVrf.class);
      return BgpTableFormat.JSON;
    } catch (IOException e) {
      return BgpTableFormat.UNKNOWN;
    }
  }
}

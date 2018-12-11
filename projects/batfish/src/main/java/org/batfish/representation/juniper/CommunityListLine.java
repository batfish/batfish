package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class CommunityListLine implements Serializable {

  private static final Pattern LITERAL_STANDARD_COMMUNITY_PATTERN =
      Pattern.compile("(\\d+):(\\d+)");

  private static final long serialVersionUID = 1L;

  public static @Nullable Long literalCommunityValue(String str) {
    Matcher m = LITERAL_STANDARD_COMMUNITY_PATTERN.matcher(str);
    if (!m.matches()) {
      return null;
    }
    Long high = shortValue(m.group(1));
    if (high == null) {
      return null;
    }
    Long low = shortValue(m.group(2));
    if (low == null) {
      return null;
    }
    return (high << 16) | low;
  }

  private static @Nullable Long shortValue(String str) {
    long val = Long.parseLong(str);
    if (val > 0xFFFFL) {
      return null;
    }
    return val;
  }

  private String _text;

  public CommunityListLine(String text) {
    _text = text;
  }

  public String getText() {
    return _text;
  }
}

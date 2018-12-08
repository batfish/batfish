package org.batfish.representation.juniper;

import static org.batfish.datamodel.StandardCommunity.literalCommunityValue;

import java.io.Serializable;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.WellKnownCommunity;

@ParametersAreNonnullByDefault
public final class CommunityListLine implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private String _regex;

  public CommunityListLine(String regex) {
    _regex = regex;
  }

  public String getRegex() {
    return _regex;
  }

  /**
   * If {@code _regex} represents a literal community, returns the long value of that community.
   * Else, returns {@code null}.
   */
  public @Nullable Long juniperLiteralCommunityValue() {
    switch (_regex) {
      case "no-advertise":
        return WellKnownCommunity.NO_ADVERTISE;
      case "no-export":
        return WellKnownCommunity.NO_EXPORT;
      case "no-export-subconfed":
        return WellKnownCommunity.NO_EXPORT_SUBCONFED;
      default:
        return literalCommunityValue(_regex);
    }
  }
}

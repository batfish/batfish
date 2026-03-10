package org.batfish.vendor.arista.representation;

import java.io.Serializable;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.batfish.datamodel.LineAction;

public class ExpandedCommunityListLine implements Serializable {

  private LineAction _action;
  private String _regex;

  public static Optional<ExpandedCommunityListLine> create(LineAction action, String regex) {
    try {
      Pattern.compile(AristaConversions.toJavaRegex(regex));
    } catch (PatternSyntaxException e) {
      return Optional.empty();
    }
    return Optional.of(new ExpandedCommunityListLine(action, regex));
  }

  private ExpandedCommunityListLine(LineAction action, String regex) {
    _action = action;
    _regex = regex;
  }

  public LineAction getAction() {
    return _action;
  }

  public String getRegex() {
    return _regex;
  }
}

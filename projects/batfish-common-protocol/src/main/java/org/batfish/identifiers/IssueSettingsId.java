package org.batfish.identifiers;

import javax.annotation.Nullable;

public class IssueSettingsId extends Id {

  private final String _majorIssue;

  public IssueSettingsId(String id, @Nullable String majorIssue) {
    super(id);
    _majorIssue = majorIssue;
  }

  public @Nullable String getMajorIssue() {
    return _majorIssue;
  }
}

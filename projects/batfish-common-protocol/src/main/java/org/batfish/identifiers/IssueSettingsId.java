package org.batfish.identifiers;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class IssueSettingsId extends Id {

  public IssueSettingsId(String id) {
    super(id);
  }

  @Override
  public IdType getType() {
    return IdType.ISSUE_SETTINGS;
  }
}

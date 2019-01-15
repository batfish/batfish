package org.batfish.representation.juniper;

import org.batfish.datamodel.LineAction;

/** Represents the action part of a Juniper screen ids-option */
public class ScreenActionDrop implements ScreenAction {

  /** */
  private static final long serialVersionUID = 1L;

  private static final String DROP = "drop";

  public static final ScreenActionDrop INSTANCE = new ScreenActionDrop();

  private ScreenActionDrop() {}

  @Override
  public String getName() {
    return DROP;
  }

  @Override
  public LineAction toAclLineAction() {
    return LineAction.DENY;
  }
}

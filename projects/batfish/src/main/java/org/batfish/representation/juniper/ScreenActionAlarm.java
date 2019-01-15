package org.batfish.representation.juniper;

import org.batfish.datamodel.LineAction;

/** Represents the action part of a Juniper screen ids-option */
public class ScreenActionAlarm implements ScreenAction {

  /** */
  private static final long serialVersionUID = 1L;

  private static final String ALARM = "alarm_without_drop";

  public static final ScreenActionAlarm INSTANCE = new ScreenActionAlarm();

  private ScreenActionAlarm() {}

  @Override
  public String getName() {
    return ALARM;
  }

  @Override
  public LineAction toAclLineAction() {
    return LineAction.PERMIT;
  }
}

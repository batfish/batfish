package org.batfish.datamodel.flow;

/** Types of actions which can be taken at the end of a {@link Step} */
public enum StepAction {
  BLOCKED,
  DROPPED,
  FORWARDED,
  ACCEPTED,
  RECEIVED,
  TRANSMITTED
}

package projects.minesweeper.src.main.java.org.batfish.minesweeper.question.semdiff;

/** Describes the route-map permit/deny actions in the reference/current snapshots. */
public enum ActionDifference {
  ReferencePermitCurrentDeny,
  ReferenceDenyCurrentPermit,
  PermitPermit
}

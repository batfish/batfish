package org.batfish.question.filterlinereachability;

public interface UnreachableFilterLineVisitor<T> {
  T visitBlockedFilterLine(BlockedFilterLine line);

  T visitIndependentlyUnmatchableFilterLine(IndependentlyUnmatchableFilterLine line);

  T visitFilterLineWithUndefinedReference(FilterLineWithUndefinedReference line);
}

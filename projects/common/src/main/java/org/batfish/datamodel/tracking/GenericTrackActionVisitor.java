package org.batfish.datamodel.tracking;

/** Visitor for {@link TrackAction} */
public interface GenericTrackActionVisitor {
  void visitDecrementPriority(DecrementPriority decrementPriority);
}

package org.batfish.datamodel.tracking;

/** Visitor for {@link TrackAction} */
public interface GenericTrackActionVisitor<R> {
  R visitDecrementPriority(DecrementPriority decrementPriority);
}

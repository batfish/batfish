package org.batfish.datamodel.tracking;

/** Visitor for {@link TrackAction} */
public interface GenericTrackActionVisitor<R> {

  default R visit(TrackAction trackAction) {
    return trackAction.apply(this);
  }

  R visitDecrementPriority(DecrementPriority decrementPriority);
}

package org.batfish.datamodel.tracking;

/** An evaluator for {@link TrackMethod}s that returns {@code true} iff the track should succeed. */
public interface TrackMethodEvaluator extends GenericTrackMethodVisitor<Boolean> {}

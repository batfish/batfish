package org.batfish.datamodel.tracking;

/** Visitor for {@link TrackMethod} */
public interface GenericTrackMethodVisitor<R> {
  R visitTrackInterface(TrackInterface trackInterface);
}

package org.batfish.datamodel.tracking;

/** Visitor for {@link TrackMethod} */
public interface GenericTrackMethodVisitor<R> {
  default R visit(TrackMethod trackMethod) {
    return trackMethod.accept(this);
  }

  R visitTrackInterface(TrackInterface trackInterface);

  R visitTrackRoute(TrackRoute trackRoute);
}

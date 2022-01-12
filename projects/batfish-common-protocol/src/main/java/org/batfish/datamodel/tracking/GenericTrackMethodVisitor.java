package org.batfish.datamodel.tracking;

/** Visitor for {@link TrackMethod} */
public interface GenericTrackMethodVisitor<R> {
  default R visit(TrackMethod trackMethod) {
    return trackMethod.accept(this);
  }

  R visitNegatedTrackMethod(NegatedTrackMethod negatedTrackMethod);

  R visitTrackInterface(TrackInterface trackInterface);

  R visitTrackMethodReference(TrackMethodReference trackMethodReference);

  R visitTrackRoute(TrackRoute trackRoute);
}

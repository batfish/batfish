package org.batfish.datamodel.tracking;

/** Visitor for {@link TrackMethod} */
public interface GenericTrackMethodVisitor<R> {
  default R visit(TrackMethod trackMethod) {
    return trackMethod.accept(this);
  }

  R visitNegatedTrackMethod(NegatedTrackMethod negatedTrackMethod);

  R visitTrackAll(TrackAll trackAll);

  R visitTrackInterface(TrackInterface trackInterface);

  R visitTrackMethodReference(TrackMethodReference trackMethodReference);

  R visitTrackReachability(TrackReachability trackReachability);

  R visitTrackRoute(TrackRoute trackRoute);

  R visitTrackTrue(TrackTrue trackTrue);
}

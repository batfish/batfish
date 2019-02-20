package org.batfish.datamodel.transformation;

/** Used to indicate which IP address is transformed by a {@link TransformationStep}. */
public enum IpField {
  SOURCE,
  DESTINATION;

  public IpField opposite() {
    switch (this) {
      case SOURCE:
        return DESTINATION;
      case DESTINATION:
        return SOURCE;
      default:
        throw new IllegalArgumentException("Unknown IpField " + this);
    }
  }
}

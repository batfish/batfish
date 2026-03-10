package org.batfish.datamodel.transformation;

/** Used to indicate which IP address is transformed by a {@link TransformationStep}. */
public enum IpField {
  SOURCE,
  DESTINATION;

  public IpField opposite() {
    return switch (this) {
      case SOURCE -> DESTINATION;
      case DESTINATION -> SOURCE;
    };
  }
}

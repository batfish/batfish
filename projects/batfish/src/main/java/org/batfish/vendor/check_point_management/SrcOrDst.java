package org.batfish.vendor.check_point_management;

/** An object representing a space of IPs (includes {@link CpmiAnyObject}). */
public interface SrcOrDst {
  <T> T accept(SrcOrDstVisitor<T> visitor);
}

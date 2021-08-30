package org.batfish.vendor.check_point_management;

/**
 * A concrete IP space object, e.g. an {@link AddressSpace}. Does not include {@link CpmiAnyObject}.
 */
public interface ConcreteSrcOrDst extends SrcOrDst {
  <T> T accept(ConcreteSrcOrDstVisitor<T> visitor);

  @Override
  default <T> T accept(SrcOrDstVisitor<T> visitor) {
    return accept((ConcreteSrcOrDstVisitor<T>) visitor);
  }
}

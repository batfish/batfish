package org.batfish.vendor.check_point_management;

/** Visitor for {@link SrcOrDst} */
public interface SrcOrDstVisitor<T> extends ConcreteSrcOrDstVisitor<T> {
  default T visit(SrcOrDst srcOrDst) {
    return srcOrDst.accept(this);
  }

  T visitCpmiAnyObject(CpmiAnyObject cpmiAnyObject);
}

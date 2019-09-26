package org.batfish.representation.f5_bigip;

import java.io.Serializable;

/** Configuration for an OSPF area within a process. */
public final class OspfArea implements Serializable {

  // https://techdocs.f5.com/content/kb/en-us/products/big-ip_ltm/manuals/related/ospf-commandreference-7-10-4/_jcr_content/pdfAttach/download/file.res/arm-ospf-command-reference-7-10-4.pdf
  private static final int DEFAULT_DEFAULT_COST = 1;

  /**
   * Default cost for a default summary route sent into a stub or NSSA area.
   *
   * @see <a
   *     href="https://techdocs.f5.com/content/kb/en-us/products/big-ip_ltm/manuals/related/ospf-commandreference-7-10-4/_jcr_content/pdfAttach/download/file.res/arm-ospf-command-reference-7-10-4.pdf">F5
   *     docs</a>
   */
  public static int defaultDefaultCost() {
    return DEFAULT_DEFAULT_COST;
  }

  public OspfArea(long id) {
    _id = id;
  }

  /** Cost for a default summary route sent into a stub or NSSA area. */
  public int getDefaultCost() {
    return _defaultCost;
  }

  public void setDefaultCost(int defaultCost) {
    _defaultCost = defaultCost;
  }

  public long getId() {
    return _id;
  }

  private int _defaultCost;
  private final long _id;
}

package org.batfish.representation.juniper;

import com.google.common.collect.Iterables;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.TcpFlagsMatchConditions;

public final class FwFromTcpFlags extends FwFrom {

  /** */
  private static final long serialVersionUID = 1L;

  private List<TcpFlagsMatchConditions> _tcpFlags;

  public FwFromTcpFlags(List<TcpFlagsMatchConditions> tcpFlags) {
    _tcpFlags = tcpFlags;
  }

  @Override
  public void applyTo(
      HeaderSpace.Builder headerSpaceBuilder,
      JuniperConfiguration jc,
      Warnings w,
      Configuration c) {
    headerSpaceBuilder.setTcpFlags(Iterables.concat(headerSpaceBuilder.getTcpFlags(), _tcpFlags));
  }
}

package org.batfish.representation.cisco;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.SubRange;

public class TcpServiceObjectGroupLine implements ServiceObjectGroupLine {

  /** */
  private static final long serialVersionUID = 1L;

  private final List<SubRange> _ports;

  public TcpServiceObjectGroupLine(@Nonnull List<SubRange> ports) {
    _ports = ImmutableList.copyOf(requireNonNull(ports));
  }

  public List<SubRange> getPorts() {
    return _ports;
  }
}

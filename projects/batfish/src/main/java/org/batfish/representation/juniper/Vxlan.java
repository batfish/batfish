package org.batfish.representation.juniper;

import java.util.List;
import java.io.Serializable;
import java.util.SortedMap;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class Vxlan implements Serializable {

  public static final Integer DEFAULT_UDP_PORT = 4789;

  public @Nullable String getExtendedVniAll() {
    return _extended_vni_all;
  }

  public @Nullable List<Integer> getExtendedVniList() {
    return _extended_vni_list;
  }

  public Integer getUdpPort() {
    return _udpPort;
  }

  //////////////////////////////////////////
  ///// Private implementation details /////
  //////////////////////////////////////////

  private Integer _udpPort;
  private SortedMap<Integer, Integer> _vlanVnis;
  private String _sourceInterface;
  private @Nullable String _extended_vni_all;
  private @Nullable List<Integer> _extended_vni_list;
  private @Nullable String _rd;
  private @Nullable String _exportRt;
  private @Nullable String _importRt;

  @Nonnull SortedMap<String, Integer> _vrfToVni;
}
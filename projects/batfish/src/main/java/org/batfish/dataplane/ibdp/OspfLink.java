package org.batfish.dataplane.ibdp;

import org.batfish.datamodel.ospf.OspfArea;

class OspfLink {

  final VirtualRouter _remoteVirtualRouter;

  final OspfArea _localOspfArea;

  final OspfArea _remoteOspfArea;

  public OspfLink(
      OspfArea localOspfArea, OspfArea remoteOspfArea, VirtualRouter remoteVirtualRouter) {
    _localOspfArea = localOspfArea;
    _remoteOspfArea = remoteOspfArea;
    _remoteVirtualRouter = remoteVirtualRouter;
  }
}

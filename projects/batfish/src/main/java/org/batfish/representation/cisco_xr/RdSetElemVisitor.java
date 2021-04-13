package org.batfish.representation.cisco_xr;

import javax.annotation.ParametersAreNonnullByDefault;

/** A visitor of {@link RdSetElem} that takes 1 generic argument and returns a generic value. */
@ParametersAreNonnullByDefault
public interface RdSetElemVisitor<T, U> {

  T visitRdSetAsDot(RdSetAsDot rdSetAsDot);

  T visitRdSetDfaRegex(RdSetDfaRegex rdSetDfaRegex);

  T visitRdSetIosRegex(RdSetIosRegex rdSetIosRegex);

  T visitRdSetAsPlain16(RdSetAsPlain16 rdSetAsPlain16);

  T visitRdSetAsPlain32(RdSetAsPlain32 rdSetAsPlain32);

  T visitRdSetIpPrefix(RdSetIpPrefix rdSetIpPrefix);

  T visitRdSetIpAddress(RdSetIpAddress rdSetIpAddress);
}

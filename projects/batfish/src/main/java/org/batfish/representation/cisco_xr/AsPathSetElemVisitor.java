package org.batfish.representation.cisco_xr;

/**
 * A visitor of {@link AsPathSetElem} that takes a generic argument and returns a generic argument.
 */
public interface AsPathSetElemVisitor<T, U> {

  T visitDfaRegexAsPathSetElem(DfaRegexAsPathSetElem dfaRegexAsPathSetElem, U arg);

  T visitIosRegexAsPathSetElem(IosRegexAsPathSetElem iosRegexAsPathSetElem, U arg);

  T visitLengthAsPathSetElem(LengthAsPathSetElem lengthAsPathSetElem, U arg);

  T visitNeighborIsAsPathSetElem(NeighborIsAsPathSetElem neighborIsAsPathSetElem, U arg);

  T visitOriginatesFromAsPathSetElem(
      OriginatesFromAsPathSetElem originatesFromAsPathSetElem, U arg);

  T visitPassesThroughAsPathSetElem(PassesThroughAsPathSetElem passesThroughAsPathSetElem, U arg);

  T visitUniqueLengthAsPathSetElem(UniqueLengthAsPathSetElem uniqueLengthAsPathSetElem, U arg);
}

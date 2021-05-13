package org.batfish.representation.cisco_xr;

/** A visitor of {@link AsPathSetElem} that returns a generic argument. */
public interface AsPathSetElemVisitor<T> {

  T visitDfaRegexAsPathSetElem(DfaRegexAsPathSetElem dfaRegexAsPathSetElem);

  T visitIosRegexAsPathSetElem(IosRegexAsPathSetElem iosRegexAsPathSetElem);

  T visitLengthAsPathSetElem(LengthAsPathSetElem lengthAsPathSetElem);

  T visitNeighborIsAsPathSetElem(NeighborIsAsPathSetElem neighborIsAsPathSetElem);

  T visitOriginatesFromAsPathSetElem(OriginatesFromAsPathSetElem originatesFromAsPathSetElem);

  T visitPassesThroughAsPathSetElem(PassesThroughAsPathSetElem passesThroughAsPathSetElem);

  T visitUniqueLengthAsPathSetElem(UniqueLengthAsPathSetElem uniqueLengthAsPathSetElem);
}

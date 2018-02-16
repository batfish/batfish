package org.batfish.z3;

import com.microsoft.z3.Z3Exception;

public interface QuerySynthesizer {

  boolean getNegate();

  NodProgram getNodProgram(SynthesizerInput input, NodProgram baseProgram) throws Z3Exception;
}

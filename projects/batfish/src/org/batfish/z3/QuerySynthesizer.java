package org.batfish.z3;

import com.microsoft.z3.Z3Exception;

public interface QuerySynthesizer {

   boolean getNegate();

   NodProgram getNodProgram(NodProgram baseProgram) throws Z3Exception;

   String getQueryText();

}

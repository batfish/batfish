package org.batfish.z3;

import java.util.ArrayList;
import java.util.List;

import com.microsoft.z3.Context;

public abstract class FirstUnsatQuerySynthesizer<Key, Result>
      extends BaseQuerySynthesizer {

   protected final Key _key;

   protected final List<Result> _resultsByQueryIndex;

   public FirstUnsatQuerySynthesizer(Key key) {
      _key = key;
      _resultsByQueryIndex = new ArrayList<>();
   }

   public Key getKey() {
      return _key;
   }

   public List<Result> getResultsByQueryIndex() {
      return _resultsByQueryIndex;
   }

   public abstract NodProgram synthesizeBaseProgram(Synthesizer synthesizer,
         Context ctx);

}

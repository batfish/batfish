package org.batfish.z3;

import java.util.ArrayList;
import java.util.List;

import com.microsoft.z3.Context;

public abstract class SatQuerySynthesizer<Key> extends BaseQuerySynthesizer {

   protected final List<Key> _keys;

   public SatQuerySynthesizer() {
      _keys = new ArrayList<Key>();
   }

   public List<Key> getKeys() {
      return _keys;
   }

   public abstract NodProgram synthesizeBaseProgram(Synthesizer synthesizer,
         Context ctx);

}

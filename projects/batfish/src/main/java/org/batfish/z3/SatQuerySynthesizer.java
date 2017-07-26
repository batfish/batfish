package org.batfish.z3;

import com.microsoft.z3.Context;
import java.util.ArrayList;
import java.util.List;

public abstract class SatQuerySynthesizer<KeyT> extends BaseQuerySynthesizer {

  protected final List<KeyT> _keys;

  public SatQuerySynthesizer() {
    _keys = new ArrayList<>();
  }

  public List<KeyT> getKeys() {
    return _keys;
  }

  public abstract NodProgram synthesizeBaseProgram(Synthesizer synthesizer, Context ctx);
}

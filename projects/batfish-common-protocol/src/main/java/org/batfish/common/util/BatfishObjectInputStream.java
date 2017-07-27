package org.batfish.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

public class BatfishObjectInputStream extends ObjectInputStream {

  private ClassLoader _loader;

  public BatfishObjectInputStream(InputStream in, ClassLoader loader) throws IOException {
    super(in);
    _loader = loader;
  }

  @Override
  protected Class<?> resolveClass(ObjectStreamClass osc)
      throws IOException, ClassNotFoundException {
    try {
      String name = osc.getName();
      return Class.forName(name, false, _loader);
    } catch (ClassNotFoundException e) {
      return super.resolveClass(osc);
    }
  }
}

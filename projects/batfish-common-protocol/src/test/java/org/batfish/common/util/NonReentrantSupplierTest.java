package org.batfish.common.util;

import com.google.common.base.Suppliers;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.batfish.common.util.NonReentrantSupplier.NonReentrantSupplierException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class NonReentrantSupplierTest {
  @Rule public ExpectedException exception = ExpectedException.none();

  @Test
  public void testNonReentrantSupplier() {
    Map<String, Supplier<String>> suppliers = new HashMap<>();
    suppliers.put("foo", new NonReentrantSupplier<>(() -> suppliers.get("bar").get()));
    suppliers.put("bar", new NonReentrantSupplier<>(() -> suppliers.get("foo").get()));
    exception.expect(NonReentrantSupplierException.class);
    suppliers.get("foo").get();
  }

  @Test
  public void testMemoize() {
    Map<String, Supplier<String>> suppliers = new HashMap<>();
    suppliers.put(
        "foo", Suppliers.memoize(new NonReentrantSupplier<>(() -> suppliers.get("bar").get())));
    suppliers.put(
        "bar", Suppliers.memoize(new NonReentrantSupplier<>(() -> suppliers.get("foo").get())));
    exception.expect(NonReentrantSupplierException.class);
    suppliers.get("foo").get();
  }
}

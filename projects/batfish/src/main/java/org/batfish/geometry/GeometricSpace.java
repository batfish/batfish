package org.batfish.geometry;

import java.util.ArrayList;
import java.util.List;

public class GeometricSpace {

  private List<EquivalenceClass> _rectangles;

  GeometricSpace(List<EquivalenceClass> rectangles) {
    this._rectangles = rectangles;
  }

  public static GeometricSpace singleton(EquivalenceClass r) {
    List<EquivalenceClass> rects = new ArrayList<>();
    rects.add(r);
    return new GeometricSpace(rects);
  }

  List<EquivalenceClass> rectangles() {
    return this._rectangles;
  }
}

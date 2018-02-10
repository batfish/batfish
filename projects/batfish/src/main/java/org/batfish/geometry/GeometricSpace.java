package org.batfish.geometry;

import java.util.ArrayList;
import java.util.List;

public class GeometricSpace {

  private List<HyperRectangle> _rectangles;

  GeometricSpace(List<HyperRectangle> rectangles) {
    this._rectangles = rectangles;
  }

  public static GeometricSpace singleton(HyperRectangle r) {
    List<HyperRectangle> rects = new ArrayList<>();
    rects.add(r);
    return new GeometricSpace(rects);
  }

  List<HyperRectangle> rectangles() {
    return this._rectangles;
  }
}

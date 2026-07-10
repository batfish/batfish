package org.batfish.specifier.parse;

interface FilterAstNodeVisitor<T> {
  T visitUnionFilterAstNode(UnionFilterAstNode unionFilterAstNode);

  T visitDifferenceFilterAstNode(DifferenceFilterAstNode differenceFilterAstNode);

  T visitIntersectionFilterAstNode(IntersectionFilterAstNode intersectionFilterAstNode);

  T visitNameFilterAstNode(NameFilterAstNode nameFilterAstNode);

  T visitNameRegexFilterAstNode(NameRegexFilterAstNode nameRegexFilterAstNode);

  T visitInFilterAstNode(InFilterAstNode inFilterAstNode);

  T visitOutFilterAstNode(OutFilterAstNode outFilterAstNode);

  T visitFilterWithNodeFilterAstNode(FilterWithNodeFilterAstNode filterWithNodeFilterAstNode);
}

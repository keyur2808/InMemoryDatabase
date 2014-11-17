package edu.buffalo.cse562.queryoptimizer;

import edu.buffalo.cse562.operator.*;

public interface OperatorVisitor {
	public void visit(FromOperator op);
	public void visit(SelectionOperator op);
	public void visit(JoinOperator op);
	public void visit(ProjectionOperator op);
	public void visit(OrderByOperator op);
	public void visit(RenameOperator op);
	public void visit(GroupByOperator op);
	public void visit(LimitOperator op);
	public void visit(DifferenceOperator differenceOperator);
	public void visit(DistinctOperator distinctOperator);
	public void visit(EquiJoinOperator equiJoinOperator);
	public void visit(ExternalSortOperator externalSortOperator);
	public void visit(IntersectionOperator intersectionOperator);
	public void visit(UnionOperator unionOperator);
}

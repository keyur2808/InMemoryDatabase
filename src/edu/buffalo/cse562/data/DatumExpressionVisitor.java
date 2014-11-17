package edu.buffalo.cse562.data;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;

public interface DatumExpressionVisitor{
	public void visit(ForDate arg0);
	public void visit(ForString arg0);
	public void visit(ForLong arg0);
	public void visit(ForInteger arg0);	
	public void visit(ForDouble arg0);
}

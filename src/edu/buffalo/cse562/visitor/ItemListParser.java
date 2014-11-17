package edu.buffalo.cse562.visitor;

import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitor;
import net.sf.jsqlparser.statement.select.SubSelect;

public class ItemListParser implements ItemsListVisitor {

	@Override
	public void visit(SubSelect arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(ExpressionList arg0) {
		// TODO Auto-generated method stub
	/*	ExpressionParser expVisit = new ExpressionParser();
		List<Expression> expressionList = arg0.getExpressions();
		for(Expression exp : expressionList){
			exp.accept(expVisit);
		}*/
	}
}

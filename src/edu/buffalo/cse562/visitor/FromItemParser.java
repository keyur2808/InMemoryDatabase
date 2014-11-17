package edu.buffalo.cse562.visitor;

import edu.buffalo.cse562.operator.BlockNestedJoinOperator;
import edu.buffalo.cse562.operator.FromOperator;
import edu.buffalo.cse562.operator.Operator;
import edu.buffalo.cse562.operator.RenameOperator;
import edu.buffalo.cse562.operator.SelectionOperator;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;

public class FromItemParser implements FromItemVisitor{

	private Operator operator = null;
	private Column[] schema = null;
	
	public Operator getOperator(){
		return this.operator;
	}
	
	public Column[] getSchema(){
		return schema;
	}
	@Override
	public void visit(Table table) {
		
		operator = new FromOperator(table);
		String alias = table.getAlias();
		if(alias != null){
			operator = new RenameOperator(operator, alias);
		}
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(SubSelect subSelect) {
		// TODO Auto-generated method stub
//		System.out.println("Sub select");
		SelectParser sp = new SelectParser();
		subSelect.getSelectBody().accept(sp);
		operator = sp.getOperator();
		String alias = subSelect.getAlias();
		if(alias != null){
			operator = new RenameOperator(operator,alias);
		}
	}

	@Override
	public void visit(SubJoin subJoin) {
		// TODO Auto-generated method stub
		//System.out.println("Sub join");
		FromItemParser leftFip = new FromItemParser();
		subJoin.getLeft().accept(leftFip);
		Operator leftOperator = leftFip.getOperator();
		FromItemParser rightFip =new FromItemParser();
		Join join = subJoin.getJoin();
		join.getRightItem().accept(rightFip);
		Operator rightOperator = rightFip.getOperator();
		operator = new BlockNestedJoinOperator(leftOperator, rightOperator);
		Expression onExp = join.getOnExpression();
		operator = new SelectionOperator(onExp,operator);
		//join.getUsingColumns();
	}
}

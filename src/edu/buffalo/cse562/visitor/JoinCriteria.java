package edu.buffalo.cse562.visitor;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;

public class JoinCriteria {
	private Table leftTable = null;
	private Table rightTable = null;
	private Column leftColumn = null;
	private Column rightColumn = null;
	private Expression expression = null;
	private boolean isEquiJoin = false;
	
	public JoinCriteria(Table leftTable,Table rightTable,Expression expression,Column leftColumn, Column rightColumn, boolean isEquiJoin){
		this.leftTable = leftTable;
		this.rightTable = rightTable;
		this.leftColumn = leftColumn;
		this.rightColumn = rightColumn;
		this.expression = expression;
		this.isEquiJoin = isEquiJoin;
	}
	
	public Table getLeftTable(){
		return this.leftTable;
	}
	
	public Table getRightTable(){
		return this.rightTable;
	}
	
	public Expression getCondition(){
		return this.expression;
	}
	
	public boolean getIsEquiJoin(){
		return isEquiJoin;
	}

	public Column getLeftColumn() {
		return leftColumn;
	}

	public Column getRightColumn() {
		return rightColumn;
	}
}

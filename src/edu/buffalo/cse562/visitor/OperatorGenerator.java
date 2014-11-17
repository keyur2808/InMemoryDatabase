package edu.buffalo.cse562.visitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.buffalo.cse562.operator.BlockNestedJoinOperator;
import edu.buffalo.cse562.operator.EquiJoinOperator;
import edu.buffalo.cse562.operator.Operator;
import edu.buffalo.cse562.operator.SelectionOperator;
import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.InverseExpression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.SubSelect;

public class OperatorGenerator implements ExpressionVisitor {
	private Operator operator = null;
	private Set<Table> tableList = null;
	private List<Expression> expressionList = null;
	private Map<String, Operator> tableMap = null;
	private List<JoinCriteria> joinList = null;
	private Map<String, List<JoinCriteria>> expressionMap = null;
	private Column column = null;
	private Table table;
	private boolean isColumn = false;

	public OperatorGenerator(Set<Table> tables, List<Expression> expressionList) {
		this.tableList = tables;
		this.expressionList = expressionList;
		tableMap = new HashMap<String, Operator>();
		joinList = new ArrayList<JoinCriteria>();
		this.expressionMap = new HashMap<String, List<JoinCriteria>>();

		parseTableList();
		parseExpressionList();
		parseJoinExpression();
	}

	public void parseJoinExpression() {
		Iterator<String> expIt = expressionMap.keySet().iterator();
		while (expIt.hasNext()) {
			String tableName = expIt.next();
			List<JoinCriteria> expList = expressionMap.get(tableName);
			Set<String> tableList = new HashSet<String>();
			Operator op = tableMap.get(tableName);
			Column leftColumn = null;
			Column rightColumn = null;
			for (JoinCriteria jc : expList) {
				String otherTableName = jc.getLeftTable().getName();
				if (otherTableName.equalsIgnoreCase(tableName)) {
					otherTableName = jc.getRightTable().getName();
					leftColumn = jc.getLeftColumn();
					rightColumn = jc.getRightColumn();
				} else {
					rightColumn = jc.getLeftColumn();
					leftColumn = jc.getRightColumn();
				}
				Operator rightOperator = tableMap.get(otherTableName);
				if (jc.getIsEquiJoin()) {
					op = new EquiJoinOperator(op, rightOperator, leftColumn,
							rightColumn);
					// op = new JoinOperator(op, rightOperator);
					// op = new SelectionOperator(jc.getCondition(), op);
				} else {
					op = new BlockNestedJoinOperator(op, rightOperator);
					op = new SelectionOperator(jc.getCondition(), op);
				}
				tableList.add(jc.getLeftTable().getName());
				tableList.add(jc.getRightTable().getName());
			}
			Iterator<String> tableIt = tableList.iterator();
			while (tableIt.hasNext()) {
				String modTableName = tableIt.next();
				tableMap.put(modTableName, op);
			}
			operator = op;
		}
	}

	public void parseJoin() {
		Iterator<JoinCriteria> it = joinList.iterator();
		while (it.hasNext()) {
			JoinCriteria jc = it.next();
			Operator leftOperator = tableMap.get(jc.getLeftTable().getName());
			Operator rightOperator = tableMap.get(jc.getRightTable().getName());
			Operator joinOp = new BlockNestedJoinOperator(leftOperator, rightOperator);
			operator = new SelectionOperator(jc.getCondition(), joinOp);
			leftOperator = operator;
			rightOperator = operator;
			tableMap.put(jc.getLeftTable().getName(), operator);
			tableMap.put(jc.getRightTable().getName(), operator);
		}

	}

	public void parseTableList() {
		Iterator<Table> tableIt = tableList.iterator();
		while (tableIt.hasNext()) {
			Table table = tableIt.next();
			// System.out.println(table.getName());
			FromItemParser fip = new FromItemParser();
			table.accept(fip);
			Operator operator = fip.getOperator();
			// System.out.println(operator);
			tableMap.put(table.getName(), operator);
		}
	}

	public void parseExpressionList() {
		Iterator<Expression> expressionIt = expressionList.iterator();
		while (expressionIt.hasNext()) {
			isColumn = false;
			Expression expression = expressionIt.next();
			expression.accept(this);
		}
	}

	public Operator getOperator() {
		return operator;
	}

	@Override
	public void visit(NullValue arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Function arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(InverseExpression arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(JdbcParameter arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(DoubleValue arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(LongValue arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(DateValue arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(TimeValue arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(TimestampValue arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Parenthesis arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(StringValue arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Addition arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Division arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Multiplication arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Subtraction arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(AndExpression arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OrExpression arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Between arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(EqualsTo arg0) {
		// TODO Auto-generated method stub
		Expression leftExp = arg0.getLeftExpression();
		leftExp.accept(this);
		Table leftTable = table;
		boolean leftIsColumn = isColumn;
		isColumn = false;
		Column leftColumn = column;
		table = null;
		column = null;
		Expression rightExp = arg0.getRightExpression();
		rightExp.accept(this);
		Table rightTable = table;
		boolean rightIsColumn = isColumn;
		Column rightColumn = column;
		associateExpression(leftIsColumn, rightIsColumn, rightTable, leftTable,
				leftColumn, rightColumn, arg0, true);

	}

	@Override
	public void visit(GreaterThan arg0) {
		// TODO Auto-generated method stub
		Expression leftExp = arg0.getLeftExpression();
		leftExp.accept(this);
		Table leftTable = table;
		boolean leftIsColumn = isColumn;
		isColumn = false;
		Column leftColumn = column;
		table = null;
		column = null;
		Expression rightExp = arg0.getRightExpression();
		rightExp.accept(this);
		Table rightTable = table;
		boolean rightIsColumn = isColumn;
		Column rightColumn = column;
		associateExpression(leftIsColumn, rightIsColumn, rightTable, leftTable,
				leftColumn, rightColumn, arg0, false);

	}

	@Override
	public void visit(GreaterThanEquals arg0) {
		// TODO Auto-generated method stub
		Expression leftExp = arg0.getLeftExpression();
		leftExp.accept(this);
		Table leftTable = table;
		boolean leftIsColumn = isColumn;
		isColumn = false;
		Column leftColumn = column;
		table = null;
		column = null;
		Expression rightExp = arg0.getRightExpression();
		rightExp.accept(this);
		Table rightTable = table;
		boolean rightIsColumn = isColumn;
		Column rightColumn = column;
		associateExpression(leftIsColumn, rightIsColumn, rightTable, leftTable,
				leftColumn, rightColumn, arg0, false);

	}

	private void associateExpression(boolean leftIsColumn,
			boolean rightIsColumn, Table rightTable, Table leftTable,
			Column leftColumn, Column rightColumn, Expression exp,
			boolean isEquijoin) {
		// TODO Auto-generated method stub
		if (rightIsColumn && leftIsColumn) {
			// join on the expression
			if (leftTable.getName().equals(rightTable.getName())) {
				Operator op = tableMap.get(leftTable.getName());
				op = new SelectionOperator(exp, op);
				tableMap.put(leftTable.getName(), op);
			} else {
				JoinCriteria jc = new JoinCriteria(leftTable, rightTable, exp,
						leftColumn, rightColumn, isEquijoin);
				joinList.add(jc);
				if (expressionMap.containsKey(leftTable.getName())) {
					List<JoinCriteria> leftExpList = expressionMap
							.get(leftTable.getName());
					leftExpList.add(jc);
				} else {
					List<JoinCriteria> leftExpList = new ArrayList<JoinCriteria>();
					leftExpList.add(jc);
					expressionMap.put(leftTable.getName(), leftExpList);
				}
			}
		} else if (rightIsColumn) {
			Operator op = tableMap.get(rightTable.getName());
			op = new SelectionOperator(exp, op);
			tableMap.put(rightTable.getName(), op);
		} else if (leftIsColumn) {
			Operator op = tableMap.get(leftTable.getName());
			op = new SelectionOperator(exp, op);
			tableMap.put(leftTable.getName(), op);
		}
	}

	@Override
	public void visit(InExpression arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(IsNullExpression arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(LikeExpression arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(MinorThan arg0) {
		// TODO Auto-generated method stub
		Expression leftExp = arg0.getLeftExpression();
		leftExp.accept(this);
		Table leftTable = table;
		boolean leftIsColumn = isColumn;
		isColumn = false;
		Column leftColumn = column;
		table = null;
		column = null;
		Expression rightExp = arg0.getRightExpression();
		rightExp.accept(this);
		Table rightTable = table;
		boolean rightIsColumn = isColumn;
		Column rightColumn = column;
		associateExpression(leftIsColumn, rightIsColumn, rightTable, leftTable,
				leftColumn, rightColumn, arg0, false);

	}

	@Override
	public void visit(MinorThanEquals arg0) {
		// TODO Auto-generated method stub
		Expression leftExp = arg0.getLeftExpression();
		leftExp.accept(this);
		Table leftTable = table;
		boolean leftIsColumn = isColumn;
		isColumn = false;
		Column leftColumn = column;
		table = null;
		column = null;
		Expression rightExp = arg0.getRightExpression();
		rightExp.accept(this);
		Table rightTable = table;
		boolean rightIsColumn = isColumn;
		Column rightColumn = column;
		associateExpression(leftIsColumn, rightIsColumn, rightTable, leftTable,
				leftColumn, rightColumn, arg0, false);

	}

	@Override
	public void visit(NotEqualsTo arg0) {
		Expression leftExp = arg0.getLeftExpression();
		leftExp.accept(this);
		Table leftTable = table;
		boolean leftIsColumn = isColumn;
		isColumn = false;
		Column leftColumn = column;
		table = null;
		column = null;
		Expression rightExp = arg0.getRightExpression();
		rightExp.accept(this);
		Table rightTable = table;
		boolean rightIsColumn = isColumn;
		Column rightColumn = column;
		associateExpression(leftIsColumn, rightIsColumn, rightTable, leftTable,
				leftColumn, rightColumn, arg0, false);
	}

	@Override
	public void visit(Column arg0) {
		isColumn = true;
		table = arg0.getTable();
		column = arg0;
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(SubSelect arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(CaseExpression arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(WhenClause arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(ExistsExpression arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(AllComparisonExpression arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(AnyComparisonExpression arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Concat arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Matches arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(BitwiseAnd arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(BitwiseOr arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(BitwiseXor arg0) {
		// TODO Auto-generated method stub

	}
}

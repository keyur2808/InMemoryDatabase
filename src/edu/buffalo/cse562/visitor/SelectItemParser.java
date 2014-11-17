package edu.buffalo.cse562.visitor;

import java.util.List;
import java.util.Map;

import edu.buffalo.cse562.configuration.SchemaConfiguration;
import edu.buffalo.cse562.data.Datum;
import edu.buffalo.cse562.data.ForDate;
import edu.buffalo.cse562.data.ForDouble;
import edu.buffalo.cse562.data.ForLong;
import edu.buffalo.cse562.data.ForString;
import edu.buffalo.cse562.data.Tuple;
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
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;
import net.sf.jsqlparser.statement.select.SubSelect;

public class SelectItemParser implements SelectItemVisitor, ExpressionVisitor {

	private Tuple tuple = null;
	private Map<String, Integer> schema = null;
	private Tuple outputTuple = null;
	private Datum accumulator = null;
	private boolean isExpression = false;
	
	public SelectItemParser(Map<String,Integer> schema){
		this.schema = schema;
	}
	
	public void reset(){
		isExpression = false;
		accumulator = null;
	}
	
	public void setInputTuple(Tuple tuple){
		reset();
		this.tuple = tuple;
		outputTuple = new Tuple();
	}

	public SelectItemParser(Tuple tuple, Map<String, Integer> schema) {
		this.tuple = tuple;
		this.schema = schema;
	}

	public Tuple getResult() {
		return outputTuple;
	}

	@Override
	public void visit(AllColumns allColumns) {
		// TODO Auto-generated method stub
		outputTuple = tuple;

	}

	@SuppressWarnings("rawtypes")
	@Override
	public void visit(AllTableColumns tableColumns) {
		// TODO Auto-generated method stub
		Table table = tableColumns.getTable();
		CreateTable tableInfo = SchemaConfiguration.getInstance().getSchema(
				table.getName());
		List columnDef = tableInfo.getColumnDefinitions();
		for (int i = 0; i < columnDef.size(); i++) {
			ColumnDefinition colDef = (ColumnDefinition) columnDef.get(i);
			Column column = new Column(table, colDef.getColumnName());
			if (schema.containsKey(column.getWholeColumnName())) {
				int index = schema.get(column.getWholeColumnName());
				outputTuple.addValue(tuple.getValue(index));
			}

		}
	}

	@Override
	public void visit(SelectExpressionItem selExpItem) {
		isExpression = false;
		Expression exp = selExpItem.getExpression();
		// System.out.println(exp);
		exp.accept(this);
		if(isExpression){
			if(selExpItem.getAlias() != null){
					accumulator = tuple.getValue(schema.get(selExpItem.getAlias()));
			}else{
				accumulator = tuple.getValue(schema.get(selExpItem.toString()));
			}
		}
		outputTuple.addValue(accumulator);

	//	System.out.println(tuple);
		// System.out.println("SelectExpItem");
		
		// System.out.println(accumulator);
		// TODO Auto-generated method stub
	}

/*	public List<Column> getColumns() {
		// TODO Auto-generated method stub
		return columnList;
	}*/

	@Override
	public void visit(NullValue arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Function function) {
		// TODO Auto-generated method stub
		String functionName = function.getName();
		if (functionName.equals("sum")) {
			isExpression = true; 
		} else if (functionName.equals("avg")) {
			isExpression = true;
		} else if (functionName.equals("count")) {
			isExpression = true;
		} else if (functionName.equals("min")) {
			isExpression = true;
		} else if (functionName.equals("max")) {
			isExpression = true;
		}
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
		accumulator = new ForDouble(arg0.getValue());

	}

	@Override
	public void visit(LongValue arg0) {
		// TODO Auto-generated method stub
		accumulator = new ForLong(arg0.getValue());
	}

	@Override
	public void visit(DateValue arg0) {
		// TODO Auto-generated method stub
		accumulator = (new ForDate()).getDatum(arg0.toString());

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
		Expression exp = arg0.getExpression();
		exp.accept(this);
	}

	@Override
	public void visit(StringValue arg0) {
		// TODO Auto-generated method stub
		StringBuffer sb = new StringBuffer(arg0.toString());
		sb.deleteCharAt(sb.length() - 1);
		sb.deleteCharAt(0);
		accumulator = (new ForString()).getDatum(arg0.toString());
	}

	@Override
	public void visit(Addition arg0) {
		Expression leftExpression = arg0.getLeftExpression();
		leftExpression.accept(this);
		Datum leftValue = accumulator;
		Expression rightExpression = arg0.getRightExpression();
		rightExpression.accept(this);
		Datum rightValue = accumulator;
		if(leftValue instanceof ForLong && rightValue instanceof ForDouble){
			accumulator = rightValue.add(leftValue);
		}else{
			accumulator = leftValue.add(rightValue);
		}

	}

	@Override
	public void visit(Division arg0) {
		Expression leftExpression = arg0.getLeftExpression();
		leftExpression.accept(this);
		Datum leftValue = accumulator;
		Expression rightExpression = arg0.getRightExpression();
		rightExpression.accept(this);
		Datum rightValue = accumulator;
		accumulator = leftValue.div(rightValue);
	}

	@Override
	public void visit(Multiplication arg0) {
		Expression leftExpression = arg0.getLeftExpression();
		leftExpression.accept(this);
		Datum leftValue = accumulator;
		Expression rightExpression = arg0.getRightExpression();
		rightExpression.accept(this);
		Datum rightValue = accumulator;
		accumulator = leftValue.mul(rightValue);
	}

	@Override
	public void visit(Subtraction arg0) {
		Expression leftExpression = arg0.getLeftExpression();
		leftExpression.accept(this);
		Datum leftValue = accumulator;
		Expression rightExpression = arg0.getRightExpression();
		rightExpression.accept(this);
		Datum rightValue = accumulator;
	//	System.out.println(leftValue);
		if(leftValue instanceof ForLong && rightValue instanceof ForDouble){
			accumulator = (rightValue.sub(leftValue)).mul(new ForDouble((double) -1));
		}else{
			accumulator = leftValue.sub(rightValue);
		}
	}

	@Override
	public void visit(AndExpression arg0) {
		Expression leftExpression = arg0.getLeftExpression();
		leftExpression.accept(this);
		Expression rightExpression = arg0.getRightExpression();
		rightExpression.accept(this);
	}

	@Override
	public void visit(OrExpression arg0) {
		Expression leftExpression = arg0.getLeftExpression();
		leftExpression.accept(this);
		Expression rightExpression = arg0.getRightExpression();
		rightExpression.accept(this);
	}

	@Override
	public void visit(Between arg0) {
		Expression leftExpression = arg0.getLeftExpression();
		leftExpression.accept(this);
		Expression start = arg0.getBetweenExpressionStart();
		start.accept(this);
		Expression end = arg0.getBetweenExpressionEnd();
		end.accept(this);
	}

	@Override
	public void visit(EqualsTo arg0) {
		Expression leftExpression = arg0.getLeftExpression();
		leftExpression.accept(this);
		Expression rightExpression = arg0.getRightExpression();
		rightExpression.accept(this);
	}

	@Override
	public void visit(GreaterThan arg0) {
		Expression leftExpression = arg0.getLeftExpression();
		leftExpression.accept(this);
		Expression rightExpression = arg0.getRightExpression();
		rightExpression.accept(this);
	}

	@Override
	public void visit(GreaterThanEquals arg0) {
		Expression leftExpression = arg0.getLeftExpression();
		leftExpression.accept(this);
		Expression rightExpression = arg0.getRightExpression();
		rightExpression.accept(this);
		// System.out.println("GTE::"+valid);
	}

	@Override
	public void visit(InExpression arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(IsNullExpression arg0) {
		Expression leftExpression = arg0.getLeftExpression();
		leftExpression.accept(this);
	}

	@Override
	public void visit(LikeExpression arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(MinorThan arg0) {
		Expression leftExpression = arg0.getLeftExpression();
		leftExpression.accept(this);
		Expression rightExpression = arg0.getRightExpression();
		rightExpression.accept(this);
		// System.out.println("MT::"+valid);
	}

	@Override
	public void visit(MinorThanEquals arg0) {
		Expression leftExpression = arg0.getLeftExpression();
		leftExpression.accept(this);
		Expression rightExpression = arg0.getRightExpression();
		rightExpression.accept(this);
	}

	@Override
	public void visit(NotEqualsTo arg0) {
		Expression leftExpression = arg0.getLeftExpression();
		leftExpression.accept(this);
		Expression rightExpression = arg0.getRightExpression();
		rightExpression.accept(this);
	}

	@Override
	public void visit(Column column) {
		// TODO Auto-generated method stub
		int i = schema.get(column.getWholeColumnName());
		accumulator = tuple.getValue(i);
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

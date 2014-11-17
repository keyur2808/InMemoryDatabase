package edu.buffalo.cse562.visitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
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
import net.sf.jsqlparser.statement.select.SubSelect;
import edu.buffalo.cse562.data.Datum;
import edu.buffalo.cse562.data.DatumExpressionVisitor;
import edu.buffalo.cse562.data.ForDate;
import edu.buffalo.cse562.data.ForDouble;
import edu.buffalo.cse562.data.ForInteger;
import edu.buffalo.cse562.data.ForLong;
import edu.buffalo.cse562.data.ForString;
import edu.buffalo.cse562.data.Tuple;
import edu.buffalo.cse562.operator.Operator;

public class ExpressionParser implements ExpressionVisitor,DatumExpressionVisitor {

	private Operator operator = null;	
	private ArrayList<Datum> values;	
	private Tuple tuple = null;
	private Map<String,Integer> schemaIndex = null;
	private boolean valid;
	
	public boolean getResult(){
		return valid;
	}
	
	public ExpressionParser(Map<String,Integer> schemaIndex){
		this.schemaIndex = schemaIndex;
		values = new ArrayList<Datum>();
	}
	
	public void setInputTuple(Tuple tuple){
		values = new ArrayList<Datum>();
		this.tuple = tuple;
	}
	
	public ExpressionParser(Tuple tup, Map<String,Integer> schemaIndex){
		this.tuple = tup;
		//this.schema = schema;
		this.schemaIndex = schemaIndex;
//		System.out.println(schema);
		values = new ArrayList<Datum>();
	}
	
	public Operator getOperator(){
		return this.operator;
	}
	
	@Override
	public void visit(NullValue arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Function function) {
		String name = function.getName();
		if(name.equals("date")){
			ExpressionList expList = function.getParameters();
			List<Expression> list = expList.getExpressions();
			//DateValue dv = new DateValue(list.get(0).toString());
			//dv.accept(this);
			StringBuffer sb = new StringBuffer(list.get(0).toString());
			sb.deleteCharAt(sb.length()-1);
			sb.deleteCharAt(0);

			values.add((new ForDate()).getDatum(sb.toString()));
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
		
		values.add((new ForDouble(arg0.getValue())));
		
	}

	@Override
	public void visit(LongValue arg0) {
		// TODO Auto-generated method stub
		values.add((new ForLong(arg0.getValue())));
	}

	@Override
	public void visit(DateValue arg0) {
		// TODO Auto-generated method stub
		values.add((new ForDate()).getDatum(arg0.getValue().toString()));
		
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
		if(arg0.isNot())
			valid=!getResult();
		else
			valid=getResult();
	}

	@Override
	public void visit(StringValue arg0) {
		// TODO Auto-generated method stub
		StringBuffer sb = new StringBuffer(arg0.toString());
		sb.deleteCharAt(sb.length()-1);
		sb.deleteCharAt(0);
		values.add((new ForString()).getDatum(sb.toString()));		
	}

	@Override
	public void visit(Addition arg0) {
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);	    
		Expression rightExpression=arg0.getRightExpression();
		rightExpression.accept(this);
		values.set(values.size()-2, values.get(values.size()-2).add(values.get(values.size()-1)));
		values.remove(values.size()-1);
	}

	@Override
	public void visit(Division arg0) {
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);
		Expression rightExpression=arg0.getRightExpression();
		rightExpression.accept(this);
		values.set(values.size()-2, values.get(values.size()-2).div(values.get(values.size()-1)));
		values.remove(values.size()-1);
	}

	@Override
	public void visit(Multiplication arg0) {
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);
		Expression rightExpression=arg0.getRightExpression();
		rightExpression.accept(this);
		values.set(values.size()-2, values.get(values.size()-2).mul(values.get(values.size()-1)));
		values.remove(values.size()-1);
	}

	@Override
	public void visit(Subtraction arg0) {
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);
		Expression rightExpression=arg0.getRightExpression();
		rightExpression.accept(this);
	//	System.out.println("left::"+values.get(values.size()-2));
	//	System.out.println("right::"+values.get(values.size()-1));
		Datum leftValue = values.get(values.size()-2);
		Datum rightValue = values.get(values.size()-1);
	//	if(leftValue instanceof ForLong && rightValue instanceof ForDouble){
	//		values.set(values.size()-2, rightValue.sub(leftValue).mul(new ForDouble((double) -1)));
	//	}else{
			values.set(values.size()-2, leftValue.sub(rightValue));
	//	}
		values.remove(values.size()-1);
	//	System.out.println("res::"+values.get(values.size()-1));
	}

	@Override
	public void visit(AndExpression arg0) {
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);
	    boolean temp = getResult();
	    if(temp){
	 //   	System.out.println("temp is true::");
		Expression rightExpression=arg0.getRightExpression();
		rightExpression.accept(this);
	//	System.out.println(rightExpression.toString());
		valid = (temp && getResult());
	    }
	}

	@Override
	public void visit(OrExpression arg0) {
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);
	    boolean temp = getResult();
	    if(!temp){
		Expression rightExpression=arg0.getRightExpression();
		rightExpression.accept(this);
		valid = (temp || getResult());
	    }
	}

	@Override
	public void visit(Between arg0) {
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);
	    Expression start = arg0.getBetweenExpressionStart();
	    start.accept(this);
	    Expression end = arg0.getBetweenExpressionEnd();
	    end.accept(this);
	    Datum tupVal = values.get(values.size()-3);
	    boolean temp = (tupVal.greaterThan(values.get(values.size()-2)) || tupVal.isEqualTo(values.get(values.size()-2)));
	    temp = temp && (tupVal.lesserThan(values.get(values.size()-1)) || tupVal.isEqualTo(values.get(values.size()-1)));
	    if(arg0.isNot())
	    	valid = !temp;
	    else
	    	valid = temp;
	    values.remove(values.size()-1);
	    values.remove(values.size()-1);
	    values.remove(values.size()-1);
	}

	@Override
	public void visit(EqualsTo arg0) {
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);
		Expression rightExpression=arg0.getRightExpression();
		rightExpression.accept(this);
		valid = values.get(values.size()-2).isEqualTo(values.get(values.size()-1));
//		System.out.println(values.get(values.size()-2));
//		System.out.println(values.get(values.size()-1));
//		System.out.println(valid);
		values.remove(values.size()-1);
		values.remove(values.size()-1);
	}

	@Override
	public void visit(GreaterThan arg0) {
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);
		Expression rightExpression=arg0.getRightExpression();
		rightExpression.accept(this);
		valid = values.get((values.size()-2)).greaterThan(values.get(values.size()-1));
		//System.out.println(values.get(values.size()-1));
		values.remove(values.size()-1);
		values.remove(values.size()-1);
	}

	@Override
	public void visit(GreaterThanEquals arg0) {
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);
		Expression rightExpression=arg0.getRightExpression();
		rightExpression.accept(this);
		int ret = values.get((values.size()-2)).compareTo(values.get(values.size()-1));
		valid = (ret != -1) ? true : false;

		values.remove(values.size()-1);
		values.remove(values.size()-1);
	}

	@Override
	public void visit(InExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(IsNullExpression arg0) {
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);
	    if(values.get(values.size()-1)==null)
	    	valid = true;
	    else
	    	valid = false;
	    values.remove(values.size()-1);		
	}

	@Override
	public void visit(LikeExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(MinorThan arg0) {
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);
		Expression rightExpression=arg0.getRightExpression();
		rightExpression.accept(this);
		valid = values.get((values.size()-2)).lesserThan(values.get(values.size()-1));
		//System.out.println(values.get(values.size()-1));
		values.remove(values.size()-1);
		values.remove(values.size()-1);
	}

	@Override
	public void visit(MinorThanEquals arg0) {
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);
		Expression rightExpression=arg0.getRightExpression();
		rightExpression.accept(this);
		int ret = values.get((values.size()-2)).compareTo(values.get(values.size()-1));
		valid = (ret != 1) ? true : false;
		values.remove(values.size()-1);
		values.remove(values.size()-1);
	}

	@Override
	public void visit(NotEqualsTo arg0) {
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);
		Expression rightExpression=arg0.getRightExpression();
		rightExpression.accept(this);
		valid = !((values.get((values.size()-2)).isEqualTo(values.get(values.size()-1))));
		values.remove(values.size()-1);
		values.remove(values.size()-1);
	}

	@Override
	public void visit(Column column) {		
		if(schemaIndex.containsKey(column.getWholeColumnName())){
			int i = schemaIndex.get(column.getWholeColumnName());
//			System.out.println("ndex::"+i);
		//	outputTuple.setValue(inputTuple.getValue(i));
			values.add(tuple.getValue(i));
//			System.out.println(inputTuple.getValue(i));
		}

/*		for(int i=0; i<schema.size(); i++){
			if(column.getWholeColumnName().equalsIgnoreCase(schema.get(i).getColumn().getWholeColumnName())){
			//	System.out.println("Whole column name");
				break;
			}else if(column.getColumnName().equalsIgnoreCase(schema.get(i).getColumn().getColumnName())){
				values.add(tuple.getValue(i));
				break;
			}
		}
*/		
	}

	@Override
	public void visit(SubSelect arg0) {
	//	System.out.println("SubSelect"+arg0);
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
		Expression leftExp = arg0.getLeftExpression();
		leftExp.accept(this);
		Expression rightExp = arg0.getRightExpression();
		rightExp.accept(this);
		ForString a = (ForString)values.get(values.size()-2);
		ForString b = (ForString)values.get(values.size()-2);
		values.add(a.concat(b));
	}

	@Override
	public void visit(Matches arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(BitwiseAnd arg0) {		
		Expression leftExp = arg0.getLeftExpression();
		leftExp.accept(this);
		Expression rightExp = arg0.getRightExpression();
		rightExp.accept(this);
		ForLong a = (ForLong)values.get(values.size()-2);
		ForLong b = (ForLong)values.get(values.size()-1);
		values.remove(values.size()-1);
		values.remove(values.size()-1);
		values.add(a.bitAnd(b));
	}

	@Override
	public void visit(BitwiseOr arg0) {
		Expression leftExp = arg0.getLeftExpression();
		leftExp.accept(this);
		Expression rightExp = arg0.getRightExpression();
		rightExp.accept(this);
		ForLong a = (ForLong)values.get(values.size()-2);
		ForLong b = (ForLong)values.get(values.size()-1);
		values.remove(values.size()-1);
		values.remove(values.size()-1);
		values.add(a.bitOr(b));		
	}

	@Override
	public void visit(BitwiseXor arg0) {
		Expression leftExp = arg0.getLeftExpression();
		leftExp.accept(this);
		Expression rightExp = arg0.getRightExpression();
		rightExp.accept(this);
		ForLong a = (ForLong)values.get(values.size()-2);
		ForLong b = (ForLong)values.get(values.size()-1);
		values.remove(values.size()-1);
		values.remove(values.size()-1);
		values.add(a.bitXor(b));		
	}
	
	@SuppressWarnings("unchecked")
	private void functionEvaluator(String name, Function function) {
		if(name.equalsIgnoreCase("DATE")){
			ExpressionList expList = function.getParameters();
			List<Expression> list = expList.getExpressions();
			DateValue dv = new DateValue(list.get(0).toString());
			dv.accept(this);
		}
	}

	@Override
	public void visit(ForDate arg0) {
		// TODO Auto-generated method stub
		values.add(arg0);
	}

	@Override
	public void visit(ForString arg0) {
		// TODO Auto-generated method stub
		values.add(arg0);
	}

	@Override
	public void visit(ForLong arg0) {
		// TODO Auto-generated method stub
		values.add(arg0);
	}

	@Override
	public void visit(ForInteger arg0) {
		// TODO Auto-generated method stub
		values.add(arg0);
	}

	@Override
	public void visit(ForDouble arg0) {
		// TODO Auto-generated method stub
		values.add(arg0);
	}
}

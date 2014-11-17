package edu.buffalo.cse562.operator;

import java.util.ArrayList;
import java.util.Map;

import edu.buffalo.cse562.configuration.ColumnDetails;
import edu.buffalo.cse562.data.Tuple;
import edu.buffalo.cse562.queryoptimizer.OperatorVisitor;

public abstract class JoinOperator implements Operator{

	protected Operator left;
	protected Operator right;
	private boolean isDone;
	private ArrayList<ColumnDetails> schema;

	public JoinOperator(Operator left,Operator right){
		this.left = left;
		this.right = right;
	}
	
	public Operator getLeftOperator(){
		return this.left;
	}
	
	public Operator getRightOperator(){
		return this.right;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		sb.append(left.toString());
		sb.append(" product ");
		sb.append(right.toString());
		sb.append("]");
		return sb.toString();
	}

	@Override
	public abstract Tuple readTuple();

	public void loadBuffer(boolean isLeft){
		if(isLeft){
		}
	}
	@Override
	public void reset() {
		left.reset();
		right.reset();
		schema = new ArrayList<ColumnDetails>();
		schema.addAll(left.getSchema());
		schema.addAll(right.getSchema());
		// Check left Operators table name and right operators table name 
	}

	@Override
	public boolean isDone() {
		return isDone;
		
	}
	
	public void clear(){
		
	}

	@Override
	public ArrayList<ColumnDetails> getSchema() {
		return schema;
	}

	@Override
	public Map<String, Integer> getSchemaIndex() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void accept(OperatorVisitor ov) {
		ov.visit(this);
	}
}

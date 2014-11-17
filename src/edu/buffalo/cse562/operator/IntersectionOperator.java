package edu.buffalo.cse562.operator;

import java.util.ArrayList;
import java.util.Map;

import edu.buffalo.cse562.configuration.ColumnDetails;
import edu.buffalo.cse562.data.Tuple;
import edu.buffalo.cse562.queryoptimizer.OperatorVisitor;

public class IntersectionOperator implements Operator{
	private Operator left;
	private Operator right;
	private boolean isDone;
	@SuppressWarnings("unused")
	private ArrayList<ColumnDetails> schema = null;

	
	public IntersectionOperator(Operator left,Operator right){
		this.left = left;
		this.right = right;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		sb.append(left.toString());
		sb.append(" intersect ");
		sb.append(right.toString());
		sb.append("]");
		return sb.toString();
	}

	@Override
	public Tuple readTuple() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isDone() {
		// TODO Auto-generated method stub
		return isDone;
		
	}
	
	public void clear(){
		
	}

	@Override
	public ArrayList<ColumnDetails> getSchema() {
		// TODO Auto-generated method stub
		this.schema = left.getSchema();
		return null;
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

	public Operator getLeftOperator() {
		// TODO Auto-generated method stub
		return this.left;
	}
	
	public Operator getRightOperator() {
		// TODO Auto-generated method stub
		return this.right;
	}
}

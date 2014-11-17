package edu.buffalo.cse562.operator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import edu.buffalo.cse562.configuration.ColumnDetails;
import edu.buffalo.cse562.data.Tuple;
import edu.buffalo.cse562.queryoptimizer.OperatorVisitor;

public class UnionOperator implements Operator {

	private Operator left;
	private Operator right;
	private boolean isDone;
	private boolean isAll;
	private ArrayList<ColumnDetails> leftSchema;
	private ArrayList<ColumnDetails> rightSchema;
	private LinkedHashMap<String,String> map;
	
	public UnionOperator(Operator left, Operator right,boolean isAll) {
		this.left = left;
		this.right = right;
		this.isAll=isAll;
		map=new LinkedHashMap<String,String>();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(left.toString());
		sb.append(" UNION ");
		if (isAll){sb.append(" ALL ");}
		sb.append(right.toString());
		return sb.toString();
	}

	public void loadSchema() {
		leftSchema = left.getSchema();
		rightSchema = right.getSchema();
	}

	public boolean compareSchema() {
		if (leftSchema.size() != rightSchema.size())
			return false;
		for (int i = 0; i < leftSchema.size(); i++) {
			ColumnDetails leftColDetails = leftSchema.get(i);
			ColumnDetails rightColDetails = rightSchema.get(i);
			if (leftColDetails.getColumnDefinition().getColDataType() != rightColDetails
					.getColumnDefinition().getColDataType()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Tuple readTuple() {
		if(left == null){
			return null;
		}
		
		if(right == null){
			return null;
		}
		
		Tuple tuple;
		tuple = left.readTuple();
		if (tuple!=null){
		if (isDistinct(tuple) && (!isAll))return tuple;
			if (isAll)return tuple;
		}
		tuple = right.readTuple();
		if (tuple!=null){
			if (isDistinct(tuple) && (!isAll) )return tuple;
			if (isAll)return tuple;
		}
		if (tuple!=null)return readTuple();
		return null;
		
	}

	
	private boolean isDistinct(Tuple tuple){
		if (!map.containsKey(tuple.toString())){ 
			   map.put(tuple.toString(),"*");
			   return true;
		   }
		 else
			   return false;
	}
	
	
	@Override
	public void reset() {
		left.reset();
		right.reset();
		loadSchema();
		compareSchema();
	}

	@Override
	public boolean isDone() {
		return isDone;

	}

	public void clear() {

	}

	@Override
	public ArrayList<ColumnDetails> getSchema() {
		return leftSchema;
	}

	@Override
	public Map<String, Integer> getSchemaIndex() {
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
		return this.right;
	}
}

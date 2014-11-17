package edu.buffalo.cse562.operator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.buffalo.cse562.configuration.ColumnDetails;
import edu.buffalo.cse562.data.Tuple;
import edu.buffalo.cse562.queryoptimizer.OperatorVisitor;

public class BlockNestedJoinOperator extends JoinOperator{
	private List<Tuple> rightBuffer = null;
	private List<Tuple> leftBuffer = null;
	private Iterator<Tuple> leftIterator = null;
	private Iterator<Tuple> rightIterator = null;
	private Tuple leftTuple = null;
	private boolean isDone;
	private ArrayList<ColumnDetails> schema;
	private boolean init = false;

	public BlockNestedJoinOperator(Operator left,Operator right){
		super(left,right);
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
		sb.append("{"+left.toString()+"}");
		sb.append("product ");
		sb.append("{"+right.toString()+"}");
		sb.append("]");
		return sb.toString();
	}

	@Override
	public Tuple readTuple() {
		if(!init ){
			init = true;
	//		System.out.println("Join "+left.toString() + "and " + right.toString());
		}
		if(left == null){
			return null;
		}
		if(right == null){
			return null;
		}
		
		if(leftBuffer == null){
			leftBuffer = new ArrayList<Tuple>();
			OperatorUtil.loadBuffer(left,leftBuffer);
			leftIterator = leftBuffer.iterator();
		}
		
		if(rightBuffer == null){
			rightBuffer = new ArrayList<Tuple>();
			OperatorUtil.loadBuffer(right, rightBuffer);
			rightIterator = rightBuffer.iterator();
		}

  		if(leftTuple == null){
			if(leftIterator.hasNext()){
				leftTuple = leftIterator.next();
			}else{
		//		System.out.println("End of" + left.toString() + "block " + right.toString());
				return null;
			}
		}
  		
  		// loadLeftBuffer
  		// loadRightBuffer
  		// loadLeftFirst
  		// Iterator over Right
  		//When Right ends iterate left and setRight to start
  		// When left and right ends load right buffer and reset left
  		// When rightInput is over loadleftBuffer and reset rightBuffer
  		
  		
		Tuple rightTuple ;
		if(rightIterator.hasNext()){
			rightTuple = rightIterator.next(); 
		}else{
			if(leftIterator.hasNext()){
				leftTuple = leftIterator.next();
				rightIterator = rightBuffer.iterator();
				rightTuple = rightIterator.next(); 
			}else{
				rightBuffer.clear();
				leftIterator = leftBuffer.iterator();
				if(leftIterator.hasNext()){
					leftTuple = leftIterator.next();
				}else{
			//		System.out.println("End of" + left.toString() + "block " + right.toString());
					return null;
				}
				OperatorUtil.loadBuffer(right, rightBuffer);
				rightIterator = rightBuffer.iterator();
				if(rightIterator.hasNext()){
					rightTuple = rightIterator.next();
				}else{
					leftBuffer.clear();
					OperatorUtil.loadBuffer(left,leftBuffer);
					leftIterator = leftBuffer.iterator();
					if(leftIterator.hasNext()){
						leftTuple = leftIterator.next();
					}else{
				//		System.out.println("End of" + left.toString() + "block " + right.toString());
						return null;
					}

					right.reset();
					rightBuffer.clear();
					OperatorUtil.loadBuffer(right, rightBuffer);
					rightIterator = rightBuffer.iterator();
					if(rightIterator.hasNext()){
						rightTuple = rightIterator.next();
					}else{
					//	System.out.println("End of" + left.toString() + "block " + right.toString());
						return null;
					}
				}
			}
		}
		Tuple tuple = new Tuple();
		tuple.append(leftTuple);
		tuple.append(rightTuple);
		return tuple;
	}

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

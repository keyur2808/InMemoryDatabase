package edu.buffalo.cse562.operator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Column;
import edu.buffalo.cse562.configuration.ColumnDetails;
import edu.buffalo.cse562.configuration.DataConfiguration;
import edu.buffalo.cse562.data.Tuple;
import edu.buffalo.cse562.queryoptimizer.OperatorVisitor;
import edu.buffalo.cse562.visitor.ColumnIndexParser;

public class EquiJoinOperator extends JoinOperator {

	private Column leftColumn = null;
	private Column rightColumn = null;
	private ArrayList<ColumnDetails> leftSchema = null;
	private ArrayList<ColumnDetails> rightSchema = null;
	private Map<String, Integer> leftSchemaIndex = null;
	private Map<String, Integer> rightSchemaIndex = null;
	private Integer leftIndex, rightIndex;
	private Map<String, ArrayList<Tuple>> hashLoad = new HashMap<String, ArrayList<Tuple>>(1000);
	private ArrayList<ColumnDetails> schema = null;
	private boolean isDone = false;
	private boolean isInit = false;
	private Tuple leftTuple = null;
	private Tuple rightTuple = null;
	private ArrayList<Tuple> leftList = null;
	private Iterator<Tuple> leftIterator = null;
	private EqualsTo condition = null;
	private boolean isPartitioned = false;

	public EquiJoinOperator(Operator leftOperator, Operator rightOperator,
			Column leftColumn, Column rightColumn) {
		super(leftOperator, rightOperator);
		this.leftColumn = leftColumn;
		this.rightColumn = rightColumn;
	}

	public EquiJoinOperator(Operator leftOperator, Operator rightOperator,
			EqualsTo exp) {
		super(leftOperator, rightOperator);
		this.condition = exp;
		this.isPartitioned = false;

		// get column from exp
		// check which column is in rightSchema
		// assign leftColumn and rightColumn
	}

	public EquiJoinOperator(Operator leftOperator, Operator rightOperator,
			EqualsTo exp,boolean isPartitioned) {
		super(leftOperator, rightOperator);
		this.condition = exp;
		this.isPartitioned = isPartitioned;

		// get column from exp
		// check which column is in rightSchema
		// assign leftColumn and rightColumn
	}

	
	public Operator getLeftOperator() {
		return this.left;
	}

	public Operator getRightOperator() {
		return this.right;
	}

	@Override
	public Tuple readTuple() {
		// TODO Auto-generated method stub
		if (!isInit) {
			// System.out.println("Joining"+left.toString()+" and "+right.toString());
			initialize();
			isInit = true;
		}

		if (rightTuple == null) {
			if (!loadTuples()) {
				// System.out.println("End of Joining"+left.toString()+" and "+right.toString());
				isDone = true;
				return null;
			}
		}

		if (!leftIterator.hasNext()) {
			if (!loadTuples()) {
				// System.out.println("End of Joining"+left.toString()+" and "+right.toString());
				isDone = true;
				return null;
			}
		}

		leftTuple = leftIterator.next();

		Tuple tup = new Tuple();
		tup.append(leftTuple);
		tup.append(rightTuple);
		return tup;
	}

	private boolean loadTuples() {
		rightTuple = right.readTuple();
		if (rightTuple != null) {
			do {
				String keyCheck = rightTuple.getValue(rightIndex).toString();
				if (hashLoad.containsKey(keyCheck)) {
					leftList = hashLoad.get(keyCheck);
					if (leftList != null && !leftList.isEmpty()) {
						leftIterator = leftList.iterator();
						break;
					} else {
						rightTuple = right.readTuple();
					}
				} else {
					rightTuple = right.readTuple();
				}
			} while (rightTuple != null);

			if (!leftIterator.hasNext()) {
				return loadTuples();
			}
			return true;
		}

		if (rightTuple == null) {
			if (isPartitioned) {
				do {
					if (((PartitionOperator) right).loadNextPartition()) {
						if (((PartitionOperator) left).loadNextPartition()) {
							hashLoad.clear();
							int dataCount = ((PartitionOperator) left).getDataCount();
							hashLoad = new HashMap<String, ArrayList<Tuple>>(dataCount % 10);
							initialize();
							loadTuples();
						} else {
							return false;
						}
					} else {
						return false;
					}
				} while (rightTuple == null);
				if (rightTuple != null) {
					return true;
				}
			}
		}
		return false;
	}

	private void initialize() {
		// Need to chk swap
		// Partition left
		// Partition right
		// Reassign left and right to partition op
		while (!left.isDone()) {
			Tuple tup = left.readTuple();
			if (tup != null) {
				String key = tup.getValue(leftIndex).toString();
				if (!hashLoad.containsKey(key)) {
					ArrayList<Tuple> list = new ArrayList<Tuple>();
					list.add(tup);
					hashLoad.put(key, list);
				} else {
					ArrayList<Tuple> list = (ArrayList<Tuple>) hashLoad
							.get(key);
					list.add(tup);
					hashLoad.put(key, list);
				}
			} else {
				break;
			}
		}
	}

	@Override
	public void reset() {
		left.reset();
		right.reset();

		leftSchema = left.getSchema();
		rightSchema = right.getSchema();
		ColumnIndexParser sip = new ColumnIndexParser(leftSchema);
		// System.out.println("SelectList::"+selectList);
		for (ColumnDetails colDetails : leftSchema) {
			Column col = colDetails.getColumn();
			col.accept(sip);
		}

		leftSchemaIndex = sip.getResult();

		sip = new ColumnIndexParser(rightSchema);
		// System.out.println("SelectList::"+selectList);
		for (ColumnDetails colDetails : rightSchema) {
			Column col = colDetails.getColumn();
			col.accept(sip);
		}

		rightSchemaIndex = sip.getResult();

		Column leftCol = (Column) condition.getLeftExpression();
		Column rightCol = (Column) condition.getRightExpression();

		if (rightSchemaIndex.containsKey(leftCol.toString())) {
			leftColumn = rightCol;
			rightColumn = leftCol;
		} else {
			leftColumn = leftCol;
			rightColumn = rightCol;
		}

		leftIndex = leftSchemaIndex.get(leftColumn.getWholeColumnName());
		rightIndex = rightSchemaIndex.get(rightColumn.getWholeColumnName());
		schema = new ArrayList<ColumnDetails>();
		schema.addAll(left.getSchema());
		schema.addAll(right.getSchema());

	if (isPartitioned) {
			Operator leftPartitionOperator = new PartitionOperator(left,
					leftColumn, leftIndex);
			Operator rightPartiotionOperator = new PartitionOperator(right,
					rightColumn, rightIndex);
			left = leftPartitionOperator;
			right = rightPartiotionOperator;
			left.reset();
			right.reset();
		}
		// System.out.println("Join::"+schema.size()+"||"+schema);
		// Check left Operators table name and right operators table name
	}

	/*
	 * public void checkSchema() { ColumnIndexParser sip = new
	 * ColumnIndexParser(schema); //
	 * System.out.println("SelectList::"+selectList); for (ColumnDetails
	 * colDetails : schema) { Column col = colDetails.getColumn();
	 * col.accept(sip); }
	 * 
	 * /* for(Column col : groupByCols){ col.accept(sip); }
	 * 
	 * schemaIndex = sip.getResult(); }
	 */

	@Override
	public boolean isDone() {
		// TODO Auto-generated method stub
		return isDone;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

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

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		sb.append("{" + left.toString() + "}");
		sb.append("EQUI JOIN ");
		if(isPartitioned){
			sb.append(" partitioned ");
		}
		sb.append("{" + right.toString() + "}");
		sb.append("]");
		return sb.toString();
	}

	@Override
	public void accept(OperatorVisitor ov) {
		ov.visit(this);
	}
}

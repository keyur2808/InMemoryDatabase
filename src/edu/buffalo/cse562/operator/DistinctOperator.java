package edu.buffalo.cse562.operator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sf.jsqlparser.statement.select.SelectItem;
import edu.buffalo.cse562.configuration.ColumnDetails;
import edu.buffalo.cse562.data.Tuple;
import edu.buffalo.cse562.queryoptimizer.OperatorVisitor;
import edu.buffalo.cse562.visitor.ColumnIndexParser;

public class DistinctOperator implements Operator {
	private Operator operator = null;
	private ArrayList<ColumnDetails> schema = null;
	private boolean isDone;
	private LinkedHashMap<String, String> map;
	private List<SelectItem> list;
	@SuppressWarnings("unused")
	private Map<String, Integer> schemaIndex = null;
	private Map<String, Integer> columnindex = null;

	public DistinctOperator(List<SelectItem> columnList, Operator operator) {
		this.operator = operator;
		this.list = columnList;
		map = new LinkedHashMap<String, String>();
		columnindex = new HashMap<String, Integer>();
	}

	public void loadSchema() {
		schema = operator.getSchema();
		for (int i = 0; i < schema.size(); i++) {
			columnindex.put(schema.get(i).getColumn().getColumnName(), i);
		}

	}

/*	private boolean getDistinct(Tuple tuple) {

		if (!map.containsKey(tuple.toString())) {
			map.put(tuple.toString(), "*");
			return true;
		} else
			return false;
	} */

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Distinct ");
		sb.append("[");
		sb.append(operator.toString());
		sb.append("]");
		return sb.toString();
	}

	@Override
	public void reset() {
		operator.reset();
		loadSchema();
		checkSchema();

	}

	public void checkSchema() {
		ColumnIndexParser sip = new ColumnIndexParser(schema);
		for (SelectItem selectExpItem : list) {

			selectExpItem.accept(sip);
		}
		schemaIndex = sip.getResult();
	}

	@Override
	public boolean isDone() {
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

	public void loadData() {
		if (!operator.isDone()) {
		}
	}

	@Override
	public Tuple readTuple() {
		if (!operator.isDone()) {
			Tuple inputTuple = operator.readTuple();
			if (inputTuple == null) {
				return null;
			}

			if (getDistinctTuples(inputTuple))
				return inputTuple;
			else
				return readTuple();
		}
		return null;

	}

	private boolean getDistinctTuples(Tuple inputTuple) {
		int j = 0;
		for (int i = 0; i < list.size(); i++) {
			j = columnindex.get(list.get(i).toString());
			if (!map.containsKey(inputTuple.getValues().get(j).getVal()
					.toString())) {
				map.put(inputTuple.getValues().get(j).getVal().toString(), "*");

			} else
				return false;
		}
		return true;

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

	public Operator getOperator() {
		// TODO Auto-generated method stub
		return this.operator;
	}
}

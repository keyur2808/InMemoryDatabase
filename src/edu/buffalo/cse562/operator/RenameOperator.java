package edu.buffalo.cse562.operator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import edu.buffalo.cse562.configuration.ColumnDetails;
import edu.buffalo.cse562.configuration.SchemaConfiguration;
import edu.buffalo.cse562.data.Tuple;
import edu.buffalo.cse562.queryoptimizer.OperatorVisitor;

public class RenameOperator implements Operator {

	private Operator input = null;
	private String alias = null;
	private Table table = null;
	private ArrayList<ColumnDetails> schema;
	private boolean isDone = false;

	public RenameOperator(Operator input, String alias) {
		this.input = input;
		this.table = new Table(null, alias);
		this.alias = alias;
	}
	
	public Operator getOperator(){
		return this.input;
	}

	@Override
	public Tuple readTuple() {
		Tuple tuple = input.readTuple();
		if(tuple == null){
			isDone  = true;
		}
		return tuple;
	}

	@Override
	public void reset() {
		try {
			input.reset();
			ArrayList<ColumnDetails> inputSchema = input.getSchema();
			List<ColumnDefinition> colDef = new ArrayList<ColumnDefinition>();
			schema = new ArrayList<ColumnDetails>();
			for (int i = 0; i < inputSchema.size(); i++) {
				ColumnDetails renameDetails = (ColumnDetails) inputSchema.get(i)
						.clone();
				renameDetails.getColumn().setTable(this.table);
				colDef.add(renameDetails.getColumnDefinition());
				schema.add(renameDetails);
			}
			//System.out.println("Rename::"+schema);
			Table table = new Table();
			table.setName(alias);
			CreateTable ct = new CreateTable();
			ct.setTable(table);
			ct.setColumnDefinitions(colDef);
			SchemaConfiguration.getInstance().addSchema(ct);
			
		} catch (CloneNotSupportedException ce) {
			ce.printStackTrace();
		}
		// load Schema

	}

	@Override
	public boolean isDone() {
		return isDone;
	}

	public void clear() {

	}

	@Override
	public ArrayList<ColumnDetails> getSchema() {
		return schema;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		sb.append(input.toString());
		sb.append(" AS ");
		sb.append(alias);
		sb.append("]");
		return sb.toString();
	}

	@Override
	public Map<String, Integer> getSchemaIndex() {
		return null;
	}
	
	public String getAlias(){
		return this.alias;
	}

	@Override
	public void accept(OperatorVisitor ov) {
		ov.visit(this);
	}
	
}

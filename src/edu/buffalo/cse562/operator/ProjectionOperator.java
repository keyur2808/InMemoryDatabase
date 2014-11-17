package edu.buffalo.cse562.operator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.buffalo.cse562.configuration.ColumnDetails;
import edu.buffalo.cse562.data.Tuple;
import edu.buffalo.cse562.queryoptimizer.OperatorVisitor;
import edu.buffalo.cse562.visitor.ColumnIndexParser;
import edu.buffalo.cse562.visitor.SelectItemParser;
import net.sf.jsqlparser.statement.select.SelectItem;

public class ProjectionOperator implements Operator{

	private List<SelectItem> selectItemList;
	private Operator input;
	private boolean isDone;
	private SelectItemParser sip = null;
	private ArrayList<ColumnDetails> schema = null;
	private Map<String,Integer> schemaIndex = null;
	private ArrayList<ColumnDetails> projectedSchema = null;
	
	public ProjectionOperator(List<SelectItem> selectItemList,Operator input){
		this.selectItemList = selectItemList;
		this.input = input;
	}
	
	public Operator getOperator(){
		return this.input;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		sb.append("Project ");
		sb.append(selectItemList);
		sb.append(input.toString());
		sb.append("]");
		return sb.toString();
	}

	public ArrayList<ColumnDetails> getSchema(){
		return projectedSchema;
	}
	public void loadSchema(){
		schema = input.getSchema();
		// Check Schema with project Columns
	}
	
	public void checkSchema(){
		//System.out.println(schema);
		ColumnIndexParser sip = new ColumnIndexParser(schema);
		for(SelectItem selectExpItem : selectItemList){
			selectExpItem.accept(sip);
		}
		schemaIndex = sip.getResult();
		projectedSchema = sip.getProjectedSchema();
		//System.out.println("ProjSchema::"+projectedSchema);
	}
	@Override
	public Tuple readTuple() {
		if(!input.isDone()){
			Tuple inputTuple = input.readTuple();
			if(inputTuple == null){
	//			System.out.println("Projecting" + selectItemList + "completed");
				isDone = true;
				return null;
			}
			Tuple outputTuple = projectColumns(inputTuple);
			return outputTuple;
		}
//		System.out.println("Projecting" + selectItemList + "completed");
		isDone = true;
		return null;
	}

	@Override
	public void reset() {
		input.reset();
		loadSchema();
	//	System.out.println("Projection::"+schema.size()+"||"+schema);
		checkSchema();
	//	System.out.println("Projection::"+schema.size()+"||"+schema);
		loadSelectItemParser();
	//	System.out.println("Projection::"+schema.size()+"||"+schema);
	}
	
	private void loadSelectItemParser(){
		this.sip = new SelectItemParser(schemaIndex);
	}

	@Override
	public boolean isDone() {
		return isDone;
	}
	
	public void clear(){
		
	}
	
	private Tuple projectColumns(Tuple inputTuple){
		//System.out.println(inputTuple);
		sip.setInputTuple(inputTuple);
	//	System.out.println(selectItemList);
		for(SelectItem selectExpItem : selectItemList){
			selectExpItem.accept(sip);
		}
		return sip.getResult();
	}

	@Override
	public Map<String, Integer> getSchemaIndex() {
		return null;
	}

	@Override
	public void accept(OperatorVisitor ov) {
		ov.visit(this);
	}

	public List<SelectItem> getSelectItemList() {
		// TODO Auto-generated method stub
		return selectItemList;
	}
}

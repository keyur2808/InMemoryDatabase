package edu.buffalo.cse562.operator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.buffalo.cse562.configuration.ColumnDetails;
import edu.buffalo.cse562.configuration.DataConfiguration;
import edu.buffalo.cse562.data.DatumConverter;
import edu.buffalo.cse562.configuration.SchemaConfiguration;
import edu.buffalo.cse562.data.Tuple;
import edu.buffalo.cse562.queryoptimizer.OperatorVisitor;
import edu.buffalo.cse562.visitor.ColumnIndexParser;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;

public class FromOperator implements Operator{
	
	private Table table;
	private List<ColumnDefinition> columnDef = null;
	private ArrayList<ColumnDetails> schema = null;
	private Map<String,Integer> schemaIndex = null;
	private DatumConverter datumObj = null;
	private boolean isDone = false;
	private File f = null;
	private BufferedReader br = null;
	private String[] dataType = null;

	public FromOperator(Table table){
		this.table = table;
		String dataPath = DataConfiguration.getInstance().getDataPath();
		if(dataPath.endsWith(File.separator)){
			dataPath = dataPath + table.getName()+".dat";
		}else{
			dataPath = dataPath + File.separator + table.getName()+".dat";
		}
		this.f = new File(dataPath);
	//	System.out.println(dataPath);
	}
	
	public Table getTable(){
		return this.table;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		sb.append("From Table ");
		sb.append(table.getName());
		sb.append("]");
		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	public void loadSchema(){
		CreateTable tableInfo = SchemaConfiguration.getInstance().getSchema(table.getName());
		this.columnDef = tableInfo.getColumnDefinitions();
		schema = new ArrayList<ColumnDetails>();
		schemaIndex = new HashMap<String,Integer>();
		dataType = new String[columnDef.size()];
		for(int i=0;i<columnDef.size();i++){
			ColumnDefinition colDef = (ColumnDefinition)columnDef.get(i);
			ColDataType colData = new ColDataType();
			colData.setDataType(colDef.getColDataType().toString().toLowerCase());
			colDef.setColDataType(colData);
			Column col = new Column(table,colDef.getColumnName());
			schema.add(new ColumnDetails(col,colDef));
			dataType[i] = colDef.getColDataType().toString();
		}
		ColumnIndexParser sip = new ColumnIndexParser(schema);
		for(ColumnDetails colDetails : schema){
			Column col = colDetails.getColumn();
			col.accept(sip);
		} 
		schemaIndex = sip.getResult();
		datumObj= new DatumConverter();
	}
	
	public ArrayList<ColumnDetails> getSchema(){
		return schema;
	}
	@Override
	public Tuple readTuple() {
	//	while(this.isDone && buffer.isEmpty()){
		Tuple tup = null;
		try{
			String value = br.readLine();
			if(value == null){
				this.isDone = true;
				return null;
			}
			while(value.trim().equals("")){
				value = br.readLine();
				if(value == null){
					this.isDone = true;
					return null;
				}
			}
			String[] tokens = value.split("\\|");
			tup = new Tuple();				
			for(int i=0; i<tokens.length; i++){
                tup.addValue(datumObj.parseToken(dataType[i] ,tokens[i]));	                
			}
		}catch(IOException e){
			System.out.println("Unable to read a new line");
		}finally{
			
		}
	//	}
		return tup;
	}

	@Override
	public void reset() {
		this.isDone = false;
		FileReader fr = null;
		try{
			fr = new FileReader(f);
			br = new BufferedReader(fr);
			loadSchema();
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	@Override
	public boolean isDone() {
		return isDone;
		
	}
	
	public void clear(){
	//	buffer.clear();
	}

	@Override
	public Map<String, Integer> getSchemaIndex() {
		// TODO Auto-generated method stub
		return schemaIndex;
	}

	@Override
	public void accept(OperatorVisitor ov) {
		ov.visit(this);
	}
}

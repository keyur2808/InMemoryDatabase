package edu.buffalo.cse562.configuration;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.jsqlparser.statement.create.table.CreateTable;

public class SchemaConfiguration {
	private static SchemaConfiguration schema = null;
	private Map<String,CreateTable> schemaMap = null;
	private Set<String> memoryFit = null;
	
	public static SchemaConfiguration getInstance(){
		
		synchronized (SchemaConfiguration.class) {
			if(schema == null){
				schema = new SchemaConfiguration();
			}
		}
		return schema;
	}
	
	private SchemaConfiguration(){
		schemaMap = new HashMap<String,CreateTable>();
		memoryFit = new HashSet<String>();
	}
	
	public void addSchema(CreateTable ctStatement){
		String table = ctStatement.getTable().getName().toLowerCase().trim();
		schemaMap.put(table,ctStatement);
	//	String path = DataConfiguration.getInstance().getDataPath() + File.separator + table+".dat";
	/*	File f = new File(path);
		if(f.length() < 52428800){
			memoryFit.add(table);
		} */
	}
	
	public CreateTable getSchema(String tableName){
		if(schemaMap.containsKey(tableName.toLowerCase().trim())){
			return schemaMap.get(tableName.toLowerCase().trim());
		}
		return null;
	}

	public boolean getIsMemoryFit(String tableName) {
		// TODO Auto-generated method stub
		if(memoryFit.contains(tableName.toLowerCase().trim())){
			return true;
		}
		return false;
	}
	
}

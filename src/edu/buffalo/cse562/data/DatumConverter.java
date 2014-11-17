package edu.buffalo.cse562.data;

import java.util.ArrayList;

import edu.buffalo.cse562.configuration.ColumnDetails;

public class DatumConverter {
	
	public Datum parseToken(String datatype, String value){
		if(!"".equals(value)){
			if(datatype.equals("int"))
				return (new ForLong()).getDatum(value);
			else if(datatype.equals("double")|| datatype.equals("float")|| datatype.equals("decimal"))
				return (new ForDouble()).getDatum(value);
			else if(datatype.equals("string") || datatype.startsWith("varchar") || datatype.startsWith("char"))
				return (new ForString()).getDatum(value);
			else if(datatype.equals("date"))
				return (new ForDate()).getDatum(value);
		}
		return null;
	}
	
	public Tuple buildTuple(String str, ArrayList<ColumnDetails> schema){
		String[] tokens = str.split("\\|");
		Tuple tup = new Tuple();				
		for(int i=0; i<tokens.length; i++){
            tup.addValue(parseToken(schema.get(i).getColumnDefinition().getColDataType().toString() ,tokens[i]));	                
		}
		return tup;
	}
}

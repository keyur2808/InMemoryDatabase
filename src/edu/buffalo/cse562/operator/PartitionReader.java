package edu.buffalo.cse562.operator;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import edu.buffalo.cse562.configuration.ColumnDetails;
import edu.buffalo.cse562.data.DatumConverter;
import edu.buffalo.cse562.data.Tuple;

public class PartitionReader {

	private File file = null;
	private FileReader fr = null;
	private BufferedReader br = null;
	private String fileName = null;
	private static DatumConverter datumObj = new DatumConverter();
	private ArrayList<ColumnDetails> schema = null;

	
	public PartitionReader(String fileName, ArrayList<ColumnDetails> schema) {
		// TODO Auto-generated constructor stub
		this.file =new File(fileName);
		this.schema = schema;
		this.fileName = fileName;
		try {
			
			this.fr = new FileReader(file);
			this.br = new BufferedReader(fr);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
	}

	public Tuple readNext() {
		// TODO Auto-generated method stub
		Tuple tuple = null;
		try {
		//	tuple = (Tuple) ois.readObject();
			String data = br.readLine();
			if(data == null){
				return null;
			}
			
			String[] tokens = data.split("\\|");
			tuple = new Tuple();
			for(int i=0; i<tokens.length; i++){
		          tuple.addValue(datumObj.parseToken(schema.get(i).getColumnDefinition().getColDataType().toString() ,tokens[i]));	                
			}
			
			return tuple;
			// Convert data to tuple;
		} catch (IOException e) {
				System.out.println("End of " +this.fileName);
		//	e.printStackTrace();
		} catch(Exception e){
			//System.out.println(this.fileName);
		} 
		return tuple;
	}
	
	public void close() {
		// TODO Auto-generated method stub
		try {
			if(fr != null){
					fr.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
		}
		try{
			if(br != null){
				br.close();
			}		
		} catch (IOException e) {
			// TODO Auto-generated catch block
	//		e.printStackTrace();
		}
	}
}

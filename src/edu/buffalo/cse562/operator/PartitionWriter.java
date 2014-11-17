package edu.buffalo.cse562.operator;

import edu.buffalo.cse562.data.Tuple;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.Flushable;
import java.io.IOException;

public class PartitionWriter implements Flushable{

	BufferedWriter bw = null;
	FileWriter fw = null;
	File file = null;
	String fileName = null;
	public PartitionWriter(String fileName) {
		// TODO Auto-generated constructor stub
		try {
			this.fileName = fileName;
			this.file = new File(fileName);
			this.fw = new FileWriter(file);
			this.bw = new BufferedWriter(fw);
		} catch (FileNotFoundException e) {
		//	e.printStackTrace();
		} catch (IOException e) {
		//	e.printStackTrace();
		}	
	}

	public void write(Tuple tuple) {
		try {
			bw.write(tuple.toString());
			//oos.writeObject(tuple);
		} catch (IOException e) {
		//	e.printStackTrace();
		}
	}

	public void flush() {
		//this.bw.flush();
		try {
			this.bw.flush();
			this.close();
			this.reset();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void reset() {
		// TODO Auto-generated method stub
		try {
			this.file = new File(fileName);
			this.fw = new FileWriter(file,true);
			this.bw = new BufferedWriter(fw);

		} catch (FileNotFoundException e) {
		//	e.printStackTrace();
		} catch (IOException e) {
		//	e.printStackTrace();
		}	
	}

	public void close() {
		// TODO Auto-generated method stub
		try{
			
			if(fw != null){
				fw.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		}

		try{
			
			if(bw != null){
				bw.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		}
	}
}

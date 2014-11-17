package edu.buffalo.cse562.operator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.OrderByElement;
import edu.buffalo.cse562.configuration.ColumnDetails;
import edu.buffalo.cse562.configuration.DataConfiguration;
import edu.buffalo.cse562.data.DatumConverter;
import edu.buffalo.cse562.data.Tuple;
import edu.buffalo.cse562.data.TupleComparator;
import edu.buffalo.cse562.queryoptimizer.OperatorVisitor;
import edu.buffalo.cse562.visitor.ColumnIndexParser;

public class ExternalSortOperator implements Operator {
	
	private final int BUFFER_SIZE = 100000;
	private Expression exp;	
	private boolean isAsc;
	private String colName;
	private Operator input;	
	private boolean isDone = false;
	private ArrayList<ColumnDetails> schema;
	private Map<String,Integer> schemaIndex;
	private File src = null;
	private BufferedReader srcBuffer;
	private FileReader srcFile;
//	private ObjectInputStream srcObj;
	private File orderDir = null;
	private File folderDir = null;
	private String path = null;
	private DatumConverter dc = null;
	private ArrayList<File> inputFiles = null;
	private ArrayList<File> outputFiles = null;
	private ArrayList<OrderByElement> orderList = null;
	private TupleComparator tupleComparator = null;
	private static int folderCount = 0;
	
	public ExternalSortOperator(Expression exp, String colName, boolean order, Operator input){
		this.exp = exp; 
		this.input = input;
		this.isAsc = order;
		this.colName = colName;
	}

	public ExternalSortOperator(List<OrderByElement> list, Operator input) {
		this.orderList = (ArrayList<OrderByElement>) list;
		this.input = input;
		// TODO Auto-generated constructor stub
	}

	public Operator getOperator(){
		return this.input;
	}
	
	@Override
	public Tuple readTuple() {		
		if(src.exists()){
			while(!isDone()){
				try{
					String s = srcBuffer.readLine();
					if(s==null){
						isDone = true;
						try{
						//	srcObj.close();
							srcBuffer.close();
							srcFile.close();
							src.delete();						
						}catch(Exception ei){
							ei.printStackTrace();
						}
						break;
					}
					Tuple tup = dc.buildTuple(s, schema);
					if(tup!=null){
						return tup;
					}
				}catch(EOFException e){
					isDone = true;
					try{
					//	srcObj.close();
						srcBuffer.close();
						srcFile.close();
						src.delete();						
					}catch(Exception ei){
						ei.printStackTrace();
					}
					return null;
				}catch(FileNotFoundException e){
					e.printStackTrace();
				}catch(IOException e){
					e.printStackTrace();
				}
			}
		}
		src.delete();
		return null;
	}

	@Override
	public void reset() {
		input.reset();
		dc = new DatumConverter();
		loadSchema();
		orderDir = new File(DataConfiguration.getInstance().getSwapPath()+File.separator+"order");
		if(!orderDir.exists()){
			orderDir.mkdir();
		}
		folderDir= new File(DataConfiguration.getInstance().getSwapPath()+File.separator+"order"+File.separator+(folderCount++));
		if(!folderDir.exists()){
			folderDir.mkdir();
		}
		path = folderDir.getAbsolutePath();
		initialize();		
	}		

	@Override
	public boolean isDone() {
		return isDone;
	}

	@Override
	public void clear() {

	}

	@Override
	public ArrayList<ColumnDetails> getSchema() {
		return schema;
	}

	private void loadSchema() {	
		schema=input.getSchema();
		ColumnIndexParser sip = new ColumnIndexParser(schema);
//		exp.accept(sip);
//		schemaIndex = sip.getResult();
	}
	
	private void initialize(){
		inputFiles = new ArrayList<File>();
		split();
		outputFiles = new ArrayList<File>();
//		if(isAsc){
			mergeAsc();
//		}else{
//			mergeDesc();
//		}
		try{
			srcFile = new FileReader(src);
			srcBuffer = new BufferedReader(srcFile);
			//srcObj = new ObjectInputStream(srcBuffer);
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	private void split(){
		ArrayList<Tuple> list;
		tupleComparator = new TupleComparator();

		List<Integer> colId = new ArrayList<Integer>();
		List<Boolean> colOrder = new ArrayList<Boolean>();
		for (OrderByElement ele : orderList) {
			String colName = ele.getExpression().toString();
			for (int i = 0; i < schema.size(); i++) {
				Column col = schema.get(i).getColumn();
				if (col != null) {
					String fullname = col.getWholeColumnName();
					String colname = schema.get(i).getColumn().getColumnName();
					if (fullname.equals(colName) || colname.equals(colName)) {
						// tupleComparator.setOnColumn(i);
						colId.add(i);
						colOrder.add(ele.isAsc());
					}
				}
			}
		}
		tupleComparator.setColumnList(colId);
		tupleComparator.setOrder(colOrder);

		int count=0;
//		String path = DataConfiguration.getInstance().getSwapPath();
		while(true){			
			list = new ArrayList<Tuple>();
			while(list.size()<=BUFFER_SIZE || input.isDone()){
				Tuple t = input.readTuple();
				if(t!=null){
						list.add(t);
				}	
				else
					break;
			}
			Collections.sort(list,tupleComparator);
			File f = new File(path+File.separator+count+".txt");
			try{
				
				/*FileOutputStream fos = new FileOutputStream(f);
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				ObjectOutputStream oos = new ObjectOutputStream(bos);*/
				FileWriter fw = new FileWriter(f);
				BufferedWriter bw = new BufferedWriter(fw);

				for(int i=1; i<=list.size(); i++){
					bw.write(list.get(i-1).toString());
					if (i % 10000 == 0) {
						bw.flush();
						fw.flush();
						bw.close();
						fw.close();
						fw = new FileWriter(f,true);
						bw = new BufferedWriter(fw);
					}
				}
				bw.flush();
				fw.flush();
				bw.close();
				fw.close();

			//	fos.close();
				inputFiles.add(f);
			}
			catch(IOException e){
				e.printStackTrace();
			}
			if(input.isDone()){
				break;
			}
			count++;
		}
	}	
	
private void mergeAsc(){
		
		int outCount = inputFiles.size();
		while(inputFiles.size()>1){
			for (int i=0; i<inputFiles.size(); i++){
				File one = inputFiles.get(i);
				i++;
				if(i>=inputFiles.size()){
					outputFiles.add(one);
					break;
				}
				File two = inputFiles.get(i);			
				File out = new File(path+File.separator+outCount+".txt");
				outCount++;
				try{
					
					/*FileInputStream fis1 = new FileInputStream(one);
					FileInputStream fis2 = new FileInputStream(two);
					FileOutputStream fos = new FileOutputStream(out);
					BufferedInputStream bis1 = new BufferedInputStream(fis1);
					BufferedInputStream bis2 = new BufferedInputStream(fis2);
					BufferedOutputStream bos = new BufferedOutputStream(fos);
					ObjectInputStream ois1 = new ObjectInputStream(bis1);
					ObjectInputStream ois2 = new ObjectInputStream(bis2);
					ObjectOutputStream oos = new ObjectOutputStream(bos);*/
					FileReader fr1 = new FileReader(one);
					FileReader fr2 = new FileReader(two);
					FileWriter fw = new FileWriter(out);
					BufferedReader br1 = new BufferedReader(fr1);
					BufferedReader br2 = new BufferedReader(fr2);
					BufferedWriter bw = new BufferedWriter(fw);
					
					
					int stat=0;
					Tuple t1=null, t2=null;
					boolean flag=true;					
					while(flag){						
						if(stat==0 || stat==1){
							try{
								String s = br1.readLine();
								if (s!=null){
									t1=dc.buildTuple(s, schema);
								}else{
									t1=null;
								}
							}catch(EOFException e){
								t1=null;
							}
						}
						if(stat==0 || stat==2){
							try{
								String s = br2.readLine();
								if(s!=null){
									t2=dc.buildTuple(s, schema);
								}else{
									t2=null;
								}
							}catch(EOFException e){
								t2=null;
							}
						}
						
						
						if(t1==null & t2==null){
							flag = false;
							continue;
						}
						else if(t1==null){
							bw.write(t2.toString());
							stat=2;
							continue;
						}
						else if(t2==null){
							bw.write(t1.toString());
							stat=1;
							continue;
						}
						int comp = tupleComparator.compare(t1, t2);
						if(comp <0){
							bw.write(t1.toString());
							stat=1;							
						}
						if(comp >0){
							bw.write(t2.toString());
							stat=2;							
						}
						if (i % 10000 == 0) {
							bw.flush();
							fw.flush();
							bw.close();
							fw.close();
							fw = new FileWriter(out,true);
							bw = new BufferedWriter(fw);
						}
					}					
					bw.flush();
					fw.flush();
					bw.close();
					fw.close();
					br1.close();
					br2.close();
					fr1.close();
					fr2.close();
					/*fos.flush();
					ois1.close();
					ois2.close();
					oos.close();
					bis1.close();
					bis2.close();
					bos.close();
					fis1.close();
					fis2.close();
					fos.close();*/
					one.delete();
					two.delete();
					outputFiles.add(out);					
				}catch(FileNotFoundException e){
					e.printStackTrace();
				}catch(IOException e){
					e.printStackTrace();
				}
			}
			inputFiles = outputFiles;
			outputFiles = new ArrayList<File>();
		}
		outCount--;
		src = new File(path+File.separator+outCount+".txt");
	}
	
	private void mergeDesc(){
	//	String path = DataConfiguration.getInstance().getSwapPath();//"E:/UB/Subjects/DB/Project/Checkpoint2/SwapDir/";
		//File dir = new File(path);
		//int inCount = 0;
		int outCount = inputFiles.size();
		while(inputFiles.size()>1){				
			for(int i=0; i<inputFiles.size(); i++){
				File one = inputFiles.get(i);
				i++;
				if(i>=inputFiles.size()){
					outputFiles.add(one);
					break;
				}
				File two = inputFiles.get(i);				
				File out = new File(path+File.separator+outCount+".txt");
				outCount++;
				try{
					
					/*FileInputStream fis1 = new FileInputStream(one);
					FileInputStream fis2 = new FileInputStream(two);
					FileOutputStream fos = new FileOutputStream(out);
					BufferedInputStream bis1 = new BufferedInputStream(fis1);
					BufferedInputStream bis2 = new BufferedInputStream(fis2);
					BufferedOutputStream bos = new BufferedOutputStream(fos);
					ObjectInputStream ois1 = new ObjectInputStream(bis1);
					ObjectInputStream ois2 = new ObjectInputStream(bis2);
					ObjectOutputStream oos = new ObjectOutputStream(bos);*/
					
					FileReader fr1 = new FileReader(one);
					FileReader fr2 = new FileReader(two);
					BufferedReader br1 = new BufferedReader(fr1);
					BufferedReader br2 = new BufferedReader(fr2);
					FileWriter fw = new FileWriter(out);
					BufferedWriter bw = new BufferedWriter(fw);
					
					
					int stat=0;
					Tuple t1=null, t2=null;
					boolean flag=true;					
					while(flag){
						
						if(stat==0 || stat==1){
							try{
								String s = br1.readLine();
								t1=dc.buildTuple(s, schema);
							}catch(EOFException e){
								t1=null;
							}
						}
						if(stat==0 || stat==2){
							try{
								String s = br2.readLine();
								t2=dc.buildTuple(s, schema);
							}catch(EOFException e){
								t2=null;
							}
						}
						if(t1==null & t2==null){
							flag=false;
							continue;
						}
						else if(t1==null){
							bw.write(t2.toString());
							stat=2;
							continue;
						}
						else if(t2==null){
							bw.write(t1.toString());
							stat=1;
							continue;
						}
						switch(tupleComparator.compare(t1, t2)){
							case 0:
							case 1:
								bw.write(t1.toString());							
								stat=1;
								break;							
							case -1:
								bw.write(t2.toString());
								stat=2;
								break;
							default:
								flag=false;
						}
					}
					bw.flush();
					fw.flush();
					bw.close();
					fw.close();
					br1.close();
					br2.close();
					fr1.close();
					fr2.close();
					/*bos.flush();
					ois1.close();
					ois2.close();
					oos.close();
					bis1.close();
					bis2.close();
					bos.close();
					fis1.close();
					fis2.close();
					fos.close();*/
					one.delete();
					two.delete();
					outputFiles.add(out);
				}catch(FileNotFoundException e){
					e.printStackTrace();
				}catch(IOException e){
					e.printStackTrace();
				}
			}
			inputFiles = outputFiles;
			outputFiles = new ArrayList<File>();
		}
		outCount--;
		src = new File(path+outCount+".txt");
	}

	@Override
	public Map<String, Integer> getSchemaIndex() {
		return null;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		sb.append(input.toString());
		sb.append(" order by");
		sb.append(this.colName);
		sb.append("]");
		return sb.toString();
	}

	@Override
	public void accept(OperatorVisitor ov) {
		ov.visit(this);
	}

	public Expression getExpression() {
		// TODO Auto-generated method stub
		return this.exp;
	}
	
	public String getColumnName(){
		return this.colName;
	}
	
	public boolean getIsAsc(){
		return this.isAsc;
	}

	public List<OrderByElement> getSortElementList() {
		return orderList;
	}
}

package edu.buffalo.cse562.visitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.buffalo.cse562.configuration.ColumnDetails;
import edu.buffalo.cse562.configuration.SchemaConfiguration;
import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.InverseExpression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;
import net.sf.jsqlparser.statement.select.SubSelect;

public class ColumnIndexParser implements SelectItemVisitor, ExpressionVisitor {

	//private List<SelectItem> columnList = new ArrayList<SelectItem>();
	private List<SelectItem> additionalColumnList = new ArrayList<SelectItem>();
	private List<Expression> hashAggregateColumnList = new ArrayList<Expression>();
	private ArrayList<ColumnDetails> schema = null;
	private Map<String,Integer> schemaIndex= null;
	private boolean isFunction = false;
	private boolean isExpression = false;
	private ColDataType colDataType = null;
	private ArrayList<ColumnDetails> projectedSchema = null;
	private Integer indexLoc = -1;


	public ColumnIndexParser(/*List<SelectItem> columnList,*/ArrayList<ColumnDetails> schema) {
		//this.columnList = columnList;
		this.schema = schema;
		projectedSchema = new ArrayList<ColumnDetails>();
		schemaIndex = new HashMap<String,Integer>();
	}
	
	

	public Map<String,Integer> getResult() {
		return schemaIndex;
	}
	
	public List<SelectItem> getNewList(){
		return additionalColumnList;
	}
	
	public List<Expression> getHashAggregateColumns(){
		return hashAggregateColumnList;
	}
	
	public ArrayList<ColumnDetails> getProjectedSchema(){
		return projectedSchema;
	}

	@Override
	public void visit(AllColumns allColumns) {
		// TODO Auto-generated method stub
		indexLoc = -1;
		int i=0;
		if(schemaIndex == null){
			schemaIndex = new HashMap<String,Integer>();
		}
		if(projectedSchema == null){
			projectedSchema = new ArrayList<ColumnDetails>();
		}
		for(ColumnDetails colDetail : schema){
			schemaIndex.put(colDetail.getColumn().getWholeColumnName(),i++);
			projectedSchema.add(colDetail);
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void visit(AllTableColumns tableColumns) {
		// TODO Auto-generated method stub
		indexLoc = -1;
		if(projectedSchema == null){
			projectedSchema = new ArrayList<ColumnDetails>();
		}
		Table table = tableColumns.getTable();
		CreateTable tableInfo = SchemaConfiguration.getInstance().getSchema(
				table.getName());
		List columnDef = tableInfo.getColumnDefinitions();
		for (int i = 0; i < columnDef.size(); i++) {
			ColumnDefinition colDef = (ColumnDefinition) columnDef.get(i);
			Column col = new Column(table,colDef.getColumnName());
			for (int j = 0; j < schema.size(); j++) {
				if (schema.get(j).getColumn().getWholeColumnName().equalsIgnoreCase(
						col.getWholeColumnName())) {
			//		System.out.println(col.getWholeColumnName());
			//		System.out.println(schemaIndex);
					schemaIndex.put(col.getWholeColumnName(),j);
				}
			}
			ColumnDetails colDetail = new ColumnDetails(col,colDef);
			projectedSchema.add(colDetail);

		}
	}

	@Override
	public void visit(SelectExpressionItem selExpItem) {
		indexLoc = -1;
		isFunction = false;
		isExpression = false;
		Expression exp = selExpItem.getExpression();
		exp.accept(this);
				
		String name = selExpItem.getAlias();
		if(name == null){
			name = exp.toString();
		}
		//System.out.println(schema);
		if(isFunction){
			if(schemaIndex == null){
				schemaIndex = new HashMap<String, Integer>();
			}
			//System.out.println(schemaIndex);
			//System.out.println("Schema::"+schema);
			if(!schemaIndex.containsKey(name)){
				//System.out.println("Adding");
				List<Integer> matchIndex = new ArrayList<Integer>();
				for(int i=0;i<schema.size();i++){
					Column schemaColumn = schema.get(i).getColumn();
					String schColWholeName = null;
					if(schemaColumn.getTable() != null){
						schColWholeName = schema.get(i).getColumn().getWholeColumnName();
					}
					String schColName = schema.get(i).getColumn().getColumnName();
					if(name != null && schColWholeName != null && name.equalsIgnoreCase(schColWholeName)){
						schemaIndex.put(name, i);
						if(selExpItem.getAlias()!=null){
							schemaIndex.put(selExpItem.getAlias(), i);
						}
						indexLoc = i;
						break;
					}else if(name != null && name.equalsIgnoreCase(schColName)){
						matchIndex.add(i);
					}
				}
				//System.out.println("MatchIndex::"+matchIndex);
				if(matchIndex.size()== 1){
					schemaIndex.put(name, matchIndex.get(0));
					schemaIndex.put(exp.toString(), matchIndex.get(0));
					indexLoc = matchIndex.get(0);
				}else {
					// throw an error
				}
			}
		}

	//	System.out.println("Schema::"+schema);
	//	System.out.println(schemaIndex);
		if (!schemaIndex.containsKey(name)) {
			if (!schemaIndex.containsKey(exp.toString())) {
				ColumnDetails colDetail = new ColumnDetails();
				colDetail.setColumn(new Column(new Table(), name));
				ColumnDefinition columnDefinition = new ColumnDefinition();
				columnDefinition.setColumnName(name);
				columnDefinition.setColDataType(colDataType);
				colDetail.setColumnDefinition(columnDefinition);
				schema.add(colDetail);
		//		System.out.println(colDetail.toString());
		//		System.out.println(colDataType.toString());
				indexLoc = schema.size() - 1;
				schemaIndex.put(name, schema.size() - 1);
				schemaIndex.put(exp.toString(), schema.size() - 1);
			}else{
				int index = schemaIndex.get(exp.toString());
				schemaIndex.put(name, index);
				ColumnDetails refColumn = schema.get(index);
				ColumnDetails colDetail = new ColumnDetails();
				ColumnDefinition columnDefinition = new ColumnDefinition();
				columnDefinition.setColumnName(name);
				columnDefinition.setColDataType(refColumn.getColumnDefinition().getColDataType());
				colDetail.setColumn(new Column(new Table(), name));
				colDetail.setColumnDefinition(columnDefinition);
		//		System.out.println(colDetail.toString());
		//		System.out.println(colDataType.toString());
				schema.set(indexLoc, colDetail);				
			}
		}
	//	System.out.println("Schema::"+schema);
		if(indexLoc != -1){
			projectedSchema.add(schema.get(indexLoc));
		}
	//	System.out.println("Schema::"+schema);
		//System.out.println("Schema1::"+schema);
		// TODO Auto-generated method stub
	}

//	public List<SelectItem> getColumns() {
		// TODO Auto-generated method stub
	//	return columnList;
//	}

	@Override
	public void visit(NullValue arg0) {
		// TODO Auto-generated method stub

	}

	@SuppressWarnings("unchecked")
	@Override
	public void visit(Function function) {
		// TODO Auto-generated method stub
		// sum
		// avg
		// count
		// max
		// min
		
		String functionName = function.getName().toString();
		if(functionName.equalsIgnoreCase("sum")){
			ExpressionList params = function.getParameters();
			List<Expression> expList  = params.getExpressions();
			for(Expression exp : expList){
				//System.out.println(exp.toString());
				exp.accept(this);
				isFunction = true;
			}
		}else if(functionName.equalsIgnoreCase("avg")){
			ExpressionList params = function.getParameters();
			List<Expression> expList  = params.getExpressions();
			Function sumFunc = new Function();
			sumFunc.setName("sum");
			sumFunc.setParameters(params);
			SelectExpressionItem sumSelExp = new SelectExpressionItem();
			sumSelExp.setExpression(sumFunc);
			if(!schemaIndex.containsKey(sumFunc.toString())){
				sumSelExp.accept(this);
				this.additionalColumnList.add(sumSelExp);
			}
			Function countFunc = new Function();
			countFunc.setName("count");
			countFunc.setParameters(params);
			SelectExpressionItem countSelExp = new SelectExpressionItem();
			countSelExp.setExpression(countFunc);
			if(!schemaIndex.containsKey(countSelExp.toString())){
				countSelExp.accept(this);
				this.additionalColumnList.add(countSelExp);
			}
			for(Expression exp : expList){
				exp.accept(this);
			}
			isFunction = true;
		}else if(functionName.equalsIgnoreCase("count")){
			ExpressionList params = function.getParameters();
			
			if(params!=null){
			List<Expression> expList  = params.getExpressions();
			if(function.isDistinct()){
				hashAggregateColumnList.addAll(expList);
			}
			//System.out.println(expList);
			for(Expression exp : expList){
				exp.accept(this);
			}
			}
			isFunction = true;
			//schema[]
		}else if(functionName.equalsIgnoreCase("max")){
			ExpressionList params = function.getParameters();
			List<Expression> expList  = params.getExpressions();
			for(Expression exp : expList){
				exp.accept(this);
			}
			isFunction = true;
		}else if(functionName.equals("min")){
			ExpressionList params = function.getParameters();
			List<Expression> expList  = params.getExpressions();
			for(Expression exp : expList){
				exp.accept(this);
			}
			isFunction = true;
		}
	}

	@Override
	public void visit(InverseExpression arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(JdbcParameter arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(DoubleValue arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(LongValue arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(DateValue arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(TimeValue arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(TimestampValue arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Parenthesis arg0) {
		// TODO Auto-generated method stub
		arg0.getExpression().accept(this);
	}

	@Override
	public void visit(StringValue arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Addition arg0) {
		// TODO Auto-generated method stub
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);	    
		Expression rightExpression=arg0.getRightExpression();
		rightExpression.accept(this);
		isExpression = true;
	}

	@Override
	public void visit(Division arg0) {
		// TODO Auto-generated method stub
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);	    
		Expression rightExpression=arg0.getRightExpression();
		rightExpression.accept(this);
		isExpression = true;
	}

	@Override
	public void visit(Multiplication arg0) {
		// TODO Auto-generated method stub
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);	    
		Expression rightExpression=arg0.getRightExpression();
		rightExpression.accept(this);
		isExpression = true;
	}

	@Override
	public void visit(Subtraction arg0) {
		// TODO Auto-generated method stub
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);	    
		Expression rightExpression=arg0.getRightExpression();
		rightExpression.accept(this);
		isExpression = true;
	}

	@Override
	public void visit(AndExpression arg0) {
		// TODO Auto-generated method stub
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);	    
		Expression rightExpression=arg0.getRightExpression();
		rightExpression.accept(this);

	}

	@Override
	public void visit(OrExpression arg0) {
		// TODO Auto-generated method stub
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);	    
		Expression rightExpression=arg0.getRightExpression();
		rightExpression.accept(this);

	}

	@Override
	public void visit(Between arg0) {
		// TODO Auto-generated method stub
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);
	    Expression start = arg0.getBetweenExpressionStart();
	    start.accept(this);
	    Expression end = arg0.getBetweenExpressionEnd();
	    end.accept(this);
	}

	@Override
	public void visit(EqualsTo arg0) {
		// TODO Auto-generated method stub
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);	    
		Expression rightExpression=arg0.getRightExpression();
		rightExpression.accept(this);
	}

	@Override
	public void visit(GreaterThan arg0) {
		// TODO Auto-generated method stub
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);	    
		Expression rightExpression=arg0.getRightExpression();
		rightExpression.accept(this);

	}

	@Override
	public void visit(GreaterThanEquals arg0) {
		// TODO Auto-generated method stub
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);	    
		Expression rightExpression=arg0.getRightExpression();
		rightExpression.accept(this);

	}

	@Override
	public void visit(InExpression arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(IsNullExpression arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(LikeExpression arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(MinorThan arg0) {
		// TODO Auto-generated method stub
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);	    
		Expression rightExpression=arg0.getRightExpression();
		rightExpression.accept(this);

	}

	@Override
	public void visit(MinorThanEquals arg0) {
		// TODO Auto-generated method stub
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);	    
		Expression rightExpression=arg0.getRightExpression();
		rightExpression.accept(this);

	}

	@Override
	public void visit(NotEqualsTo arg0) {
		// TODO Auto-generated method stub
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);	    
		Expression rightExpression=arg0.getRightExpression();
		rightExpression.accept(this);
	}

	@Override
	public void visit(Column column) {
		// TODO Auto-generated method stub
		//System.out.println("Column::"+column.getWholeColumnName());
		String colWholeName = column.getWholeColumnName();
		String colName = column.getColumnName();
		List<Integer> matchIndex = new ArrayList<Integer>();
		if(schemaIndex == null){
			schemaIndex = new HashMap<String,Integer>();
		}
		boolean flag = true;
		for(int i=0;i<schema.size();i++){
			Column schemaColumn = schema.get(i).getColumn();
			String schColWholeName = null;
			if(schemaColumn.getTable() != null){
				schColWholeName = schema.get(i).getColumn().getWholeColumnName();
			}
			String schColName = schema.get(i).getColumn().getColumnName();
			if(colWholeName != null && schColWholeName != null && colWholeName.equalsIgnoreCase(schColWholeName)){				
				schemaIndex.put(column.getWholeColumnName(), i);
				colDataType = schema.get(i).getColumnDefinition().getColDataType();
				indexLoc = i;
				flag = false;
				break;
			}else if(colWholeName != null && colWholeName.equalsIgnoreCase(schColName)){
				colDataType = schema.get(i).getColumnDefinition().getColDataType();
				matchIndex.add(i);
			}else if(colName != null && colName.equalsIgnoreCase(schColName)){
				matchIndex.add(i);
			}
		}
		//System.out.println("MatchIndex::"+matchIndex);
		if(flag && matchIndex.size()== 1){
			schemaIndex.put(column.getColumnName(), matchIndex.get(0));
			indexLoc = matchIndex.get(0);
		}else {
			// throw an error
		}
		// accumulator = tuple.getValue(column.getColumnName());
	}

	@Override
	public void visit(SubSelect arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(CaseExpression arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(WhenClause arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(ExistsExpression arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(AllComparisonExpression arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(AnyComparisonExpression arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Concat arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Matches arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(BitwiseAnd arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(BitwiseOr arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(BitwiseXor arg0) {
		// TODO Auto-generated method stub

	}

}

package edu.buffalo.cse562.queryoptimizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import edu.buffalo.cse562.configuration.DataConfiguration;
import edu.buffalo.cse562.configuration.SchemaConfiguration;
import edu.buffalo.cse562.operator.BlockNestedJoinOperator;
import edu.buffalo.cse562.operator.DifferenceOperator;
import edu.buffalo.cse562.operator.DistinctOperator;
import edu.buffalo.cse562.operator.EquiJoinOperator;
import edu.buffalo.cse562.operator.ExternalSortOperator;
import edu.buffalo.cse562.operator.FromOperator;
import edu.buffalo.cse562.operator.GroupByOperator;
import edu.buffalo.cse562.operator.IntersectionOperator;
import edu.buffalo.cse562.operator.JoinOperator;
import edu.buffalo.cse562.operator.LimitOperator;
import edu.buffalo.cse562.operator.MergeJoin;
import edu.buffalo.cse562.operator.Operator;
import edu.buffalo.cse562.operator.OrderByOperator;
import edu.buffalo.cse562.operator.ProjectionOperator;
import edu.buffalo.cse562.operator.RenameOperator;
import edu.buffalo.cse562.operator.SelectionOperator;
import edu.buffalo.cse562.operator.UnionOperator;
 

public class EquivalenceGenerator implements OperatorVisitor{
	
	Set<String> tableList = new HashSet<String>();
	Map<Expression,Set<String>> tableExpMap = new HashMap<Expression,Set<String>>();
	Set<Expression> expressionList = new HashSet<Expression>();
	Stack<Operator> stack = new Stack<Operator>();
	HashMap<String,HashMap<String,SelectExpressionItem>> columnMap = new HashMap<String,HashMap<String,SelectExpressionItem>>();
	boolean memfit = false;
	
	public Operator getOperator(){
		return stack.pop();
	}
	
	public void visit(FromOperator op){
		Table table = op.getTable();
		stack.push(op);
		if(columnMap.containsKey(table.getName())){
			Operator colFilter = new ProjectionOperator(getFilteredColumnList(table.getName()),stack.pop());
			stack.push(colFilter);
		}
		tableList.add(table.getName());
		Iterator<Expression> expIt = expressionList.iterator();
		while(expIt.hasNext()){
			Expression exp = expIt.next();
			Set<String> tableinExp = tableExpMap.get(exp);
			if(tableinExp.size() == 1){
				Iterator<String> tableIt = tableinExp.iterator();
				while(tableIt.hasNext()){
					String tableName = tableIt.next();
					if(tableName != null && tableName.equalsIgnoreCase(table.getName())){
						stack.push(new SelectionOperator(exp,stack.pop()));
						expIt.remove();
					}
				}
			}
		}
	}
	
	public void visit(SelectionOperator op){
		Expression exp = op.getExpression();
		ExpressionParser expParser = new ExpressionParser();
		exp.accept(expParser);
		ArrayList<Column> columnList = expParser.getColumns();
		columnPopulator(columnList);
		Set<String> tables = expParser.getTables();
		tableExpMap.put(exp,tables);
		expressionList.add(exp);
		op.getOperator().accept(this);
		if(expressionList.contains(exp)){
			//
			Operator childOp = stack.pop();
			stack.push(new SelectionOperator(exp, childOp));
		}
		//
	}
	
	public void visit(JoinOperator op){
		op.getLeftOperator().accept(this);
		op.getRightOperator().accept(this);

		Iterator<Expression> expIt = expressionList.iterator();
		Operator left = stack.pop();
		Operator right = stack.pop();
		
		Operator join = new BlockNestedJoinOperator(left, right);
		stack.push(join);
		
		while(expIt.hasNext()){
			Expression exp = expIt.next();
			Set<String> tableinExp = tableExpMap.get(exp);
			if(tableinExp.size() != 1){
				Iterator<String> tableIt = tableinExp.iterator();
				boolean containsAllTables = true;
				while(tableIt.hasNext()){
					String tableName = tableIt.next();
					if(!tableList.contains(tableName)){
						containsAllTables = false;
						break;
			//			Operator newOp = new SelectionOperator(op,exp);
			//			expIt.remove();
						//
					}
				}
				
				if(containsAllTables){
				if(exp instanceof EqualsTo){
						JoinOperator modOp = (JoinOperator)stack.pop();
						if(DataConfiguration.getInstance().isSwap()){
							Operator equi = new EquiJoinOperator(modOp.getLeftOperator(), modOp.getRightOperator(),(EqualsTo)exp,true);
							stack.push(equi);
						}else{
							Operator equi = new EquiJoinOperator(modOp.getLeftOperator(), modOp.getRightOperator(),(EqualsTo)exp);
							stack.push(equi);
						} 
					}else{
						stack.push(new SelectionOperator(exp, stack.pop()));
						//Operator newOp = new SelectionOperator(op,exp)
					}
					expIt.remove();
					break;
				}
			}
		}
	}
	
	public void visit(ProjectionOperator op){
		ExpressionParser expParser = new ExpressionParser();
		for(SelectItem si: op.getSelectItemList()){
			si.accept(expParser);
		}
		columnPopulator(expParser.getColumns());
		op.getOperator().accept(this);		
		Operator childOp = stack.pop();
		stack.push(new ProjectionOperator(op.getSelectItemList(), childOp));
	}
	public void visit(OrderByOperator op){
		op.getOperator().accept(this);
		Operator childOp = stack.pop();
		stack.push(new OrderByOperator(op.getSortElementList(),childOp));		
	}
	public void visit(RenameOperator op){
		op.getOperator().accept(this);
		String alias = op.getAlias();

		tableList.add(alias);
		Iterator<Expression> expIt = expressionList.iterator();
		stack.push(new RenameOperator(stack.pop(), op.getAlias()));
		
		if(columnMap.containsKey(alias)){
			Operator colFilter = new ProjectionOperator(getFilteredColumnList(alias),stack.pop());
			stack.push(colFilter);
		}
		
		while(expIt.hasNext()){
			Expression exp = expIt.next();
			Set<String> tableinExp = tableExpMap.get(exp);
			if(tableinExp.size() == 1){
				Iterator<String> tableIt = tableinExp.iterator();
				while(tableIt.hasNext()){
					String tableName = tableIt.next();
					if(tableName.equalsIgnoreCase(alias)){
						stack.push(new SelectionOperator(exp,stack.pop()));
						expIt.remove();
					}
				}
			}
		}
	}
	public void visit(GroupByOperator op){
		columnPopulator(op.getGroupByColumns());		
		op.getOperator().accept(this);		
		Operator childOp = stack.pop();
		stack.push(new GroupByOperator(childOp, op.getSelectList(), op.getGroupByColumns()));
	}
	public void visit(LimitOperator op){
		op.getOperator().accept(this);
		Operator childOp = stack.pop();
		stack.push(new LimitOperator(childOp, op.getLimit(), op.getOffset(), op.getIsAll()));
	}
	@Override
	public void visit(DifferenceOperator op) {
		// TODO Auto-generated method stub
		op.getLeftOperator().accept(this);
		op.getRightOperator().accept(this);

	}
	@Override
	public void visit(DistinctOperator op) {
		// TODO Auto-generated method stub
		op.getOperator().accept(this);
	}
	@Override
	public void visit(EquiJoinOperator op) {
		// TODO Auto-generated method stub
		op.getLeftOperator().accept(this);
		op.getRightOperator().accept(this);

	}
	@Override
	public void visit(ExternalSortOperator op) {
		// TODO Auto-generated method stub
		List<OrderByElement> orderList = op.getSortElementList();
		ExpressionParser expParser = new ExpressionParser();
		for (OrderByElement order: orderList){
			Expression exp = order.getExpression();
			exp.accept(expParser);
		}
		columnPopulator(expParser.getColumns());
		op.getOperator().accept(this);
		Operator childOp = stack.pop();
		stack.push(new ExternalSortOperator(op.getSortElementList(),childOp));		
	}
	@Override
	public void visit(IntersectionOperator op) {
		// TODO Auto-generated method stub
		op.getLeftOperator().accept(this);
		op.getRightOperator().accept(this);

	}
	@Override
	public void visit(UnionOperator op) {
		// TODO Auto-generated method stub
		op.getLeftOperator().accept(this);
		op.getRightOperator().accept(this);

	}
	
	private void columnPopulator(List<Column> columnList){
		if (columnList != null) {
			for (Column col : columnList) {
				SelectExpressionItem sie = new SelectExpressionItem();
				sie.setExpression(col);
				String tableName = col.getTable().getName();
				/*
				 * if(tableName == null){ tableName = col.getTable().getName();
				 * }
				 */
				if (tableName == null) {
					continue;
				}
				HashMap<String, SelectExpressionItem> selectList = null;
				if (columnMap.containsKey(tableName)) {
					selectList = columnMap.get(tableName);
				} else {
					selectList = new HashMap<String, SelectExpressionItem>();
				}
				if (!selectList.containsKey(col.getColumnName())) {
					selectList.put(col.getColumnName(), sie);
					columnMap.put(tableName, selectList);
				}
			}
		}
	}
	
	private ArrayList<SelectItem> getFilteredColumnList(String tableName){
		if(columnMap.containsKey(tableName)){
			ArrayList<SelectItem> list = new ArrayList<SelectItem>();
			HashMap<String, SelectExpressionItem> columns = columnMap.get(tableName); 
			Iterator<String> itr = columns.keySet().iterator();
			while(itr.hasNext()){
				list.add(columns.get(itr.next()));
			}
			return list;
		}
		return null;
	}
}

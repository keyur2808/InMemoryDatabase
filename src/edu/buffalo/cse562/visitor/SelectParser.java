package edu.buffalo.cse562.visitor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.buffalo.cse562.configuration.DataConfiguration;
import edu.buffalo.cse562.operator.BlockNestedJoinOperator;
import edu.buffalo.cse562.operator.ExternalSortOperator;
import edu.buffalo.cse562.operator.GroupByOperator;
import edu.buffalo.cse562.operator.LimitOperator;
import edu.buffalo.cse562.operator.Operator;
import edu.buffalo.cse562.operator.OrderByOperator;
import edu.buffalo.cse562.operator.ProjectionOperator;
import edu.buffalo.cse562.operator.SelectionOperator;
import edu.buffalo.cse562.operator.UnionOperator;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.Union;

public class SelectParser implements SelectVisitor {

	private Operator operator = null;

	public Operator getOperator() {
		return this.operator;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void visit(PlainSelect plainSelect) {

		FromItemParser fip = new FromItemParser();
		plainSelect.getFromItem().accept(fip);

		List<Join> joinsList = new ArrayList<Join>();
		

		Expression whereExpression = plainSelect.getWhere();
		if(false){
//		if (joinsList != null && whereExpression != null) {
			JoinParser jp = new JoinParser(plainSelect.getFromItem(),
					plainSelect.getJoins(), plainSelect.getWhere());
			operator = jp.getOperator();
		} else {

			operator = fip.getOperator();
 
			
			if (plainSelect.getJoins() != null) {
				List<Join> bottomUpJoin = plainSelect.getJoins();
				for(Join join : bottomUpJoin){
					FromItemParser joinItem = new FromItemParser();
					join.getRightItem().accept(joinItem);
					Operator rightItem = joinItem.getOperator();
					operator = new BlockNestedJoinOperator(operator, rightItem);
					Expression exp = join.getOnExpression();
					if (exp != null) {
						operator = new SelectionOperator(exp, operator);
					}

				}
				// join.getUsingColumns();
			}
			
			/* Left Tree */
		/*	if (plainSelect.getJoins() != null) {
					joinsList.addAll(plainSelect.getJoins());
					Join join = joinsList.get(0);
					Operator leftOperator = formleftTree(joinsList);
					operator = new BlockNestedJoinOperator(leftOperator, operator);
					Expression exp = join.getOnExpression();
					if (exp != null) {
						operator = new SelectionOperator(exp, operator);
					}
					// join.getUsingColumns();
			} */ 

			if (whereExpression != null) {
				// ExpressionParser expParser = new ExpressionParser();
				// whereExpression.accept(expParser);
				JoinParser jp = new JoinParser(whereExpression);
				List<Expression> expList =  jp.getExpressionList();
				Iterator<Expression> expIt = expList.iterator();
				while(expIt.hasNext()){
					Expression exp = expIt.next();
					operator = new SelectionOperator(exp, operator);
				}
				// Operator expOperator = expParser.getOperator();
			}
		}
		
		// Check Distinct
		List<SelectItem> selectItemList = (List<SelectItem>) plainSelect
				.getSelectItems();		
		
		List<Column> groupByColumnList = plainSelect
				.getGroupByColumnReferences();

		if (groupByColumnList != null) {
			operator = new GroupByOperator(operator, selectItemList,
					groupByColumnList);
		} else {
			AggregateChecker aggCheck = new AggregateChecker();
			for (SelectItem selItem : selectItemList) {
				selItem.accept(aggCheck);
			}
			if (aggCheck.getResult()) {
				operator = new GroupByOperator(operator, selectItemList, null);
			}
		}

		Expression havingExpression = plainSelect.getHaving();
		if (havingExpression != null) {
			// ExpressionParser expParser = new ExpressionParser();
			// havingExpression.accept(expParser);
			operator = new SelectionOperator(havingExpression, operator);
			// Operator expOperator = expParser.getOperator();
		}

		// SelectItemParser itemParser = new SelectItemParser();
		// for(SelectItem si : selectItemList){
		// si.accept(itemParser);
		// }

		List<OrderByElement> list = (List<OrderByElement>) plainSelect
				.getOrderByElements();
		if (list != null) {
			if (DataConfiguration.getInstance().isSwap()) {
				operator = new ExternalSortOperator(list, operator);
			} else {
				operator = new OrderByOperator(list, operator);
			}
		}
	/*	if (list != null) {
			for (int i = list.size() - 1; i >= 0; i--) {
				Expression orderExpression = list.get(i).getExpression();
				OrderByParser orderParser = new OrderByParser();
				orderExpression.accept(orderParser);
				if(DataConfiguration.getInstance().isSwap()){
					operator = new ExternalSortOperator(orderExpression,orderParser.getColName(), list
							.get(i).isAsc(), operator);
				}else{
					operator = new OrderByOperator(orderParser.getColName(), list
						.get(i).isAsc(), operator);
				}
			}
		} */

/*		Distinct distinct = plainSelect.getDistinct();
		if (distinct != null) {
			List<SelectItem> columnList = (List<SelectItem>) distinct
					.getOnSelectItems();
			operator = new DistinctOperator(columnList, operator);
		}
*/
		Limit limit = plainSelect.getLimit();
		if(limit != null){
			operator = new LimitOperator(operator, limit.getRowCount(), limit.getOffset(), limit.isLimitAll());
		}
		
		operator = new ProjectionOperator(selectItemList, operator);
	}

	private Operator formleftTree(List<Join> joinsList) {
		// TODO Auto-generated method stub
		Join join = joinsList.remove(0);
		FromItemParser joinItem = new FromItemParser();
		join.getRightItem().accept(joinItem);
		Operator rightItem = joinItem.getOperator();
		if(!joinsList.isEmpty()){
			Operator leftTree = formleftTree(joinsList);
			Operator op = new BlockNestedJoinOperator(leftTree,rightItem);
			return op;
		}
		return rightItem;
	}

	@Override
	public void visit(Union union) {
		System.out.println("Hello");
		@SuppressWarnings("unchecked")
		List<PlainSelect> plainselects = (List<PlainSelect>) union
				.getPlainSelects();
		for (PlainSelect ps : plainselects) {
			SelectParser sp = new SelectParser();
			ps.accept(sp);
			Operator op = sp.getOperator();
			if (operator == null) {
				operator = op;
			} else {
				operator = new UnionOperator(operator, op, union.isAll());
			}
		}
	}
}
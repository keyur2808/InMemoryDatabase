package edu.buffalo.cse562.visitor;

import edu.buffalo.cse562.configuration.SchemaConfiguration;
import edu.buffalo.cse562.operator.Operator;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;

public class StatementParser implements StatementVisitor{

	private Operator operator ;
	
	public Operator getOperator(){
		return this.operator;
	}
	@Override
	public void visit(Select selectSt) {
//		System.out.println(selectSt.toString());
		SelectParser sp = new SelectParser();
		selectSt.getSelectBody().accept(sp);
		this.operator = sp.getOperator();
	}

	@Override
	public void visit(Delete arg0) {
		System.out.println("Unhandled Statement");

		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Update arg0) {
		System.out.println("Unhandled Statement");
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Insert arg0) {
		System.out.println("Unhandled Statement");
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Replace arg0) {
		System.out.println("Unhandled Statement");
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Drop arg0) {
		System.out.println("Unhandled Statement");
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Truncate arg0) {
		System.out.println("Unhandled Statement");
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(CreateTable ct) {
		//System.out.println("This is a create table statement");
		SchemaConfiguration.getInstance().addSchema((CreateTable)ct);		
	}

}

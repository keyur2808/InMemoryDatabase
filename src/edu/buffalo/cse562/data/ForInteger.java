package edu.buffalo.cse562.data;

import net.sf.jsqlparser.expression.ExpressionVisitor;

public class ForInteger implements Datum{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -344760472080754577L;
	private Long val;

	public Object getVal() {
		return val;
	}

	public void setVal(long val) {
		this.val = new Long(val);
	}
		
	public ForInteger getDatum(String value){
		setVal(Long.parseLong(value));
		return this;
	}

	@Override
	public boolean isEqualTo(Datum d) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean greaterThan(Datum d) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean lesserThan(Datum d) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Datum add(Datum d) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Datum mul(Datum d) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Datum sub(Datum d) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Datum div(Datum d) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int compareTo(Datum d) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int hashCode(){
		return this.val.hashCode();
	}
	
	@Override
	public boolean equals(Object d){
		return this.isEqualTo((Datum)d);
	}
	
	public void accept(DatumExpressionVisitor dev){
		dev.visit(this);
	}

	@Override
	public void accept(ExpressionVisitor arg0) {
		// TODO Auto-generated method stub
		if(arg0 instanceof DatumExpressionVisitor){
			((DatumExpressionVisitor)arg0).visit(this);
		}
	}
}

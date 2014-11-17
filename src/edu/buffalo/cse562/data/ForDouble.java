package edu.buffalo.cse562.data;

import net.sf.jsqlparser.expression.ExpressionVisitor;


public class ForDouble implements Datum,DatumExpression{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4671182820512019280L;
	private Double val;

	public ForDouble(){
		
	}
	
	public ForDouble(Double v){
		val = v;
	}

	@Override
	public Double getVal() {
		return val;
	}

	public void setVal(Double val) {
		this.val = new Double(val);
	}
	
	@Override
	public ForDouble getDatum(String value){
		setVal(Double.parseDouble(value));
		return this;
	}

	@Override
	public boolean isEqualTo(Datum d) {
		double a = getVal();
		double b = (Double) d.getVal();
		return a==b;
	}
	
	@Override
	public boolean greaterThan(Datum d) {
		double a = getVal();
		double b = (Double) d.getVal();
		return a>b;
	}

	@Override
	public boolean lesserThan(Datum d) {
		double b;
		if(d instanceof ForLong){
			b = ((Long)d.getVal()).doubleValue();
		}else{
			b = (Double)d.getVal();
		}
		double a = getVal();
		return a<b;
	}

	@Override
	public Datum add(Datum d) {
		double val2;
		if(d instanceof ForLong){
			val2 = ((Long)d.getVal()).doubleValue();
		}else{
			val2 = (Double)d.getVal();
		}
		double sum = val + val2;
		sum = Math.round(sum*100)/(double)100;
		return new ForDouble(sum);
	}

	@Override
	public Datum mul(Datum d) {
		return new ForDouble(val*((Double)d.getVal()));
	}

	@Override
	public Datum sub(Datum d) {
		double val2;
		if(d instanceof ForLong){
			val2 = ((Long)d.getVal()).doubleValue();
		}else{
			val2 = (Double)d.getVal();
		}
		double diff = val - val2;
		diff = Math.round(diff*100)/(double)100;
		return new ForDouble(diff);
	}

	@Override
	public Datum div(Datum d) {
		double val2;
		if(d instanceof ForLong){
			val2 = ((Long)d.getVal()).doubleValue();
		}else{
			val2 = (Double)d.getVal();
		}
		return new ForDouble(val/val2);
	}
	
	@Override
	public int compareTo(Datum d) {
		double b;
		if(d instanceof ForLong){
			b = ((Long)d.getVal()).doubleValue();
		}else{
			b = (Double)d.getVal();
		}
		double a = getVal();
		if(a < b){
			return -1;
		}
		else if(a == b){
			return 0;
		}
		else{
			return 1;
		}
	}
	
	public String toString(){
		return this.getVal().toString();
	}
	
	@Override
	public int hashCode(){
		return this.val.hashCode();
	}
	
	@Override
	public boolean equals(Object d){
		return this.isEqualTo((Datum)d);
	}

	@Override
	public void accept(DatumExpressionVisitor dev) {
		// TODO Auto-generated method stub
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
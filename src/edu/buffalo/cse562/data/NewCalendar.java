package edu.buffalo.cse562.data;

import java.util.GregorianCalendar;

public class NewCalendar extends GregorianCalendar {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public String toString(){			
		return String.format("%04d-%02d-%02d",get(YEAR),(get(MONTH)+1),get(DAY_OF_MONTH));
	}

}

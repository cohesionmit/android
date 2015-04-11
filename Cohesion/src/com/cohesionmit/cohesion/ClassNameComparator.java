package com.cohesionmit.cohesion;

import java.util.Comparator;

public class ClassNameComparator implements Comparator<String> {
	@Override
	public int compare(String lhs, String rhs) {
		// Compare department number
		int i = Integer.parseInt(lhs.substring(0, lhs.indexOf('.')), 36)
				- Integer.parseInt(rhs.substring(0, rhs.indexOf('.')), 36);
		if (i != 0) {
			return i;
		}
		
		// Compare class number
		return Integer.parseInt(lhs.substring(1+lhs.indexOf('.')), 36)
				- Integer.parseInt(rhs.substring(1+rhs.indexOf('.')), 36);
	}
}

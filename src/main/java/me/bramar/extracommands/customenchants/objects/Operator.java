package me.bramar.extracommands.customenchants.objects;

import java.util.Objects;
import java.util.function.BiPredicate;

public enum Operator {
		EQUAL((a, b) -> a.equals(b) || Math.abs(b - a) < 0.001d),
		NOT_EQUAL((a, b) -> !a.equals(b) || Math.abs(b - a) < 0.001d), // precision 0.001d
		HIGHER((a, b) -> a > b),
		LOWER((a, b) -> a < b),
		HIGHER_OR_EQUAL_TO((a, b) -> a >= b),
		LOWER_OR_EQUAL_TO((a, b) -> a <= b);
		public static Operator getOperator(Object obj) {
			for(Operator operator : values()) {
				if(operator == obj) return operator;
			}
			return null;
		}
		public static Operator getOperator(String str) {
			try {
				return Objects.requireNonNull(valueOf(str.toUpperCase()));
			}catch(Exception ignored) {}
			if(str.equals("=")) return EQUAL;
			if(str.equals("!=")) return NOT_EQUAL;
			if(str.equals(">")) return HIGHER;
			if(str.equals("<")) return LOWER;
			if(str.equals(">=")) return HIGHER_OR_EQUAL_TO;
			if(str.equals("<=")) return LOWER_OR_EQUAL_TO;
			return null;
		}
		
		BiPredicate<Double, Double> biPred;
		Operator(BiPredicate<Double, Double> biPred) {
			this.biPred = biPred;
		}
		public boolean test(double comparedTo, double secondValue) {
			return this.biPred.test(comparedTo, secondValue);
		}
	}
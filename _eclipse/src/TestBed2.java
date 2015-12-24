import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.maybites.mxj.expression.Expression;
import ch.maybites.mxj.expression.ExpressionVar;
import ch.maybites.mxj.expression.RunTimeEnvironment;
import ch.maybites.mxj.expression.Expression.ExpressionException;

public class TestBed2 {

	static public void main(String[] args){
		 
		List<String> segmts = new ArrayList<String>();
		Matcher m = Pattern.compile("([^{]\\S*|.+?[{}])\\s*").matcher("/queOne color {sin($color_f1)} {'passed[' + ($global1) + ']'} {$global1} test");
		while (m.find()){
			System.out.println(m.group(1));
			segmts.add(m.group(1).trim()); // Add .replace("\"", "") to remove surrounding quotes.
		}
		

		try{
			double eff = 10.0;
			System.out.print(Double.toString(eff));
			
			ExpressionVar result = null;
			
			RunTimeEnvironment rt = new RunTimeEnvironment();
			
			Expression expression;

			ExpressionVar a2 = new ExpressionVar(5);
			ExpressionVar a = new ExpressionVar("test");
			ExpressionVar b = new ExpressionVar("test");
			ExpressionVar f = new ExpressionVar(4);

			rt.setPublicVariable("a2",a2);

			rt.setProtectedVariable("a.1",a);
			rt.setProtectedVariable("b",b);
			rt.setProtectedVariable("f",f);
			
			expression = new Expression("??a2 = sqrt((a.1 == b) * 4)");
			result = expression.parse(rt);
			System.out.println("a2 = sqrt((a.1 == b) * 4) = " + result.eval());

			result = new Expression("('test' == 'test')/2").parse(rt);
			System.out.println("'test' == 'test'/2 = " + result.eval());

			result = new Expression("a.1 = (a2 * 2)/5").parse(rt);
			System.out.println("a.1 = (a2 * 2)/5 = " + result.eval());

			expression = new Expression("'prefix [' + (a.1) + '] postfix'");
			result = expression.parse(rt);
			System.out.println("1+1/3 = " + result.eval());

			a.setValue(3);
			b.setValue(4);
					
			long millis = System.currentTimeMillis();
			for(int i = 0; i < 10000; i++)
				result.eval();
			long millis2 = System.currentTimeMillis() - millis;
			
			System.out.println("sqrt((a == b) * 4) = " + result + " | time needed =" + millis2);

			
			result = new Expression("2.4/PI").parse(rt);
			System.out.println("2.4/PI = " + result.eval());


			a.setValue(1);
			result = new Expression("random() < 0.5 * $a").parse(rt);
			System.out.println("random() < 0.5 * $a= " + result.eval());
			result = new Expression("not($x<7||sqrt(max($x,9)) <= 3)").parse(rt);
			System.out.println("not(x<7||sqrt(max(x,9)) <= 3) = " + result.eval());
			
		} catch(ExpressionException e){
			System.err.println(e.getMessage());
			
		}
		
	}
}

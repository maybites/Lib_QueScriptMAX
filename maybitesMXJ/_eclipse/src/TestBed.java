import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.maybites.mxj.quescript.MutableDouble;
import de.cnc.expression.AbstractRuntimeEnvironment;
import de.cnc.expression.Expression;
import de.cnc.expression.StandaloneRuntimeEnvironment;
import de.cnc.expression.exceptions.ExpressionEvaluationException;
import de.cnc.expression.exceptions.ExpressionParseException;

public class TestBed {

	static public void main(String[] args){
		Expression exp;
		//Laufzeit-Umgebung mit Variablen
		AbstractRuntimeEnvironment runEnv = new StandaloneRuntimeEnvironment() ;

		Object oValue = null;
		try {
	
			String _content = "/queOne color {sin($f1) + $f2} $f2 $f3";

			List<String> list = new ArrayList<String>();
//			Matcher m = Pattern.compile("([^{]\\S*|.+?})\\s*").matcher(_content);
			Matcher m = Pattern.compile("([^{]\\S*|.+?[}].)\\s*").matcher(_content);
			while (m.find())
			    list.add(m.group(1)); // Add .replace("\"", "") to remove surrounding quotes.

			System.out.println(list);
						
			
			exp = Expression.parse("$anim2#t1+$sin", new StandaloneRuntimeEnvironment());
			//exp = Expression.parse("{for( i := 0 , i <= 3 , i++ , { print( 'for:' + string ) ; println( i ) } ); $a + $b + i}", runEnv);
			//exp = Expression.parse("{'' + if($sin == null, 0, $sin)+'s'}", new StandaloneRuntimeEnvironment() );

			//Setzen Anfangsvariable
			MutableDouble sin = new MutableDouble(1);
			
			runEnv.setVariable("$anim2#t1", new MutableDouble(1));
			runEnv.setVariable("$sin", sin);
			runEnv.setVariable("$string", "test");
			
			sin.setDouble(6);
			

			oValue = exp.eval( runEnv ); // siehe oben: "a+b"
			if(oValue instanceof Number){
				System.out.println( "oValue-Number:"+oValue);
			} else {
				System.out.println( "oValue-String:"+oValue);
			}
		} catch (ExpressionEvaluationException exc) {
			System.err.println(exc.getMessage());
		} catch (ExpressionParseException exc) {
			System.err.println(exc.getMessage());
		}


	}
}

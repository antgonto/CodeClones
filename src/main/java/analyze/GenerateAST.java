package analyze;

import java.io.Serializable;
import java.util.Map;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class GenerateAST implements Serializable{

	public static CompilationUnit parse(String str) {
		Map<String, String> options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_7);
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setCompilerOptions(options);
		parser.setSource(str.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		CompilationUnit cu = (CompilationUnit) parser.createAST(null );
		return cu;
	}
	
}

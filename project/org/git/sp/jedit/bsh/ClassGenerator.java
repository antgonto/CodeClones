package org.gjt.sp.jedit.bsh;

import org.gjt.sp.jedit.bsh.Capabilities.Unavailable;
import java.lang.reflect.InvocationTargetException;

public abstract class ClassGenerator
{
	private static ClassGenerator cg;

	@SuppressWarnings("unchecked")
	public static ClassGenerator getClassGenerator() 
		throws UtilEvalError
	{
		if ( cg == null ) 
		{
			try {
				Class clas = Class.forName( "org.gjt.sp.jedit.bsh.ClassGeneratorImpl" );
				cg = (ClassGenerator)clas.getDeclaredConstructor().newInstance();
			} catch ( Exception e ) {
				throw new Unavailable("ClassGenerator unavailable: "+e);
			}
		}
	
		return cg;
	}

	/**
		Parse the BSHBlock for the class definition and generate the class.
	*/
	public abstract Class generateClass( 
		String name, Modifiers modifiers, 
		Class [] interfaces, Class superClass, BSHBlock block, 
		boolean isInterface, CallStack callstack, Interpreter interpreter 
	)
		throws EvalError;

	/**
		Invoke a super.method() style superclass method on an object instance.
		This is not a normal function of the Java reflection API and is
		provided by generated class accessor methods.
	*/
	public abstract Object invokeSuperclassMethod(
		BshClassManager bcm, Object instance, String methodName, Object [] args
	)
        throws UtilEvalError, ReflectError, InvocationTargetException;

	/**
		Change the parent of the class instance namespace.
		This is currently used for inner class support.
		Note: This method will likely be removed in the future.
	*/
	public abstract void setInstanceNameSpaceParent( 
		Object instance, String className, NameSpace parent );

}

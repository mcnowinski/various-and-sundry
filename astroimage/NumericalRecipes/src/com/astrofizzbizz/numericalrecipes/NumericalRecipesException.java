package com.astrofizzbizz.numericalrecipes;

public class NumericalRecipesException extends Exception
{
	private static final long serialVersionUID = -7272382811735381124L;
	NumericalRecipesException(String smessage)
	{
		super(smessage);
	}
	NumericalRecipesException(String smessage, Throwable cause)
	{
		super(smessage, cause);
	}
	public void printErrorMessage()
	{
		System.out.println("NumericalRecipesException: " + " " + getMessage());
	}

}

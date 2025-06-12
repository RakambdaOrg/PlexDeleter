package fr.rakambda.plexdeleter;

public interface ThrowingRunnable<I>{
	void run(I input) throws Throwable;
}

package fr.rakambda.plexdeleter.schedule;

import org.jetbrains.annotations.NotNull;

public interface IScheduler extends Runnable{
	@NotNull
	String getTaskId();
}

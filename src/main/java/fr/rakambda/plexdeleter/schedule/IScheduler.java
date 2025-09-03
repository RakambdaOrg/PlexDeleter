package fr.rakambda.plexdeleter.schedule;

import org.jspecify.annotations.NonNull;

public interface IScheduler extends Runnable{
	@NonNull
	String getTaskId();
}

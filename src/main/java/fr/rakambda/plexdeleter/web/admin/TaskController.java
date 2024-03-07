package fr.rakambda.plexdeleter.web.admin;

import fr.rakambda.plexdeleter.schedule.IScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/admin/task")
public class TaskController{
	private final TaskScheduler taskScheduler;
	private final Collection<IScheduler> tasks;
	
	@Autowired
	public TaskController(TaskScheduler taskScheduler, Collection<IScheduler> tasks){
		this.taskScheduler = taskScheduler;
		this.tasks = tasks;
	}
	
	@GetMapping("/")
	public List<String> getTasks(){
		return tasks.stream().map(IScheduler::getTaskId).toList();
	}
	
	@PostMapping("/{taskId}/trigger")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void triggerTask(@PathVariable("taskId") String taskId){
		var task = tasks.stream()
				.filter(t -> Objects.equals(t.getTaskId(), taskId))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Invalid task id"));
		taskScheduler.schedule(task, Instant.now());
	}
}

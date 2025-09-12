package com.github.cunvoas.geoserviceisochrone.config;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Configuration du planificateur de tâches (Scheduler).
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {  //implements SchedulingConfigurer {

    /**
     * Active les tâches planifiées.
     * @return Executor pour les tâches planifiées
     */
	@Bean(destroyMethod="shutdown")
    public Executor taskExecutor() {
        return Executors.newSingleThreadScheduledExecutor();
    }
    

    /**
     * Configure le planificateur de tâches.
     * @return TaskScheduler configuré
     */
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(1);
        threadPoolTaskScheduler.setThreadNamePrefix("ThreadPoolTaskScheduler");
        threadPoolTaskScheduler.setWaitForTasksToCompleteOnShutdown(true);
        return threadPoolTaskScheduler;
    }

    /*
	@Override
	// https://www.baeldung.com/spring-scheduled-tasks
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {

        taskRegistrar.setScheduler(taskExecutor());
        taskRegistrar.addTriggerTask(
          new Runnable() {
              @Override
              public void run() {
                  tickService.tick();
              }
          },
          new Trigger() {
              @Override
              public Date nextExecutionTime(TriggerContext context) {
                  Optional<Date> lastCompletionTime =
                    Optional.ofNullable(context.lastCompletionTime());
                  Instant nextExecutionTime =
                    lastCompletionTime.orElseGet(Date::new).toInstant()
                      .plusMillis(tickService.getDelay());
                  return Date.from(nextExecutionTime);
              }
          }
        );
		
	}
	*/
}
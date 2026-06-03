package ca.uhn.fhir.jpa.starter.custom.multitenancy;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.IInterceptorService;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.interceptor.model.RequestPartitionId;
import ca.uhn.fhir.jpa.starter.custom.dbaccess.DatabaseHelper;
import ca.uhn.fhir.jpa.starter.custom.helper.CommonHelper;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.batch2.api.StepExecutionDetails;
import jakarta.annotation.PostConstruct;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Aspect
@Component
@Interceptor
public class TenantPartitionInterceptor {

	private static final Logger ourLog = LoggerFactory.getLogger(TenantPartitionInterceptor.class);


	// ThreadLocal container to bind the active Job ID safely to the current executing worker thread
	private static final ThreadLocal<String> FOR_CURRENT_THREAD_JOB_ID = new ThreadLocal<>();

	@Autowired
	private IInterceptorService myInterceptorService;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@PostConstruct
	public void init() {
		ourLog.info("TenantPartitionInterceptor registering globally...");
		myInterceptorService.registerInterceptor(this);
	}

	/**
	 * SPRING AOP CAPTURE: Intercepts any HAPI Batch2 step execution right as it starts.
	 * This captures the execution details object which contains the pristine Job Instance ID.
	 */
	@Before("execution(* ca.uhn.fhir.batch2.api.IJobStepWorker.run(..))")
	public void captureBatchContext(JoinPoint joinPoint) {
		try {
			Object[] args = joinPoint.getArgs();
			if (args != null && args.length > 0 && args[0] instanceof StepExecutionDetails) {
				StepExecutionDetails<?, ?> details = (StepExecutionDetails<?, ?>) args[0];
				if (details.getInstance().getInstanceId() != null) {
					FOR_CURRENT_THREAD_JOB_ID.set(details.getInstance().getInstanceId());

					/*
					ourLog.info("Spring AOP captured active Job ID '{}' for thread '{}'",
						details.getInstance().getInstanceId(), Thread.currentThread().getName());
					*/
				}
			}
		} catch (Exception e) {
			ourLog.warn("Failed to capture batch context via AOP: {}", e.getMessage());
		}
	}

	@Hook(Pointcut.STORAGE_PARTITION_IDENTIFY_ANY)
	public RequestPartitionId identifyAny(RequestDetails requestDetails) {
		return resolvePartition(requestDetails);
	}

	@Hook(Pointcut.STORAGE_PARTITION_IDENTIFY_READ)
	public RequestPartitionId identifyRead(RequestDetails requestDetails) {
		return resolvePartition(requestDetails);
	}

	private RequestPartitionId resolvePartition(RequestDetails requestDetails) {
		if (requestDetails == null) {
			return RequestPartitionId.defaultPartition();
		}

		//Handle Background Batch Workers, specifically for bulk export
		String className = requestDetails.getClass().getName();
		if (className.toLowerCase().contains(CommonHelper.SYSTEM_REQUEST_DETAILS.toLowerCase())) {

			// Pull the specific Job ID cached for this exact executing thread
			String activeJobId = FOR_CURRENT_THREAD_JOB_ID.get();

			if (activeJobId != null) {
				String partitionName = DatabaseHelper. fetchPartitionNameFromDatabase(activeJobId);
				if (partitionName != null) {
					return RequestPartitionId.fromPartitionName(partitionName);
				}
			}

			// Default fallback for general internal tasks
			return RequestPartitionId.defaultPartition();
		}
		//Handle Background Batch Workers, specifically for bulk export


		String operation = requestDetails.getOperation();
		if (operation !=null &&
			CommonHelper.OPERATION_TYPE_PARTITION_MANAGEMENT_CREATE_PARTITION.equals(operation)) {
			return RequestPartitionId.defaultPartition();
		}

		//Handle Interactive HTTP Web Request Threads
		RequestPartitionId partitionId = GetPartitionId(requestDetails);

		return partitionId;
	}

	private  RequestPartitionId GetPartitionId(RequestDetails requestDetails) {
		String headerTenantId = requestDetails.getTenantId();
		if (headerTenantId == null || headerTenantId.isBlank()) {
			return RequestPartitionId.defaultPartition();
		}

		return RequestPartitionId.fromPartitionName(headerTenantId);
	}
}
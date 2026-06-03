package ca.uhn.fhir.jpa.starter.custom.dbaccess;

import ca.uhn.fhir.batch2.model.JobInstance;
import org.quartz.Job;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class DatabaseHelper {
	private static final Pattern PARTITION_NAME_PATTERN = Pattern.compile("\"partitionNames\":\\s*\\[\\s*\"([^\"]+)\"\\s*\\]");

	public static String fetchPartitionNameFromDatabase(String jobId) {

		Object[] params = new Object[] { jobId };
		DataLayer dblayer = new DataLayer();

		try {
			String query = "SELECT params_json_vc FROM BT2_JOB_INSTANCE WHERE id = ?";
			dblayer.Connect();
			List<Map<String, Object>> result = dblayer.execute(query, params);

			for (Map<String, Object> row : result) {
				String jsonParams = row.get("params_json_vc").toString();

				if (jsonParams != null) {
					Matcher matcher = PARTITION_NAME_PATTERN.matcher(jsonParams);
					if (matcher.find()) {
						return matcher.group(1);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try{
				dblayer.CloseConnection();
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		return null;
	}

	public static JobInstance GetJobInstanceByJobId(String jobId) {
		JobInstance jobInstance = new JobInstance();

		String query = "SELECT id, job_cancelled, cmb_recs_processed, cmb_recs_per_sec, create_time, cur_gated_step_id, definition_id, definition_ver, end_time, error_count, error_msg, est_remaining, fast_tracking, params_json, params_json_lob, params_json_vc, progress_pct, report, report_vc, start_time, stat, tot_elapsed_millis, client_id, user_name, update_time, warning_msg, work_chunks_purged FROM BT2_JOB_INSTANCE WHERE ID = ?";
		Object[] params = new Object[] { jobId };
		DataLayer dblayer = new DataLayer();

		try {
			dblayer.Connect();
			List<Map<String, Object>> result = dblayer.execute(query, params);
			for (Map<String, Object> row : result) {

				jobInstance.setCancelled((Boolean) row.get("job_cancelled"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			try{
				dblayer.CloseConnection();
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		return jobInstance;
	}

}

package net.measurementlab.ndt;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity to display the results of the testing, including summary, detailed
 * and expert views.
 * 
 * @author gideon@newamerica.net
 * 
 */
public class ResultsActivity extends Activity {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.results);

		// surprising that typeface cannot be set in styles.xml or
		// even in the respective layout .xml file
		NdtSupport.applyFont(this, R.id.ResultsHeader);
		NdtSupport.applyFont(this, R.id.UploadSpeedHeader, R.id.UploadSpeed,
				R.id.UploadSpeedMbps);
		NdtSupport.applyFont(this, R.id.DownloadSpeedHeader,
				R.id.DownloadSpeed, R.id.DownloadSpeedMbps);
		NdtSupport.applyFont(this, R.id.DetailedResultsHeader,
				R.id.DetailedResultsInfo);
		NdtSupport.applyFont(this, R.id.AdvancedResultsHeader,
				R.id.AdvancedResultsInfo);
		NdtSupport.applyFont(this, R.id.MoreInfo);

		OnClickListener aboutListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri
						.parse("http://measurementlab.net"));
				startActivity(intent);
			}
		};

		View aboutView = findViewById(R.id.MoreInfo);
		aboutView.setOnClickListener(aboutListener);
		aboutView = findViewById(R.id.MLabLogo);
		aboutView.setOnClickListener(aboutListener);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void onResume() {
		super.onResume();
		Intent intent = getIntent();
		if ( intent != null ) {
			Map<String, Object> variables = (Map<String, Object>) intent
				.getSerializableExtra(NdtService.EXTRA_VARS);
			if ( variables.containsKey(new String("pub_isReady")) && 
			     variables.get("pub_isReady").equals("yes") ) { 
				formatSummaryResults(variables);
				formatDetailedResults(variables);
				String diagnosticStatus = intent
						.getStringExtra(NdtService.EXTRA_DIAG_STATUS);
				formatAdvancedResults(diagnosticStatus);
			}
		}
		Toast resultsHint = Toast.makeText(getApplicationContext(),
				getString(R.string.results_swipe_hint), 10);
		resultsHint.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.results, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.MenuEmail: {
			emailResults();

			return true;
		}
		case R.id.MenuStartAgain: {
			Intent intent = new Intent(getApplicationContext(),
					TestsActivity.class);
			startActivity(intent);

			return true;
		}
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void formatSummaryResults(Map<String, Object> variables) {
		DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance();
		format.setMaximumFractionDigits(2);

		Double uploadSpeed = (Double) variables.get("pub_c2sspd");
		TextView textView = (TextView) findViewById(R.id.UploadSpeed);
		textView.setText(format.format(uploadSpeed));
    
		Double downloadSpeed = (Double) variables.get("pub_s2cspd");
		textView = (TextView) findViewById(R.id.DownloadSpeed);
		textView.setText(format.format(downloadSpeed));

		Integer maxRtt = (Integer) variables.get("pub_MaxRTT");
		Integer minRtt = (Integer) variables.get("pub_MinRTT");
		Double avgRtt = (Double) variables.get("pub_avgrtt");
		variables.put("pub_jitter", maxRtt - minRtt);

		String latency = getString(R.string.results_latency, Math.round(avgRtt));
		textView = (TextView) findViewById(R.id.Latency);
		textView.setText(latency);

		String jitter = getString(R.string.results_jitter, maxRtt - minRtt);
		textView = (TextView) findViewById(R.id.Jitter);
		textView.setText(jitter);
	}

	private void formatDetailedResults(Map<String, Object> variables) {
		StringBuilder results = new StringBuilder();
		results.append(formatDetailedLine(R.string.results_detailed_system,
				variables, "pub_osName", "pub_osVer", "pub_javaVer",
				"pub_osArch"));
		results.append('\n');

		results.append(formatDetailedLine(R.string.results_detailed_tcp,
				variables, "pub_CurRwinRcvd", "pub_MaxRwinRcvd"));
		results.append(formatDetailedLine(
				R.string.results_detailed_packet_loss, variables, "pub_loss"));
		results.append(formatDetailedLine(R.string.results_detailed_rtt,
				variables, "pub_MinRTT", "pub_MaxRTT", "pub_avgrtt"));
		results.append(formatDetailedLine(R.string.results_detailed_jitter,
				variables, "pub_jitter"));
		Integer curRTO = (Integer) variables.get("pub_CurRTO");
		Integer timeouts = (Integer) variables.get("pub_Timeouts");
		if (null == timeouts) {
			timeouts = 0;
		}
		Integer waitSec = (curRTO * timeouts) / 1000;
		variables.put("pub_WaitSec", waitSec);
		results.append(formatDetailedLine(R.string.results_detailed_timeout,
				variables, "pub_WaitSec", "pub_CurRTO"));
		results.append(formatDetailedLine(R.string.results_detailed_ack,
				variables, "pub_SACKsRcvd"));

		results.append('\n');

		if (isVarSet(variables, "pub_mismatch")) {
			results.append(getString(R.string.results_detailed_mismatch));
		} else {
			results.append(getString(R.string.results_detailed_no_mismatch));
		}
		results.append('\n');

		if (isVarSet(variables, "pub_Bad_cable")) {
			results.append(getString(R.string.results_detailed_cable_fault));
		} else {
			results.append(getString(R.string.results_detailed_no_cable_fault));
		}
		results.append('\n');

		if (isVarSet(variables, "pub_congestion")) {
			results.append(getString(R.string.results_detailed_congestion));
		} else {
			results.append(getString(R.string.results_detailed_no_congestion));
		}
		results.append('\n');

		if (isVarSet(variables, "pub_natBox")) {
			results.append(getString(R.string.results_detailed_nat));
		} else {
			results.append(getString(R.string.results_detailed_no_nat));
		}
		results.append('\n');

		results.append(formatDetailedLine(R.string.results_detailed_cwndtime, variables,
				"pub_pctcwndtime"));
		results.append(formatDetailedLine(R.string.results_detailed_rcvr_limiting, variables,
				"pub_pctRcvrLimited"));
		variables.put("pub_OptimalRcvrBuffer", ((Integer) variables
				.get("pub_MaxRwinRcvd") )); // NB: value is in octets 
		results.append(formatDetailedLine(R.string.results_detailed_optimal_rcvr_buffer,
				variables, "pub_OptimalRcvrBuffer"));
		// TODO re-enable when link detection is more reliable
//		results.append(formatDetailedLine(R.string.results_detailed_bottleneck_link,
//				variables, "pub_AccessTech"));
		// haven't been able to deduce where this var is set
//		results.append(formatDetailedLine(R.string.results_detailed_dupe_acks, variables,
//				"pub_DupAcksIn"));

		TextView textView = (TextView) findViewById(R.id.DetailedResultsInfo);
		textView.setText(results.toString());
	}

	private void formatAdvancedResults(String diagnosticStatus) {
		String advanced = getString(R.string.results_advanced_web100,
				diagnosticStatus);
		TextView textView = (TextView) findViewById(R.id.AdvancedResultsInfo);
		textView.setText(advanced);
	}

	private String formatDetailedLine(int templateId,
			Map<String, Object> variables, String... paramKeys) {
		List<Object> params = new LinkedList<Object>();
		for (String paramKey : paramKeys) {
			params.add(variables.get(paramKey));
		}
		String message = getString(templateId, params.toArray(new Object[params
				.size()]));
		return message.concat("\n");
	}
	
	private void emailResults() {
	    Intent intentMail = new Intent(Intent.ACTION_SEND);
		TextView textView = (TextView) findViewById(R.id.AdvancedResultsInfo);
	    intentMail.putExtra(Intent.EXTRA_TEXT, textView.getText());
	    String emailTitle = getString(R.string.email_title, new Date().toString());
	    intentMail.putExtra(Intent.EXTRA_SUBJECT, emailTitle);
	    intentMail.setType("plain/text");
	    startActivity(Intent.createChooser(intentMail, "Choose Email Client"));
	}
	
	private boolean isVarSet(Map<String,Object> variables, String key) {
		Object value = variables.get(key);
		if (value instanceof String) {
			return "yes".equals(value) || "1".equals(value);
		}
		return Integer.valueOf(1).equals(value);
	}
}

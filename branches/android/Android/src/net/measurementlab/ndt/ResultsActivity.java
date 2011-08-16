package net.measurementlab.ndt;

import java.text.DecimalFormat;
import java.util.Map;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.TextView;

public class ResultsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.results);

		Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/League_Gothic.otf");
		TextView textView = (TextView) findViewById(R.id.ResultsHeader);
		textView.setTypeface(typeFace);
		textView = (TextView) findViewById(R.id.UploadSpeedHeader);
		textView.setTypeface(typeFace);
		textView = (TextView) findViewById(R.id.UploadSpeed);
		textView.setTypeface(typeFace);
		textView = (TextView) findViewById(R.id.UploadSpeedMbps);
		textView.setTypeface(typeFace);
		textView = (TextView) findViewById(R.id.DownloadSpeedHeader);
		textView.setTypeface(typeFace);
		textView = (TextView) findViewById(R.id.DownloadSpeed);
		textView.setTypeface(typeFace);
		textView = (TextView) findViewById(R.id.DownloadSpeedMbps);
		textView.setTypeface(typeFace);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onResume() {
		super.onResume();
		Map<String,Object> variables = (Map<String,Object>) getIntent().getSerializableExtra(NdtService.EXTRA_VARS);
		
		DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance();
		format.setMaximumFractionDigits(2);

		Double uploadSpeed = (Double) variables.get("pub_c2sspd");
		TextView textView = (TextView) findViewById(R.id.UploadSpeed);
		textView.setText(format.format(uploadSpeed));

		Double downloadSpeed = (Double) variables.get("pub_s2cspd");
		textView = (TextView) findViewById(R.id.DownloadSpeed);
		textView.setText(format.format(downloadSpeed));
	}
}

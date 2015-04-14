package at.eht.stream;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * MainActivity receives acceleration data from Pebble and saves it in the form of Samples
 * in the SampleCollection. Receiving data is paused when the Activity is paused.
 * Data can be exported as CSV file.
 *
 * @author Markus Deutsch
 */
public class MainActivity extends Activity {

    /*
     * Make sure PEBBLE_UUID matches the APP UUID in the settings of your CloudPebble project.
     * All the other vars need to match the constant values at the beginning of main.c
     */
    private final static UUID PEBBLE_UUID = UUID.fromString("e892fa89-8790-4fc7-a77e-19b4e09d53f4");
    private final static int KEY_COMMAND = 0;
    private final static int COMMAND_DATA = 1;
    private final static int NUMBER_SAMPLES = 20;
    private final static int NUMBER_PARAMETERS = 3;
    private final static String LOG_TAG = "Accel Streamer";

    private SampleCollection collection;
    private PebbleKit.PebbleDataReceiver pebbleDataReceiver;
    private TextView tvStatus;
    private int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvStatus = (TextView) findViewById(R.id.tvStatus);
        ((TextView) findViewById(R.id.tvMaxNumber)).setText(getString(R.string.retention, SampleCollection.MAX_RETENTION_COUNT));

        if(savedInstanceState != null && savedInstanceState.containsKey("COUNT")){
            count = savedInstanceState.getInt("COUNT", 0);
            updateCount();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        collection = SampleCollection.getInstance();

        pebbleDataReceiver = new PebbleKit.PebbleDataReceiver(PEBBLE_UUID) {
            @Override
            public void receiveData(Context context, int transactionId, PebbleDictionary pebbleTuples) {
                if(!pebbleTuples.contains(KEY_COMMAND) || pebbleTuples.getInteger(KEY_COMMAND) != COMMAND_DATA){
                    Log.i(LOG_TAG, "Message with invalid/no command received.");
                    return;
                }

                PebbleKit.sendAckToPebble(context, transactionId);

                Sample[] samples = new Sample[NUMBER_SAMPLES];

                for(int i = 0; i < NUMBER_SAMPLES; i++){
                    Sample sample = new Sample();
                    sample.setX(pebbleTuples.getInteger(i * NUMBER_PARAMETERS + 0 + 1).intValue());
                    sample.setY(pebbleTuples.getInteger(i * NUMBER_PARAMETERS + 1 + 1).intValue());
                    sample.setZ(pebbleTuples.getInteger(i * NUMBER_PARAMETERS + 2 + 1).intValue());
                    samples[i] = sample;
                    count++;
                }

                collection.insert(samples);
                updateCount();

                /*
                 * At this point Sample[] samples contains all newly received acceleration data
                 * and the collection contains an array of all newly and previously received
                 * data but not more than 500 samples.
                 * Get all data by calling collection.getSnapshot().
                 */

            }
        };

        PebbleKit.registerReceivedDataHandler(this, pebbleDataReceiver);

    }

    public void updateCount(){
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                tvStatus.setText(getString(R.string.count, String.valueOf(count)));
            }
        });
    }

    public void exportData(){
        try {
            File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "acceldata");
            if(!dir.exists()) dir.mkdirs();
            File outputFile = new File(dir.getAbsolutePath(), String.valueOf("accel-"+new Date().getTime()) + ".csv");
            Log.i(LOG_TAG, "Saving output to " + outputFile.getAbsolutePath());
            FileOutputStream outputStream = new FileOutputStream(outputFile);
            for (Sample sample : collection.getSnapshot()) {
                outputStream.write((sample.toString()+"\n").getBytes());
            }
            outputStream.close();
            Toast.makeText(this, "Export finished.", Toast.LENGTH_SHORT).show();
        } catch (IOException e){
            Toast.makeText(this, "Export failed.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(pebbleDataReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_export) {
            exportData();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("COUNT", count);
        super.onSaveInstanceState(outState);
    }
}

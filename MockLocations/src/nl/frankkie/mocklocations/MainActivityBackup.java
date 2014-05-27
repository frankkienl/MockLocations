package nl.frankkie.mocklocations;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author FrankkieNL
 */
public class MainActivityBackup extends Activity {

    Context context;
    String mockProviderName = "gps";
    boolean isMockEnabled = false;
    int delay = 1000 * 2; //2 seconds
    Button btn1;
    Button btn2;
    TextView tv1;
    EditText ed1;
    Timer timer1;
    TimerTask task1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); //To change body of generated methods, choose Tools | Templates.
        context = this;
        initUI();
    }

    /**
     * UI boilerplate
     */
    public void initUI() {
        setContentView(R.layout.main);
        tv1 = (TextView) findViewById(R.id.tv1);
        tv1.setText("Mock Locations\nEnabled: " + isMockEnabled);
        ed1 = (EditText) findViewById(R.id.ed1);
        //ed1.setVisibility(View.GONE); //not needed, just use the name 'mock'
        ed1.setText("gps");
        btn1 = (Button) findViewById(R.id.btn1);
        btn1.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                startMockLocations();
            }
        });
        btn2 = (Button) findViewById(R.id.btn2);
        btn2.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                stopMockLocations();
            }
        });

    }

    public void startMockLocations() {
        isMockEnabled = true;
        mockProviderName = ed1.getText().toString();
        LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        try {
            locationManager.addTestProvider(mockProviderName, false /*network*/, false/*satellite*/, false/*call*/, false/*moneycost*/, true/*altitude*/, true/*speed*/, true/*bearing*/, Criteria.POWER_LOW/*power*/, Criteria.ACCURACY_FINE /*accuracy*/);
            locationManager.setTestProviderEnabled(mockProviderName, true);
            tv1.setText("Mock Locations\nEnabled: " + isMockEnabled);
            if (timer1 != null) {
                timer1.cancel();
                timer1 = null;
            }
            timer1 = new Timer();
            task1 = new TimerTask() {
                @Override
                public void run() {
                    changeMockLocation();
                }
            };
            timer1.schedule(task1, delay, delay); //repeating
        } catch (SecurityException e) {
            //Security Exception
            //User has not enabled Mock-Locations
            isMockEnabled = false;
            enableMockLocationsInSettings();
        } catch (IllegalArgumentException e) {
            //probaly the 'unknown provider issue'
            Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
            isMockEnabled = false;
        }

    }

    public void enableMockLocationsInSettings() {
        Toast.makeText(context, "Please Enable Mock Locations in Settings", Toast.LENGTH_LONG).show();
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
        try {
            startActivity(intent);
        } catch (Exception e) {
            //Apparantly something went wrong here.. Cannot send user to right place in Settings.
        }
    }

    public void changeMockLocation() {
        //Is it needed to call this from the UI-thread?
        LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        Location mockLocation = new Location(mockProviderName);
        Random random = new Random();
        mockLocation.setAccuracy(random.nextFloat() * 100);
        mockLocation.setAltitude(random.nextDouble() * 100);
        mockLocation.setBearing(random.nextFloat() * 360);
        mockLocation.setElapsedRealtimeNanos(System.nanoTime());
        mockLocation.setLatitude(-90 + random.nextDouble() * (90 * 2)); //-90 till 90
        mockLocation.setLongitude(-180 + random.nextDouble() * (180 * 2)); //-180 till 180
        mockLocation.setSpeed(random.nextFloat() * 100); //speed in m/s
        mockLocation.setTime(System.currentTimeMillis());
        locationManager.setTestProviderLocation(mockProviderName, mockLocation);
    }

    public void stopMockLocations() {
        isMockEnabled = false;
        LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        locationManager.setTestProviderEnabled(mockProviderName, false);
        tv1.setText("Mock Locations\nEnabled: " + isMockEnabled);
        if (timer1 != null) {
            timer1.cancel();
            timer1 = null;
        }
        locationManager.removeTestProvider(mockProviderName);
    }

}

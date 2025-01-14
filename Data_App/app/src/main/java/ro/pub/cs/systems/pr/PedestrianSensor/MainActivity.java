package ro.pub.cs.systems.pr.PedestrianSensor;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.LegendRenderer;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private GraphView graphView;
    private LineGraphSeries<DataPoint> seriesCrosswalk1;
    private LineGraphSeries<DataPoint> seriesCrosswalk2;
    private int xValue = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize GraphView
        graphView = findViewById(R.id.graph);
        seriesCrosswalk1 = new LineGraphSeries<>();
        seriesCrosswalk2 = new LineGraphSeries<>();

        graphView.addSeries(seriesCrosswalk1);
        graphView.addSeries(seriesCrosswalk2);

        // Customize GraphView for better visibility
        graphView.getViewport().setYAxisBoundsManual(true);
        graphView.getViewport().setMinY(0);
        graphView.getViewport().setMaxY(10); // Adjust this value based on your data range

        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setMinX(0);
        graphView.getViewport().setMaxX(100); // Display last 100 data points

        // Set colors and styles for the graphs
        seriesCrosswalk1.setColor(android.graphics.Color.RED);
        seriesCrosswalk1.setThickness(8);
        seriesCrosswalk1.setDrawDataPoints(true);
        seriesCrosswalk1.setDataPointsRadius(10);
        seriesCrosswalk1.setTitle("Crosswalk 1");

        seriesCrosswalk2.setColor(android.graphics.Color.GREEN);
        seriesCrosswalk2.setThickness(8);
        seriesCrosswalk2.setDrawDataPoints(true);
        seriesCrosswalk2.setDataPointsRadius(10);
        seriesCrosswalk2.setTitle("Crosswalk 2");

        // Enable the legend
        graphView.getLegendRenderer().setVisible(true);
        graphView.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

        // Connect to MQTT broker
        connectToMqtt();
    }

    private void connectToMqtt() {
        try {
            MqttClient client = new MqttClient("tcp://broker.hivemq.com:1883", MqttClient.generateClientId(), null);
            client.connect();
            client.subscribe("crosswalk/esp32", (topic, message) -> {
                try {
                    String messageContent = new String(message.getPayload());
                    Log.d(TAG, "Received message: " + messageContent);
                    processMessage(messageContent); // Process and update the graph
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void processMessage(String messageContent) {
        try {
            // Split the message into parts
            String[] parts = messageContent.split("-");
            if (parts.length == 2) {
                String crosswalk = parts[0].trim(); // Crosswalk 1 or Crosswalk 2
                String countString = parts[1].trim();
                int count = Integer.parseInt(countString.split(":")[1].trim());

                // Exclude 0 values from being added to the graph
                if (count > 0) {
                    runOnUiThread(() -> {
                        if (crosswalk.equals("Crosswalk 1")) {
                            updateGraph(seriesCrosswalk1, count);
                        } else if (crosswalk.equals("Crosswalk 2")) {
                            updateGraph(seriesCrosswalk2, count);
                        }
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateGraph(LineGraphSeries<DataPoint> series, int yValue) {
        series.appendData(new DataPoint(xValue++, yValue), true, 100);

        // Dynamically adjust Y-axis for visibility
        int maxYValue = Math.max(
                (int) seriesCrosswalk1.getHighestValueY(),
                (int) seriesCrosswalk2.getHighestValueY()
        );
        graphView.getViewport().setMaxY(maxYValue + 2); // Add padding for readability
        graphView.getViewport().setMinY(0);
        graphView.getViewport().setMinX(0);// Ensure Y-axis starts at 0
    }
}

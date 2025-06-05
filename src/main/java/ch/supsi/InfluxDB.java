package ch.supsi;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;

import java.io.IOException;
import java.time.Instant;

public class InfluxDB {
    private static final String TOKEN = "Eup7-Qpx76MdqrPYdk2LbERon9ZcW4HYFwbqJyRpAbmwaO0vTrT1HmTdhaxV1FJlALSpJCk33ZrsJ09JxYwXUQ==";
    private static final String BUCKET = "Project";
    private static final String ORG = "SUPSI";

    private InfluxDBClient client;
    private WriteApiBlocking writeApiBlocking;

    public InfluxDB() throws IOException {
        client = InfluxDBClientFactory.create("http://169.254.113.180:8086", TOKEN.toCharArray());
        writeApiBlocking = client.getWriteApiBlocking();

        if (!client.ping())
            throw new IOException("InfluxDB connection failed");
    }

    private void writePointOnDB(Point point){
        System.out.println(point.toLineProtocol());
        try {
            writeApiBlocking.writePoint(BUCKET, ORG, point);
            System.out.println("✔️ Point written successfully");
        } catch (Exception e) {
            System.err.println("❌ Failed to write point: " + e.getMessage());
            e.printStackTrace();
        }

    }

    public void writeBallOnDB(BallColor color, double angle) {
        Point point = Point
                .measurement("balls-py")
                .addTag("color", color.toString())
                .addField("count", 1L)
                .time(Instant.now().toEpochMilli(),WritePrecision.MS);
        writePointOnDB(point);

    }


    public void closeConnection(){
        client.close();
    }
}

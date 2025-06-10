package ch.supsi;

public class Main {

    public static void main(String[] args) throws Exception {
        Logic logic = new Logic();
        logic.start();

        InfluxDB influxDB = new InfluxDB();

        /*for (int i = 0; i < 20; i++) {
            influxDB.writeBallOnDB(BallColor.values()[i % 2], 1);
            Thread.sleep(500);
        }
        influxDB.writeBallOnDB(BallColor.WHITE_YELLOW);
        influxDB.writeBallOnDB(BallColor.WHITE_YELLOW);
        influxDB.writeBallOnDB(BallColor.WHITE_YELLOW);
        influxDB.writeBallOnDB(BallColor.WHITE_YELLOW);
        influxDB.writeBallOnDB(BallColor.WHITE_YELLOW);
        influxDB.writeBallOnDB(BallColor.WHITE_YELLOW);
        influxDB.writeBallOnDB(BallColor.RED_BLUE);*/
    }

}

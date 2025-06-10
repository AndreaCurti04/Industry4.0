package ch.supsi;

import org.iot.raspberry.grovepi.GrovePi;
import org.iot.raspberry.grovepi.pi4j.GrovePi4J;
import org.iot.raspberry.grovepi.sensors.analog.GroveRotarySensor;
import org.iot.raspberry.grovepi.sensors.digital.GroveUltrasonicRanger;
import org.iot.raspberry.grovepi.sensors.i2c.GroveRgbLcd;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Logic {
    private final static double MIN_WHITE_YELLOW = 180;
    private final static double MAX_RED_BLUE = 80;
    private final static double MIN_IDLE_RANGE = 90;
    private final static double MAX_IDLE_RANGE = 120;

    private boolean acquisitionOn;
    private int totalWhiteYellow;
    private int totalRedBlue;
    GrovePi grovePi;
    GroveRotarySensor rotary;
    GroveRgbLcd lcd;
    GroveUltrasonicRanger ranger;
    private boolean flagWhiteYellow;
    private boolean flagRedBlue;
    private boolean emptyTheContainer;
    private boolean insertTheContainer;
    private InfluxDB influx;


    public Logic() {
        initializer();
    }

    //rotatory = A0
    //LCD = I2C-3
    //Ultrasonic = D2
    private void initializer() {
        acquisitionOn = true;
        totalRedBlue = 0;
        totalWhiteYellow = 0;
        flagWhiteYellow = true;
        flagRedBlue = true;
        emptyTheContainer = false;
        insertTheContainer = false;
        try {
            influx = new InfluxDB();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        try {
            grovePi = new GrovePi4J();
            rotary = new GroveRotarySensor(grovePi, 0); //A0
            lcd = grovePi.getLCD(); //I2C-3
            ranger = new GroveUltrasonicRanger(grovePi, 2); //D2
        } catch (Exception e) {
            System.err.println("Error while initializing");
        }
    }

    public void start() {
        Logger.getLogger("GrovePi").setLevel(Level.OFF);
        Logger.getLogger("RaspberryPi").setLevel(Level.OFF);

        //GroveUltrasonicRanger ranger = new GroveUltrasonicRanger(grovePi, 6);
        String data;
        try {
            double degrees, distance;
            lcd.setRGB(0,0,0);
            while (true) {
                if (acquisitionOn) {
                    degrees = rotary.get().getDegrees();
                    distance = ranger.get();
                    System.out.println("Ranger value: "+distance);
                    System.out.println("Rotatory value: " + degrees);
                    if((totalRedBlue >= 5 || totalWhiteYellow >= 5) && !emptyTheContainer){
                        emptyTheContainer = true;
                        influx.writeContainerOnDB(State.FULL);
                        lcd.setText("Svuotare i      container");
                        lcd.setRGB(255,255,0);
                    }
                    if(distance >= 23.0 && emptyTheContainer){
                        emptyTheContainer = false;
                        totalRedBlue = 0;
                        totalWhiteYellow = 0;
                        lcd.setText("Container       svuotati");
                        influx.writeContainerOnDB(State.EMPTY);
                        lcd.setRGB(0,0,255);
                    }else if(distance >= 23.0){
                        insertTheContainer = true;
                        lcd.setText("Inserire i      container");
                        lcd.setRGB(255,255,0);
                    }else if(distance < 23.0 && insertTheContainer){
                        insertTheContainer = false;
                        lcd.setText("Container       inseriti");
                        lcd.setRGB(255,255,0);
                    }

                    if (degrees > MIN_IDLE_RANGE && degrees < MAX_IDLE_RANGE) {
                        flagWhiteYellow = false;
                        flagRedBlue = false;
                    }
                    if (degrees < MAX_RED_BLUE) {
                        if (!flagRedBlue) {
                            totalRedBlue++;
                            flagRedBlue = true;
                            influx.writeBallOnDB(BallColor.RED_BLUE);
                            if(!emptyTheContainer){
                                lcd.setRGB(255,0,0);
                                lcd.setText("Biscotto        bruciato (" + totalRedBlue + ")");
                            }
                            System.out.println("Biscotto bruciato (" + totalRedBlue + ")");
                        }
                    } else if (degrees > MIN_WHITE_YELLOW) {
                        if (!flagWhiteYellow) {
                            totalWhiteYellow++;
                            flagWhiteYellow = true;
                            influx.writeBallOnDB(BallColor.WHITE_YELLOW);
                            if (!emptyTheContainer){
                                lcd.setRGB(0,255,0);
                                lcd.setText("Biscotto        buono (" + totalWhiteYellow + ")");
                            }
                            System.out.println("Biscotto buono (" + totalWhiteYellow + ")");
                        }
                    }

                }
                Thread.sleep(200);
            }
        } catch (Exception e) {
            System.err.println("Errore: " + e.getMessage());
        }finally {
            influx.closeConnection();
        }

    }
    /*
    *
    * GroveUltrasonicRanger ranger = new GroveUltrasonicRanger(grovePi, 2);
        GroveRotarySensor rotary = new GroveRotarySensor(grovePi, 0);
        GroveRgbLcd lcd = grovePi.getLCD();

        Status status = Status.GATE_CLOSED;
        int people = 0;
        double distance, degrees;
        while (true) {
            if (acquisitionOn) {
                distance = ranger.get();
                degrees = rotary.get().getDegrees();
                System.out.println("Rotatory value: "+degrees);
                System.out.println("Distance value: "+distance);
                switch (status){
                    case WAITING_ENTRY:
                        if(distance < 10.0){
                            lcd.setRGB(0,255,0);
                            people++;
                            status = Status.ENTERED;
                        }
                        break;
                    case WAITING_EXIT:
                        if(distance < 10.0){
                            lcd.setRGB(255,0,0);
                            people--;
                            status = Status.EXITED;
                        }
                        break;
                    case GATE_CLOSED:
                        System.out.println("SONO DENTRO GATE");
                        if(degrees >= 250.0){
                            if(people < MAX_PEOPLE){
                                status = Status.WAITING_ENTRY;
                            }else{
                                System.out.println("Stadio pieno");
                            }
                        } else if (degrees <= 50.0) {
                            if(people > 0){
                                status = Status.WAITING_EXIT;
                            }
                        }
                        break;
                    default:
                        if(degrees >= 140.0 && degrees <= 160.0){
                            lcd.setRGB(0,0,0);
                            status = Status.GATE_CLOSED;
                        }
                        System.out.println("GRADI: "+degrees);
                }
                lcd.setText("Persone: "+people);
            }
            Thread.sleep(500);
        }*/
}

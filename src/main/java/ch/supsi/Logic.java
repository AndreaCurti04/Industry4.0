package ch.supsi;

import org.iot.raspberry.grovepi.GrovePi;
import org.iot.raspberry.grovepi.pi4j.GrovePi4J;
import org.iot.raspberry.grovepi.sensors.analog.GroveRotarySensor;

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
    private boolean flagWhiteYellow;
    private boolean flagRedBlue;

    public Logic() {
        initializer();
    }

    private void initializer(){
        acquisitionOn = true;
        totalRedBlue = 0;
        totalWhiteYellow = 0;
        flagWhiteYellow = false;
        flagRedBlue = false;
        try {
            grovePi = new GrovePi4J();
            rotary = new GroveRotarySensor(grovePi, 0);
        } catch (Exception e) {
            System.err.println("Error while initializing");
        }
    }

    public void start(){
        Logger.getLogger("GrovePi").setLevel(Level.OFF);
        Logger.getLogger("RaspberryPi").setLevel(Level.OFF);
        //GroveUltrasonicRanger ranger = new GroveUltrasonicRanger(grovePi, 6);

        try {
            double degrees;
            while (true) {
                if (acquisitionOn) {
                    degrees = rotary.get().getDegrees();
                    System.out.println("Rotatory value: "+degrees);
                    if(degrees > MIN_IDLE_RANGE && degrees < MAX_IDLE_RANGE){
                        flagWhiteYellow = false;
                        flagRedBlue = false;
                    }
                    if(degrees < MAX_RED_BLUE){
                        if(!flagRedBlue) {
                            totalRedBlue++;
                            flagRedBlue = true;
                            System.out.println("Red/Blue ball added ("+totalRedBlue+")");
                        }
                    }else if(degrees > MIN_WHITE_YELLOW){
                        if(!flagWhiteYellow){
                            totalWhiteYellow++;
                            flagWhiteYellow = true;
                            System.out.println("White/Yellow ball added ("+totalWhiteYellow+")");
                        }
                    }
                }
                Thread.sleep(200);
            }
        } catch (Exception e) {
            System.err.println("Errore: "+e.getMessage());
        }

    }
}

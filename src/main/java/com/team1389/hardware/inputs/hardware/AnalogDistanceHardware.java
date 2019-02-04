package com.team1389.hardware.inputs.hardware;

import java.util.Optional;
import java.util.function.Function;

import com.team1389.hardware.Hardware;
import com.team1389.hardware.inputs.software.RangeIn;
import com.team1389.hardware.registry.Registry;
import com.team1389.hardware.registry.port_types.Analog;
import com.team1389.hardware.value_types.Position;
import com.team1389.hardware.value_types.Value;

import edu.wpi.first.wpilibj.AnalogInput;

/**
 * Provides streams for voltage & distance sensed by any IR analog distance sensor. 
 */
//TODO: what happens if you plug in two distance sensors which want different sample rates?
//TODO: test removing line setting global sample rate, see if it still works
public class AnalogDistanceHardware extends Hardware<Analog>{

    private Optional<AnalogInput> wpiIR;  
    private Function<Double, Double> getDistInInches;
    private double samplesPerSecond;
    private double maxVoltOutput;
    /**
     * 
     * @param sensor Model of sensor being used
     * @param requestedPort port to init this hardware on
     * @param registry overarching registry object from robot project
     */
    public AnalogDistanceHardware(SensorType sensor, Analog requestedPort, Registry registry){
        getDistInInches = sensor.getDistInInches;
        samplesPerSecond = sensor.samplesPerSecond;
        maxVoltOutput = sensor.maxVoltOutput;
        attachHardware(requestedPort, registry);
    }  

    
    public enum SensorType{
        
        //Don't want to magic number this but I don't see a great other option
        SHARP_GP2Y0A21YK0F(volt -> 27/(volt * 2.54), 26, 1.9);

        private Function<Double, Double> getDistInInches;
        private double samplesPerSecond;
        private double maxVoltOutput;

        /**
         * 
         * @param getDistInInches function to apply to calculate distance given a voltage reading
         * @param samplesPerSecond number of times to sample the port per second
         * 
         */
        private SensorType(Function <Double,Double> getDistInInches, double samplesPerSecond, double maxVoltOutput){
            this.getDistInInches = getDistInInches;
            this.samplesPerSecond = samplesPerSecond;
            this.maxVoltOutput = maxVoltOutput;
        }
    }

    /**
     * 
     * @return a stream containing position reading from sensor
     */
    public RangeIn<Position> getPositionInInches(){
        return new RangeIn<Position>(Position.class,() -> getDistInInches.apply(wpiIR.map(s -> s.getVoltage()).orElse(0.0)), 0, 1);
    }

    /**
     * @return a stream containing raw voltage reading from sensor
     */
    public RangeIn<Value> getVoltageReading(){
        return new RangeIn<Value>(Value.class, () -> wpiIR.map(s -> s.getVoltage()).orElse(0.0), 0, maxVoltOutput);
    }

    @Override
    protected void init(Analog port) {
        AnalogInput IRDistanceSensor = new AnalogInput(port.index());
        AnalogInput.setGlobalSampleRate(samplesPerSecond);
        wpiIR = Optional.of(IRDistanceSensor);
    }
    @Override
    protected void failInit() {
        wpiIR = Optional.empty();
    }
    @Override
    protected String getHardwareIdentifier() {
        return "IR Distance Sensor";
    }


} 

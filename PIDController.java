import java.io.*;
import java.util.*;

public class PIDController {
    // gains
    double Kp;
    double Ki;
    double Kd;

    // sample time (seconds)
    double T;
    double tau;

    // terms and dependent variables
    // (proportional calculated on update)
    double integral;
    double prevError;
    double derivative;
    double prevMeasurement;

    public void PID_init(double p, double i, double d, double time, double t) {
        // value assignments
        Kp = p;
        Ki = i;
        Kd = d;
        T = time;
        tau = t;

        // reset values
        integral = 0;
        prevError = 0;
        derivative = 0;
        prevMeasurement = 0;
    }

    public double PID_update(double x, double xDot, double target) {
        double error = x;

        double Ti = 0.5;
        double Td = 0.5;

        // Ki = Kp / Ti;
        // Kd = Kp * Td;

        // calculate PID terms
        double proportional = Kp * error;
        // integral += 0.5 * Ki * T * (error + prevError); //add incremental rectangles (0.1 * xDiff)
        // derivative = -(2 * Kd * (x - prevMeasurement)
        //             + (2 * tau - T) * derivative)
        //             / (2 * tau + T); // might just need xDot

        integral += Ki * (error - prevError) * 0.01; // time btwn cycles
        derivative = Kd * xDot;

        // set prevError for next iteration
        prevError = error;
        prevMeasurement = x;

        // todo: generate output limit based on projected distance
        // todo: figure out how to move cart w/ no angle change
        // todo: damping

        double output = proportional + integral + derivative;

        // stop applying force when angle is within threshold
        // if (-0.005 < x && x < 0.005) {
        //     output = 0;
        // }

        return output;
    }
}
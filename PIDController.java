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

        // calculate PID terms
        double proportional = Kp * error;
        integral += 0.5 * Ki * T * (error + prevError);
        derivative = -(2 * Kd * (x - prevMeasurement)
                    + (2 * tau - T) * derivative)
                    / (2 * tau + T);

        // set prevError for next iteration
        prevError = error;
        prevMeasurement = x;

        // todo: generate output limit based on projected distance
        // todo: figure out how to move cart w/ no angle change
        double output = proportional + integral + derivative;

        return output;
    }
}
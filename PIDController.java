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
        Kp = p;
        Ki = i;
        Kd = d;
        T = time;
        tau = t;
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

        double output = proportional + integral;

        return output;
    }
}
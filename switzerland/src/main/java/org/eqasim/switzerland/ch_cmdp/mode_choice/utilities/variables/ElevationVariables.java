package org.eqasim.switzerland.ch_cmdp.mode_choice.utilities.variables;

import org.eqasim.core.simulation.mode_choice.utilities.variables.BaseVariables;

public class ElevationVariables implements BaseVariables {
    final public double slope;

    public ElevationVariables(double slope) {
        this.slope = slope;
    }
}

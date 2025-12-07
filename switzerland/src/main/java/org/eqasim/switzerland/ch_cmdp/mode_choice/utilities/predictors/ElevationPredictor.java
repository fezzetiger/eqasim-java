package org.eqasim.switzerland.ch_cmdp.mode_choice.utilities.predictors;

import com.google.inject.Inject;
import org.eqasim.core.simulation.mode_choice.utilities.predictors.CachedVariablePredictor;
import org.eqasim.core.simulation.mode_choice.utilities.predictors.PredictorUtils;
import org.eqasim.switzerland.ch_cmdp.mode_choice.utilities.variables.ElevationVariables;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.contribs.discrete_mode_choice.model.DiscreteModeChoiceTrip;

import java.util.List;

public class ElevationPredictor extends CachedVariablePredictor<ElevationVariables> {
    private final Network network;

    @Inject
    public ElevationPredictor(Network network) {
        this.network = network;
    }

    @Override
    public ElevationVariables predict(Person person, DiscreteModeChoiceTrip trip, List<? extends PlanElement> elements) {

        // --- 1. Links bestimmen ---
        Link originLink = network.getLinks().get(trip.getOriginActivity().getLinkId());
        Link destinationLink = network.getLinks().get(trip.getDestinationActivity().getLinkId());

        if (originLink == null || destinationLink == null) {
            // Defensive fallback bei fehlerhaften Plänen
            return new ElevationVariables(0.0);
        }

        // --- 2. Höheninformationen auslesen ---
        double z_origin = originLink.getFromNode().getCoord().getZ();
        double z_destination = destinationLink.getFromNode().getCoord().getZ();

        double deltaZ = z_destination - z_origin;

        // --- 3. Distanz bestimmen ---
        double distance_km = PredictorUtils.calculateEuclideanDistance_km(trip);

        // --- 4. Slope berechnen ---
        double slope;
        if (distance_km > 0.0) {
            slope = (deltaZ/1000) / distance_km;  
        } else {
            slope = 0.0;
        }

        // --- 5. Variables zurückgeben ---
        return new ElevationVariables(slope);
    }
}

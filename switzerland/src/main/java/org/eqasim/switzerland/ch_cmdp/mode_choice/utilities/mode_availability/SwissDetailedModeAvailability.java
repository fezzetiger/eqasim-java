package org.eqasim.switzerland.ch_cmdp.mode_choice.utilities.mode_availability;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contribs.discrete_mode_choice.model.DiscreteModeChoiceTrip;
import org.matsim.contribs.discrete_mode_choice.model.mode_availability.ModeAvailability;
import org.matsim.core.population.PersonUtils;

public class SwissDetailedModeAvailability implements ModeAvailability {
    @Override
    public Collection<String> getAvailableModes(Person person, List<DiscreteModeChoiceTrip> trips) {
        Boolean isFreight = (Boolean) person.getAttributes().getAttribute("isFreight");

        if (isFreight != null && isFreight) {
            return Collections.singleton("truck");
        }

        Collection<String> modes = new HashSet<>();

        // Modes that are always available
        modes.add(TransportMode.walk);
        modes.add(TransportMode.pt);

        // Check car availability
        boolean carAvailability = true;

        if (PersonUtils.getLicense(person).equals("no")) {
            carAvailability = false;
        }

        if (PersonUtils.getCarAvail(person).equals("never")) {
            carAvailability = false;
        }

        if (carAvailability) {
            modes.add(TransportMode.car);
        }

        // Check bike availability
        boolean bikeAvailability = true;
        Object bikeAttr = person.getAttributes().getAttribute("bikeAvailability");
        if (bikeAttr != null && bikeAttr.equals("FOR_NONE")) {
            bikeAvailability = false;
        }
        if (bikeAvailability) {
            modes.add(TransportMode.bike);
        }

        // Check ebike availability (requires ebikeAvailability attribute, else not available)
        boolean ebikeAvailability = false;
        Object ebikeAttr = person.getAttributes().getAttribute("ebikeAvailability");
        if (ebikeAttr == null) {
            // Some pipelines write "eBikeAvailability" (capital B) – accept both spellings
            ebikeAttr = person.getAttributes().getAttribute("eBikeAvailability");
        }
        if (ebikeAttr instanceof String ebikeAvailabilityString) {
            ebikeAvailability = !"FOR_NONE".equalsIgnoreCase(ebikeAvailabilityString);
        }
        if (ebikeAvailability) {
            modes.add("ebike");
        }

        // Add special mode "outside" if applicable
        Boolean isOutside = (Boolean) person.getAttributes().getAttribute("outside");

        if (isOutside != null && isOutside) {
            modes.add("outside");
        }

        // Add special mode "car_passenger" if applicable
        boolean carPassengerAvailability = !"never".equals(PersonUtils.getCarAvail(person));
        Boolean isCarPassenger = (Boolean) person.getAttributes().getAttribute("isCarPassenger");
        if (isCarPassenger != null && isCarPassenger) {
            carPassengerAvailability = true;
        }

        if (carPassengerAvailability) {
            modes.add("car_passenger");
        }

        // Add special modes "*_loop" if applicable (only if base mode is available)
        List<String> LOOP_MODES      = new ArrayList<>(Arrays.asList("walk_loop", "pt_loop", "bike_loop", "car_loop", "car_passenger_loop", "ebike_loop"));
        List<String> LOOP_ATTRIBUTES = new ArrayList<>(Arrays.asList("hasWalkLoopTrip", "hasPtLoopTrip", "hasBikeLoopTrip", "hasCarLoopTrip", "hasCarPassengerLoopTrip", "hasEBikeLoopTrip"));

        for (int i = 0; i < LOOP_MODES.size(); i++){
            String mode = LOOP_MODES.get(i);
            String attribute = LOOP_ATTRIBUTES.get(i);

            Boolean hasLoopTrip = (Boolean) person.getAttributes().getAttribute(attribute);
            // if no explicit ebike-loop flag is set, fall back to bike loop for ebike_loop
            if (mode.equals("ebike_loop") && hasLoopTrip == null) {
                hasLoopTrip = (Boolean) person.getAttributes().getAttribute("hasBikeLoopTrip");
            }
            if (hasLoopTrip != null && hasLoopTrip) {
                // ensure base availability
                if (mode.equals("bike_loop") && bikeAvailability) {
                    modes.add(mode);
                } else if (mode.equals("ebike_loop") && ebikeAvailability) {
                    modes.add(mode);
                } else if (!mode.equals("bike_loop") && !mode.equals("ebike_loop")) {
                    modes.add(mode);
                }
            }
        }

        return modes;
    }
}

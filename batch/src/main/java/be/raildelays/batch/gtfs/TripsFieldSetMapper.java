package be.raildelays.batch.gtfs;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

/**
 * {@link FieldSetMapper} capable to map a GTFS trips.txt file into a {@link Trip}.
 *
 * @author Almex
 * @since 2.0
 */
public class TripsFieldSetMapper implements GtfsFiledSetMapper<Trip> {

    @Override
    public Trip mapFieldSet(FieldSet fieldSet) throws BindException {
        Trip result = new Trip();

        result.setServiceId(fieldSet.readRawString("service_id"));
        result.setRouteId(fieldSet.readRawString("route_id"));
        result.setTripId(fieldSet.readRawString("trip_id"));

        return result;
    }
}

package be.raildelays.batch.processor;

import be.raildelays.domain.entities.LineStop;
import be.raildelays.domain.entities.Station;
import be.raildelays.domain.entities.TimestampDelay;
import be.raildelays.domain.entities.Train;
import be.raildelays.service.RaildelaysService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import be.raildelays.domain.dto.RouteLogDTO;
import be.raildelays.domain.dto.ServedStopDTO;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * If one stop is not deserved (canceled) then we have no expected time. We must
 * therefore find another way to retrieve line scheduling before persisting a
 * <code>RouteLog</code>.
 *
 * @author Almex
 */
public class AggregateExpectedTimeProcessor implements ItemProcessor<List<LineStop>, List<LineStop>> {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(AggregateExpectedTimeProcessor.class);

    @Resource
    private RaildelaysService service;

    @Override
    public List<LineStop> process(List<LineStop> items) throws Exception {
        List<LineStop> result = null;

        for (LineStop item : items) {
            if (result == null) {
                result = new ArrayList<>();
            }

            result.add(process(item));
        }

        return result;
    }

    public LineStop.Builder fetchScheduling(LineStop item) throws Exception {
        LineStop.Builder result = new LineStop.Builder(item);

        if (item.getArrivalTime() == null || item.getArrivalTime().getExpected() == null ||
                item.getDepartureTime() == null || item.getDepartureTime().getExpected() == null) {
            LOGGER.info("It lacks one expected time from this stop={}", item);

            LineStop line = service.searchScheduledLine(item.getTrain(), item.getStation());

            //-- If we cannot retrieve one of the expected time then this item is corrupted we must filter it.
            if (line == null) {
                LOGGER.debug("We must filter this {}", item);

                return null;
            }

            result.departureTime(new TimestampDelay(line.getDepartureTime().getExpected(), 0L)) //
                    .arrivalTime(new TimestampDelay(line.getArrivalTime().getExpected(), 0L));
        }

        return result;
    }

    public LineStop process(LineStop item) throws Exception {
        LineStop result = null;
        LineStop.Builder builder = fetchScheduling(item);

        //-- Modify backward
        LineStop previous = item.getPrevious();
        while (previous != null) {
            builder.addPrevious(fetchScheduling(previous));
            previous = previous.getPrevious();
        }

        //-- Modify forward
        LineStop next = item.getNext();
        while (next != null) {
            builder.addNext(fetchScheduling(next));
            next = next.getNext();
        }

        result = builder.build();

        LOGGER.debug("LineStop after processing={}", result);

        return result;
    }
}
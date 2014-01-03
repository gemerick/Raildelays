package be.raildelays.batch.processor;

import be.raildelays.batch.bean.BatchExcelRow;
import be.raildelays.domain.entities.LineStop;
import be.raildelays.domain.entities.Station;
import be.raildelays.domain.entities.TimestampDelay;
import be.raildelays.service.RaildelaysService;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Search next train which allow you to arrive earlier to your destination.
 * This processor take into account delays from your departure station and
 * cancellation.
 * 
 * @author Almex
 */
public class SearchNextTrainProcessor implements
		ItemProcessor<List<BatchExcelRow>, List<BatchExcelRow>>, InitializingBean {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(SearchNextTrainProcessor.class);

	@Resource
	private RaildelaysService service;

	@Override
	public void afterPropertiesSet() throws Exception {

	}

	@Override
	public List<BatchExcelRow> process(List<BatchExcelRow> items) throws Exception {
		List<BatchExcelRow> result = new ArrayList<>();
		
		for (BatchExcelRow item : items) {
			result.add(process(item));	
		}
		
		return result;
	}

	public BatchExcelRow process(final BatchExcelRow item) throws Exception {
		BatchExcelRow result = item; // By default we return the item itself
		List<LineStop> candidates = new ArrayList<>();
		LocalDate date = new LocalDate(item.getDate());
		LocalTime time = new LocalTime(item.getExpectedArrivalTime());
		DateTime dateTime = date.toDateTime(time);

		LOGGER.debug("item={}", item);
		
		candidates = service.searchNextTrain(item.getArrivalStation(), dateTime.toDate());

		LOGGER.trace("candidates={}", candidates);

		LineStop fastestTrain = searchFastestTrain(item, candidates);

		if (fastestTrain != null) {
			result = map(item, searchDepartureLineStop(fastestTrain, item.getDepartureStation()), fastestTrain);
			LOGGER.info("Found faster train={}", result);
		}

		return result;
	}

	private BatchExcelRow map(final BatchExcelRow item, LineStop departureStop, LineStop arrivalStop) {	
		LocalDate date = new LocalDate(item.getDate());
		DateTime effectiveArrivalTime = date.toDateTime(new LocalTime(arrivalStop
				.getArrivalTime().getExpected()).plusMinutes(arrivalStop
				.getArrivalTime().getDelay().intValue()));		
		DateTime effectiveDepartureTime = date.toDateTime(new LocalTime(departureStop
				.getDepartureTime().getExpected()).plusMinutes(departureStop
				.getDepartureTime().getDelay().intValue()));
		Duration delay = new Duration(date.toDateTime(new LocalTime(item.getExpectedArrivalTime())), effectiveArrivalTime);
		
		return new BatchExcelRow.Builder(arrivalStop.getDate(),
				item.getSens())
				.arrivalStation(item.getArrivalStation())
				.departureStation(item.getDepartureStation())
				.expectedTrain1(item.getExpectedTrain1())
				.expectedTrain2(item.getEffectiveTrain2())
				.effectiveTrain1(arrivalStop.getTrain())
				.expectedDepartureTime(item.getExpectedDepartureTime())
				.expectedArrivalTime(item.getExpectedArrivalTime())
				.effectiveArrivalTime(effectiveArrivalTime.toDate())
				.effectiveDepartureTime(effectiveDepartureTime.toDate())
				.delay(delay.getMillis() / 1000 / 60)
				.build();
	}

	private LineStop searchFastestTrain(BatchExcelRow item,
			List<LineStop> candidates) {
		LineStop fastestTrain = null;		

		/*
		 * The only delay that we can take into account is the one from the 
		 * departure station. When you have to decide to take another train 
		 * you don't know the effective arrival time.
		 */
		
		for (LineStop candidate : candidates) {
            LineStop candidateDeparture = searchDepartureLineStop(candidate, item.getDepartureStation());
            LineStop candidateArrival = candidate;

            // We don't process null values
            if (candidateDeparture == null || candidateArrival == null) {
                LOGGER.trace("Filtering null: candidateDeparture={} candidateArrival={}", candidateDeparture, candidateArrival);
                continue;
            }
			
			// FIXME We must go recursively into getNext() to search any other 
			// cancellation.
            if (candidateDeparture.isCanceled() || candidateArrival.isCanceled()) {
                LOGGER.trace("Filtering canceling: candidateDeparture={} candidateArrival={}", candidateDeparture, candidateArrival);
                continue;
			}

			if (item.isCanceled()) {
                fastestTrain = candidateArrival;
                break; // candidate arrives before item
			}
			
			// Do not take into account train which leaves after the expected
			// one.
            if (compareTimeAndDelay(candidateDeparture.getDepartureTime(),
                    item.getEffectiveDepartureTime()) >= 0) {
                LOGGER.trace("Filtering candidate leaving after item: candidateDeparture={} candidateArrival={}", candidateDeparture, candidateArrival);
                continue; // candidate leaves after item
			}

			// expected arrivalTime to destination and using delay from departure
            if (compareTime(candidateArrival.getArrivalTime(),
                    item.getEffectiveArrivalTime()) < 0) {
                fastestTrain = candidateArrival;
                break; // candidate arrives before item
			}
		}

		return fastestTrain;
	}
	
	private LineStop searchDepartureLineStop(LineStop lineStop, Station departureStation) {		
		LineStop result = null;

		if (lineStop != null) {
			if (lineStop.getStation().equals(departureStation)) {
				result = lineStop;
			} else if (lineStop.getPrevious() != null) {
				result = searchDepartureLineStop(lineStop.getPrevious(), departureStation);
			}
		}
		
		return result;
	}
	
	public static long compareTime(TimestampDelay departureA, Date departureB) {
        long result = 0;

        if (departureA == null && departureB != null) {
            result = -1;
        } else if (departureA != null && departureB == null) {
            result = 1;
        } else {
            LocalTime localTimeA = new LocalTime(departureA.getExpected());
            LocalTime localTimeB = new LocalTime(departureB.getTime());

            LOGGER.trace("compareTime: start={} - end={}", localTimeB, localTimeA);

            Duration duration = new Duration(localTimeB.toDateTimeToday(), localTimeA.toDateTimeToday());

            result = duration.getMillis();
        }

        return result;
	}

	public static long compareTimeAndDelay(TimestampDelay departureA, Date departureB) {
        long result = 0;

        if (departureA == null && departureB != null) {
            result = -1;
        } else if (departureA != null && departureB == null) {
            result = 1;
        } else {
            LocalTime localTimeA = new LocalTime(departureA.getExpected()).plusMinutes(departureA.getDelay().intValue());
            LocalTime localTimeB = new LocalTime(departureB.getTime());

            LOGGER.trace("compareTimeAndDelay: start={} - end={}", localTimeB, localTimeA);

            Duration duration = new Duration(localTimeB.toDateTimeToday(), localTimeA.toDateTimeToday());

            result = duration.getMillis();
        }

        return result;
	}

	public void setService(RaildelaysService service) {
		this.service = service;
	}

}

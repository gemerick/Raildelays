package be.raildelays.batch.processor;

import be.raildelays.delays.TimeDelay;
import be.raildelays.domain.entities.LineStop;
import be.raildelays.domain.entities.Station;
import be.raildelays.domain.entities.Train;
import be.raildelays.service.RaildelaysService;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalTime;

public class AggregateExpectedTimeProcessorTest {

    public static final Train TRAIN = new Train("0");
    private static final LocalDate TODAY = LocalDate.now();
    private static final Station DEPARTURE_STATION = new Station("Liège-Guillemins");
    private static final Station INTERMEDIATE_STATION = new Station("Bruxelles-Nord");
    private static final Station ARRIVAL_STATION = new Station("Bruxelles-Central");
    private AggregateExpectedTimeProcessor processor;
    private RaildelaysService raildelaysServiceMock;
    private LineStop item;
    private LineStop expected;

    @Before
    public void setUp() throws ParseException {
        processor = new AggregateExpectedTimeProcessor();

        raildelaysServiceMock = EasyMock.createMock(RaildelaysService.class);

        processor.setService(raildelaysServiceMock);

        item = new LineStop
                .Builder()
                .date(TODAY)
                .train(TRAIN)
                .station(INTERMEDIATE_STATION)
                .arrivalTime(TimeDelay.of(LocalTime.parse("18:20"), 0L))
                .departureTime(TimeDelay.of(LocalTime.parse("18:20"), 0L))
                .addNext(new LineStop
                                .Builder()
                                .date(TODAY)
                                .train(TRAIN)
                                .station(ARRIVAL_STATION)
                                .arrivalTime(TimeDelay.of(null, 0L))
                                .departureTime(TimeDelay.of(null, 0L))
                                .canceledArrival(true)
                )
                .addPrevious(new LineStop
                                .Builder()
                                .date(TODAY)
                                .train(TRAIN)
                                .station(DEPARTURE_STATION)
                                .arrivalTime(TimeDelay.of(null, 0L))
                                .departureTime(TimeDelay.of(null, 0L))
                )
                .build();

        expected = new LineStop
                .Builder()
                .date(TODAY)
                .train(TRAIN)
                .station(INTERMEDIATE_STATION)
                .arrivalTime(TimeDelay.of(LocalTime.parse("18:20"), 0L))
                .departureTime(TimeDelay.of(LocalTime.parse("18:21"), 0L))
                .canceledDeparture(true)
                .canceledArrival(true)
                .addNext(new LineStop
                                .Builder()
                                .date(TODAY)
                                .train(new Train("1")).station(ARRIVAL_STATION)
                                .arrivalTime(TimeDelay.of(LocalTime.parse("18:30"), 0L))
                                .departureTime(TimeDelay.of(LocalTime.parse("18:31"), 0L))
                                .canceledDeparture(true)
                                .canceledArrival(true)
                )
                .addPrevious(new LineStop
                                .Builder()
                                .date(TODAY)
                                .train(new Train("1")).station(DEPARTURE_STATION)
                                .arrivalTime(TimeDelay.of(LocalTime.parse("17:00"), 0L))
                                .departureTime(TimeDelay.of(LocalTime.parse("17:01"), 0L))
                                .canceledDeparture(true)
                )
                .build();

		/*
         * Note: when we mock the service we must respect order of expectedTime
		 * arrival time.
		 */
    }

    @Test
    public void testProcess() throws Exception {
        EasyMock.expect(raildelaysServiceMock.searchScheduledLine(TRAIN, DEPARTURE_STATION))
                .andReturn(expected.getPrevious());
        EasyMock.expect(raildelaysServiceMock.searchScheduledLine(TRAIN, INTERMEDIATE_STATION))
                .andReturn(expected);
        EasyMock.expect(raildelaysServiceMock.searchScheduledLine(TRAIN, ARRIVAL_STATION))
                .andReturn(expected.getNext());
        EasyMock.replay(raildelaysServiceMock);

        LineStop result = processor.process(item);

        Assert.assertNotNull(result);

        Assert.assertEquals(LocalTime.parse("17:00"), result.getPrevious().getArrivalTime().getExpectedTime());
        Assert.assertEquals(LocalTime.parse("17:01"), result.getPrevious().getDepartureTime().getExpectedTime());
        Assert.assertEquals(LocalTime.parse("18:20"), result.getArrivalTime().getExpectedTime());
        Assert.assertEquals(LocalTime.parse("18:21"), result.getDepartureTime().getExpectedTime());
        Assert.assertEquals(LocalTime.parse("18:30"), result.getNext().getArrivalTime().getExpectedTime());
        Assert.assertEquals(LocalTime.parse("18:31"), result.getNext().getDepartureTime().getExpectedTime());

        EasyMock.verify(raildelaysServiceMock);
    }

    @Test
    public void testProcessNoCandidate() throws Exception {
        EasyMock.expect(raildelaysServiceMock.searchScheduledLine(TRAIN, DEPARTURE_STATION))
                .andReturn(null);
        EasyMock.expect(raildelaysServiceMock.searchScheduledLine(TRAIN, INTERMEDIATE_STATION))
                .andReturn(null);
        EasyMock.expect(raildelaysServiceMock.searchScheduledLine(TRAIN, ARRIVAL_STATION))
                .andReturn(null);
        EasyMock.replay(raildelaysServiceMock);

        LineStop result = processor.process(item);

        Assert.assertNotNull(result);
        Assert.assertEquals(item, result);
    }
}

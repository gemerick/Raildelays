package be.raildelays.batch.processor;

import be.raildelays.domain.entities.LineStop;
import be.raildelays.domain.entities.Station;
import be.raildelays.domain.entities.TrainLine;
import be.raildelays.logging.Logger;
import be.raildelays.logging.LoggerFactory;
import be.raildelays.repository.LineStopDao;
import be.raildelays.repository.StationDao;
import be.raildelays.repository.TrainLineDao;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.util.function.Function;

/**
 * Make sure to not create duplication in our database when retrieving data from GTFS.
 *
 * @author Almex
 * @since 2.0
 */
public class AggregateLineStopProcessor extends AbstractGtfsDataProcessor<LineStop, LineStop> implements InitializingBean {

    private TrainLineDao trainLineDao;
    private LineStopDao lineStopDao;
    private StationDao stationDao;

    private static final Logger LOGGER = LoggerFactory.getLogger("Agg", AggregateLineStopProcessor.class);

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(trainLineDao, "The property 'trainLineDao' is mandatory");
        Assert.notNull(lineStopDao, "The property 'lineStopDao' is mandatory");
        Assert.notNull(stationDao, "The property 'stationDao' is mandatory");
    }

    @Override
    public LineStop process(LineStop item) throws Exception {
        LineStop result = passThrough(item, this::merge);

        if (result == null) {
            result = passThrough(item, this::aggregate);
        }

        LOGGER.debug("result", result);

        return result;
    }

    private LineStop passThrough(LineStop item, Function<LineStop, LineStop.Builder> function) {
        LineStop.Builder builder = function.apply(item);
        LineStop next = item.getNext();

        if (builder != null) {
            while (next != null) {
                builder.addNext(function.apply(next));
                next = next.getNext();
            }
        }

        return builder != null ? builder.build(false) : null;
    }

    private Station findStation(Station actual) {
        Station result = actual;

        if (!"".equals(actual.getFrenchName())) {
            result = stationDao.findByFrenchName(actual.getFrenchName());
        } else if (!"".equals(actual.getDutchName())) {
            result = stationDao.findByDutchName(actual.getDutchName());
        } else if (!"".equals(actual.getEnglishName())) {
            result = stationDao.findByEnglishName(actual.getEnglishName());
        }

        return result;
    }

    private LineStop.Builder aggregate(LineStop item) {
        LineStop.Builder result = new LineStop.Builder(item, false, false);
        Station station = findStation(item.getStation());
        TrainLine trainLine = trainLineDao.findByRouteId(item.getTrainLine().getRouteId());

        if (station != null) {
            result.station(station);
        } else {
            result.station(stationDao.save(item.getStation()));
        }

        if (trainLine != null) {
            result.trainLine(trainLine);
        } else {
            result.trainLine(trainLineDao.save(item.getTrainLine()));
        }

        return result;
    }

    private LineStop.Builder merge(LineStop item) {
        LineStop.Builder result = null; // If we don't find any existing data we return null
        LineStop actual = lineStopDao.findByTrainLineAndDateAndStation(
                item.getTrainLine(), item.getDate(), item.getStation()
        );

        if (actual != null) {
            result = new LineStop.Builder(item, false, false)
                    .id(actual.getId())
                    .station(actual.getStation())
                    .trainLine(actual.getTrainLine());
        }

        return result;
    }

    public void setTrainLineDao(TrainLineDao trainLineDao) {
        this.trainLineDao = trainLineDao;
    }

    public void setLineStopDao(LineStopDao lineStopDao) {
        this.lineStopDao = lineStopDao;
    }

    public void setStationDao(StationDao stationDao) {
        this.stationDao = stationDao;
    }
}

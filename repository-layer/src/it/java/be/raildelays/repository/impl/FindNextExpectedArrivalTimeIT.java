package be.raildelays.repository.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import be.raildelays.domain.entities.LineStop;
import be.raildelays.domain.entities.Station;
import be.raildelays.repository.LineStopDao;

import com.excilys.ebi.spring.dbunit.config.DBOperation;
import com.excilys.ebi.spring.dbunit.test.DataSet;
import com.excilys.ebi.spring.dbunit.test.RollbackTransactionalDataSetTestExecutionListener;

@DataSet(value = "classpath:FindNextExpectedArrivalTimeIT.xml",
        tearDownOperation = DBOperation.DELETE_ALL, dataSourceSpringName = "dataSource")
public class FindNextExpectedArrivalTimeIT extends AbstractIT {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(FindNextExpectedArrivalTimeIT.class);

	@Resource
	private LineStopDao lineStopDao;

	@Test
	public void testFindNextExpectedArrivalTime() throws ParseException {
		Station station = new Station("Bruxelles-Central");
		Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
				.parse("2000-01-01 16:27:00");

		LOGGER.info("size={}", lineStopDao.findAll().size());
		for (LineStop lineStop : lineStopDao.findAll()) {
			LOGGER.info(lineStop.toString());
		}
		List<LineStop> lineStops = lineStopDao.findNextExpectedArrivalTime(
				station, date);

		Assert.assertEquals(3, lineStops.size());
		Assert.assertEquals("1715", lineStops.get(0).getTrain().getEnglishName());
	}

}

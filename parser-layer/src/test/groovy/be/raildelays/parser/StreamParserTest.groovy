package be.raildelays.parser

import org.apache.log4j.Logger
import org.junit.Assert
import org.junit.Test

import be.raildelays.domain.railtime.Direction
import be.raildelays.domain.railtime.Step
import be.raildelays.httpclient.RequestStreamer
import be.raildelays.httpclient.impl.RailtimeRequestStreamer
import be.raildelays.parser.impl.RailtimeStreamParser
import be.raildelays.util.ParsingUtil

/**
 * Tests for the {@link StreamParser} class.
 */
class StreamParserTest
{
	
	StreamParser parser;
	
	
	Logger log = Logger.getLogger(StreamParserTest.class)
	
	/**
	 * Test train 466 today.
	 */
	@Test
    void testParseDelayFrom466() {	
		RequestStreamer streamer = new RailtimeRequestStreamer();	
		Date date = new Date();
		String train = "466";
		parser = new RailtimeStreamParser(streamer.getDelays(train, date));
		Object object = parser.parseDelay(train, date);
		Assert.assertNotNull("This method should return a result", object);
		Assert.assertNotNull("This method should return a Direction", object instanceof Direction);
	}
	
	@Test
    void testParseDelayFromSample1() {		
		Reader reader = new InputStreamReader(this.getClass().getResourceAsStream("/Sample1.html"));
		parser = new RailtimeStreamParser(reader);
		Object object = parser.parseDelay("477", ParsingUtil.parseDate("13/01/2012"));
		Assert.assertNotNull("This method should return a result", object);
		Assert.assertNotNull("This method should return a Direction", object instanceof Direction);
		Direction direction = (Direction)object;
		Step[] steps = (Step[])direction.getSteps().toArray();
		Assert.assertEquals("Step1 should from station", "Gouvy", steps[0].getStation().getName());
		Assert.assertEquals("Step12 should have a delay of", 0, steps[0].getDelay());
		Assert.assertEquals("Step12 should have timestamp of", ParsingUtil.parseTimestamp("13/01/201205:07"), steps[0].getTimestamp());
		//Assert.assertEquals("Step12 should from station", "Liège-Guillemins", steps[12].getStation().getName());
		Assert.assertEquals("Step12 should have a delay of", 0, steps[12].getDelay());
		Assert.assertEquals("Step12 should have timestamp of", ParsingUtil.parseTimestamp("13/01/201206:34"), steps[12].getTimestamp());
		Assert.assertTrue("Step15 should be canceled", steps[13].isCanceled());
		Assert.assertTrue("Step15 should be canceled", steps[14].isCanceled());
		Assert.assertTrue("Step15 should be canceled", steps[15].isCanceled());
		Assert.assertTrue("Step15 should be canceled", steps[16].isCanceled());
		
	}
	
	
	
	@Test
	void testParseDelayFromSample2() {
		Reader reader = new InputStreamReader(this.getClass().getResourceAsStream("/Sample2.html"));
		parser = new RailtimeStreamParser(reader);
		Object object = parser.parseDelay("466", ParsingUtil.parseDate("11/01/2012"));
		Assert.assertNotNull("This method should return a result", object);
		Assert.assertNotNull("This method should return a Direction", object instanceof Direction);
		Direction direction = (Direction)object;
		Step[] steps = (Step[])direction.getSteps().toArray();
		Assert.assertEquals("Step1 should from station", "Brussels (Bruxelles)-Midi", steps[0].getStation().getName());
		Assert.assertEquals("Step12 should have a delay of", 19, steps[0].getDelay());
		Assert.assertEquals("Step12 should have timestamp of", ParsingUtil.parseTimestamp("11/01/201216:24"), steps[0].getTimestamp());
		Assert.assertEquals("Step12 should from station", "Brussels (Bruxelles)-Central", steps[1].getStation().getName());
		Assert.assertEquals("Step12 should have a delay of", 22, steps[1].getDelay());
		Assert.assertEquals("Step12 should have timestamp of", ParsingUtil.parseTimestamp("11/01/201216:28"), steps[1].getTimestamp());
		Assert.assertEquals("Step12 should from station", "Brussels (Bruxelles)-Nord", steps[2].getStation().getName());
		Assert.assertEquals("Step12 should have a delay of", 22, steps[2].getDelay());
		Assert.assertEquals("Step12 should have timestamp of", ParsingUtil.parseTimestamp("11/01/201216:33"), steps[2].getTimestamp());
		Assert.assertTrue("Step15 should be canceled", steps[4].isCanceled());
		Assert.assertTrue("Step15 should be canceled", steps[5].isCanceled());
	}
}
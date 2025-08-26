/**
 * MIT License

Copyright (c) 2015  Rob Terpilowski

Permission is hereby granted, free of charge, to any person obtaining a copy of this software 
and associated documentation files (the "Software"), to deal in the Software without restriction, 
including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING 
BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE 
OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/

package com.sumzerotrading.realtime.bar.ib.util;

import com.sumzerotrading.data.BarData.LengthUnit;
import com.sumzerotrading.data.StockTicker;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.realtime.bar.RealtimeBarRequest;
import com.sumzerotrading.realtime.bar.ib.BarBuilderJob;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;

/**
 *
 * @author Rob Terpilowski
 */
@Ignore("Ignore until unit tests are fixed")
public class RealtimeBarUtilTest extends TestCase {

    public RealtimeBarUtilTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testBuildJob() {
        String jobName = "myJob";
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(RealtimeBarUtil.BAR_BUILDER_DATA, null);
        JobDetail expectedJob = JobBuilder.newJob(BarBuilderJob.class).withIdentity(jobName).usingJobData(jobDataMap)
                .build();
        assertEquals(expectedJob, RealtimeBarUtil.buildJob(jobName, null));
    }

    @Test
    public void testGetFirstScheduledDate() {
        int interval = 1;
        Date date = new Date();

        LocalDateTime expected = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        expected = expected.withNano(0).withSecond(0).plusMinutes(1);

        while (expected.getMinute() % interval != 0) {
            expected = expected.plusMinutes(1);
        }

        assertEquals(Date.from(expected.atZone(ZoneId.systemDefault()).toInstant()),
                RealtimeBarUtil.getFirstScheduledDate(date, interval));
    }

    @Test
    public void testGetFirstScheduledDate_60MinuteBar() {
        int interval = 60;
        Date date = new Date();

        GregorianCalendar expected = new GregorianCalendar();
        expected.setTime(date);
        expected.set(Calendar.MILLISECOND, 0);
        expected.set(Calendar.SECOND, 0);
        expected.add(Calendar.MINUTE, 1);

        expected.set(Calendar.MINUTE, 0);
        expected.add(Calendar.HOUR, 1);

        assertEquals(expected.getTime(), RealtimeBarUtil.getFirstScheduledDate(date, interval));
    }

    @Test
    public void testGetBarName() {
        int requestId = 1;
        Ticker ticker = new StockTicker("qqq");
        int timeInterval = 1;
        RealtimeBarRequest realtimeBarRequest = new RealtimeBarRequest(requestId, ticker, timeInterval,
                LengthUnit.MINUTE);
        String expected = "ticker: " + realtimeBarRequest.getTicker().getSymbol() + " type: "
                + realtimeBarRequest.getTicker().getInstrumentType() + " barLength:"
                + realtimeBarRequest.getTimeInterval() + realtimeBarRequest.getTimeUnit();

        assertEquals(expected, RealtimeBarUtil.getJobName(realtimeBarRequest));
    }

    @Test
    public void testGetBarDate_ZeroSecond() {
        ZonedDateTime expected = ZonedDateTime.of(2012, 6, 1, 5, 30, 0, 0, ZoneId.systemDefault());

        RealtimeBarUtil.testDateTime = expected;

        assertEquals(expected, RealtimeBarUtil.getBarDate());
    }

    @Test
    public void testGetBarDate_minus3Seconds() {
        ZonedDateTime testDateTime = ZonedDateTime.of(2012, 6, 1, 5, 59, 57, 0, ZoneId.systemDefault());

        ZonedDateTime expected = testDateTime.withSecond(0).withMinute(0).withHour(6);

        RealtimeBarUtil.testDateTime = testDateTime;

        assertEquals(expected, RealtimeBarUtil.getBarDate());
    }

    @Test
    public void testGetBarDate_plus3Seconds() {
        ZonedDateTime testDateTime = ZonedDateTime.of(2012, 6, 1, 5, 1, 3, 0, ZoneId.systemDefault());

        ZonedDateTime expected = testDateTime.withSecond(0).withMinute(1).withHour(5);

        RealtimeBarUtil.testDateTime = testDateTime;

        assertEquals(expected, RealtimeBarUtil.getBarDate());
    }
}

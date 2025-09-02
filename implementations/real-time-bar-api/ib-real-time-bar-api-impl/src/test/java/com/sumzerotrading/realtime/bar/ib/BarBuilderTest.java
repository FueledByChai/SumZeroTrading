/**
 * MIT License
 *
 * Copyright (c) 2015  Rob Terpilowski
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package com.sumzerotrading.realtime.bar.ib;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;

import com.sumzerotrading.data.BarData;
import com.sumzerotrading.data.BarData.LengthUnit;
import com.sumzerotrading.data.InstrumentType;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.historicaldata.IHistoricalDataProvider;
import com.sumzerotrading.marketdata.ILevel1Quote;
import com.sumzerotrading.marketdata.Level1Quote;
import com.sumzerotrading.marketdata.QuoteType;
import com.sumzerotrading.realtime.bar.RealtimeBarListener;
import com.sumzerotrading.realtime.bar.RealtimeBarRequest;
import com.sumzerotrading.realtime.bar.ib.util.RealtimeBarUtil;

import junit.framework.TestCase;

/**
 *
 * @author Rob Terpilowski
 */
@RunWith(MockitoJUnitRunner.class)
@Ignore("Ignore until IB API fixed")
public class BarBuilderTest extends TestCase {

    protected Mockery mockery;

    @Spy
    protected BarBuilder testBarBuilder;

    public BarBuilderTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        mockery = new Mockery();

    }

    @After
    public void tearDown() {
    }

    @Test
    public void testConstructor() throws Exception {
        final SchedulerFactory mockFactory = mockery.mock(SchedulerFactory.class);
        final Scheduler mockScheduler = mockery.mock(Scheduler.class);
        final IHistoricalDataProvider mockHistoricalDataProvider = mockery.mock(IHistoricalDataProvider.class);
        Ticker ticker = new Ticker("qqq").setInstrumentType(InstrumentType.STOCK);
        final RealtimeBarRequest request = new RealtimeBarRequest(1, ticker, 1, LengthUnit.MINUTE);
        String jobName = RealtimeBarUtil.getJobName(request);
        final Trigger trigger = RealtimeBarUtil.getTrigger(jobName, request.getTimeInterval(), request.getTimeUnit());
        final List<BarData> barList = new ArrayList<BarData>();
        BigDecimal barOpen = new BigDecimal(1);
        BigDecimal barHigh = new BigDecimal(2);
        BigDecimal barLow = new BigDecimal(1);
        BigDecimal barClose = new BigDecimal(1);
        BigDecimal barVolume = new BigDecimal(10);

        barList.add(new BarData(ZonedDateTime.now(), new BigDecimal(2), new BigDecimal(3), new BigDecimal(2),
                new BigDecimal(2), new BigDecimal(11)));
        barList.add(new BarData(ZonedDateTime.now(), barOpen, barHigh, barLow, barClose, barVolume));

        mockery.checking(new Expectations() {

            {
                one(mockFactory).getScheduler();
                will(returnValue(mockScheduler));

                one(mockHistoricalDataProvider).requestHistoricalData(request.getTicker(), 1, LengthUnit.DAY,
                        request.getTimeInterval(), request.getTimeUnit(), IHistoricalDataProvider.ShowProperty.TRADES,
                        false);
                will(returnValue(barList));

                one(mockScheduler).scheduleJob(with(any(JobDetail.class)), with(any(Trigger.class)));
                one(mockScheduler).isStarted();
                will(returnValue(true));
            }
        });

        BarBuilder builder = new BarBuilder(mockFactory, request, mockHistoricalDataProvider);
        assertEquals(request, builder.realtimeBarRequest);
        assertEquals(barOpen, builder.open);
        assertEquals(barHigh, builder.high);
        assertEquals(barLow, builder.low);
        assertEquals(barClose, builder.close);
        assertEquals(barVolume, builder.volume);

    }

    @Test
    public void testConstructor_schedulerNotStarted() throws Exception {
        final SchedulerFactory mockFactory = mockery.mock(SchedulerFactory.class);
        final Scheduler mockScheduler = mockery.mock(Scheduler.class);
        final IHistoricalDataProvider mockHistoricalDataProvider = mockery.mock(IHistoricalDataProvider.class);
        Ticker ticker = new Ticker("qqq").setInstrumentType(InstrumentType.STOCK);
        final RealtimeBarRequest request = new RealtimeBarRequest(1, ticker, 1, LengthUnit.MINUTE);
        String jobName = RealtimeBarUtil.getJobName(request);
        final Trigger trigger = RealtimeBarUtil.getTrigger(jobName, request.getTimeInterval(), request.getTimeUnit());
        final List<BarData> barList = new ArrayList<BarData>();
        barList.add(new BarData(ZonedDateTime.now(), new BigDecimal(2), new BigDecimal(3), new BigDecimal(2),
                new BigDecimal(2), new BigDecimal(10)));

        mockery.checking(new Expectations() {

            {
                one(mockFactory).getScheduler();
                will(returnValue(mockScheduler));

                one(mockHistoricalDataProvider).requestHistoricalData(request.getTicker(), 1, LengthUnit.DAY,
                        request.getTimeInterval(), request.getTimeUnit(), IHistoricalDataProvider.ShowProperty.TRADES,
                        false);
                will(returnValue(barList));

                one(mockScheduler).scheduleJob(with(any(JobDetail.class)), with(any(Trigger.class)));
                one(mockScheduler).isStarted();
                will(returnValue(false));

                one(mockScheduler).start();
            }
        });

        BarBuilder builder = new BarBuilder(mockFactory, request, mockHistoricalDataProvider);
        assertEquals(request, builder.realtimeBarRequest);

    }

    @Test
    public void testConstructor_throwsSchedulerException() throws Exception {
        final SchedulerFactory mockFactory = mockery.mock(SchedulerFactory.class);
        final Scheduler mockScheduler = mockery.mock(Scheduler.class);
        final IHistoricalDataProvider mockHistoricalDataProvider = mockery.mock(IHistoricalDataProvider.class);
        Ticker ticker = new Ticker("qqq").setInstrumentType(InstrumentType.STOCK);
        final RealtimeBarRequest request = new RealtimeBarRequest(1, ticker, 1, LengthUnit.MINUTE);
        final List<BarData> barList = new ArrayList<BarData>();
        barList.add(new BarData(ZonedDateTime.now(), new BigDecimal(2), new BigDecimal(3), new BigDecimal(2),
                new BigDecimal(2), new BigDecimal(10)));

        mockery.checking(new Expectations() {

            {
                one(mockFactory).getScheduler();
                will(throwException(new SchedulerException()));

                one(mockHistoricalDataProvider).requestHistoricalData(request.getTicker(), 2, request.getTimeUnit(),
                        request.getTimeInterval(), request.getTimeUnit(), IHistoricalDataProvider.ShowProperty.TRADES,
                        false);
                will(returnValue(barList));
            }
        });

        try {
            BarBuilder builder = new BarBuilder(mockFactory, request, mockHistoricalDataProvider);
            fail();
        } catch (IllegalStateException ex) {
            // this should happen
        }
    }

    @Test
    public void testQuoteReceived() throws Exception {
        final SchedulerFactory mockFactory = mockery.mock(SchedulerFactory.class);
        final Scheduler mockScheduler = mockery.mock(Scheduler.class);
        final ILevel1Quote mockQuote = mockery.mock(ILevel1Quote.class);
        final IHistoricalDataProvider mockHistoricalDataProvider = mockery.mock(IHistoricalDataProvider.class);
        Ticker ticker = new Ticker("qqq").setInstrumentType(InstrumentType.STOCK);
        final RealtimeBarRequest request = new RealtimeBarRequest(1, ticker, 1, LengthUnit.MINUTE);
        String jobName = RealtimeBarUtil.getJobName(request);
        final Trigger trigger = RealtimeBarUtil.getTrigger(jobName, request.getTimeInterval(), request.getTimeUnit());
        final List<BarData> barList = new ArrayList<BarData>();
        barList.add(new BarData(ZonedDateTime.now(), new BigDecimal(2), new BigDecimal(3), new BigDecimal(2),
                new BigDecimal(2), new BigDecimal(10)));

        mockery.checking(new Expectations() {

            {
                one(mockFactory).getScheduler();
                will(returnValue(mockScheduler));

                one(mockHistoricalDataProvider).requestHistoricalData(request.getTicker(), 1, LengthUnit.DAY,
                        request.getTimeInterval(), request.getTimeUnit(), IHistoricalDataProvider.ShowProperty.TRADES,
                        false);
                will(returnValue(barList));

                one(mockScheduler).scheduleJob(with(any(JobDetail.class)), with(any(Trigger.class)));

                one(mockScheduler).isStarted();
                will(returnValue(true));

                one(mockQuote).containsType(QuoteType.VOLUME);
                will(returnValue(false));

                one(mockQuote).containsType(QuoteType.LAST_SIZE);
                will(returnValue(false));

                one(mockQuote).containsType(QuoteType.LAST);
                will(returnValue(true));

                one(mockQuote).getValue(QuoteType.LAST);
                will(returnValue(BigDecimal.ONE));

            }
        });

        BarBuilder builder = new BarBuilder(mockFactory, request, mockHistoricalDataProvider);
        builder.quoteRecieved(mockQuote);
        mockery.assertIsSatisfied();
    }

    @Test
    public void testQuoteReceived_volume() throws Exception {
        final SchedulerFactory mockFactory = mockery.mock(SchedulerFactory.class);
        final Scheduler mockScheduler = mockery.mock(Scheduler.class);
        final ILevel1Quote mockQuote = mockery.mock(ILevel1Quote.class);
        final IHistoricalDataProvider mockHistoricalDataProvider = mockery.mock(IHistoricalDataProvider.class);
        Ticker ticker = new Ticker("qqq").setInstrumentType(InstrumentType.STOCK);
        final RealtimeBarRequest request = new RealtimeBarRequest(1, ticker, 1, LengthUnit.MINUTE);
        String jobName = RealtimeBarUtil.getJobName(request);
        final Trigger trigger = RealtimeBarUtil.getTrigger(jobName, request.getTimeInterval(), request.getTimeUnit());
        final List<BarData> barList = new ArrayList<BarData>();
        barList.add(new BarData(ZonedDateTime.now(), new BigDecimal(2), new BigDecimal(3), new BigDecimal(2),
                new BigDecimal(2), new BigDecimal(10)));

        mockery.checking(new Expectations() {

            {
                one(mockFactory).getScheduler();
                will(returnValue(mockScheduler));

                one(mockHistoricalDataProvider).requestHistoricalData(request.getTicker(), 1, LengthUnit.DAY,
                        request.getTimeInterval(), request.getTimeUnit(), IHistoricalDataProvider.ShowProperty.TRADES,
                        false);
                will(returnValue(barList));

                one(mockScheduler).scheduleJob(with(any(JobDetail.class)), with(any(Trigger.class)));

                one(mockScheduler).isStarted();
                will(returnValue(true));

                one(mockQuote).containsType(QuoteType.VOLUME);
                will(returnValue(true));

                one(mockQuote).containsType(QuoteType.LAST);
                will(returnValue(false));

                one(mockQuote).getValue(QuoteType.VOLUME);
                will(returnValue(BigDecimal.ONE));

            }
        });

        BarBuilder builder = new BarBuilder(mockFactory, request, mockHistoricalDataProvider);
        builder.quoteRecieved(mockQuote);
        mockery.assertIsSatisfied();
    }

    @Test
    public void testAddRemoveBarListeners() throws Exception {
        final SchedulerFactory mockFactory = mockery.mock(SchedulerFactory.class);
        final Scheduler mockScheduler = mockery.mock(Scheduler.class);
        final RealtimeBarListener mockBarListener = mockery.mock(RealtimeBarListener.class);
        final IHistoricalDataProvider mockHistoricalDataProvider = mockery.mock(IHistoricalDataProvider.class);
        Ticker ticker = new Ticker("qqq").setInstrumentType(InstrumentType.STOCK);
        final RealtimeBarRequest request = new RealtimeBarRequest(1, ticker, 1, LengthUnit.MINUTE);
        String jobName = RealtimeBarUtil.getJobName(request);
        final Trigger trigger = RealtimeBarUtil.getTrigger(jobName, request.getTimeInterval(), request.getTimeUnit());
        final List<BarData> barList = new ArrayList<BarData>();
        barList.add(new BarData(ZonedDateTime.now(), new BigDecimal(2), new BigDecimal(3), new BigDecimal(2),
                new BigDecimal(2), new BigDecimal(10)));

        mockery.checking(new Expectations() {

            {
                one(mockFactory).getScheduler();
                will(returnValue(mockScheduler));

                one(mockHistoricalDataProvider).requestHistoricalData(request.getTicker(), 1, LengthUnit.DAY,
                        request.getTimeInterval(), request.getTimeUnit(), IHistoricalDataProvider.ShowProperty.TRADES,
                        false);
                will(returnValue(barList));

                one(mockScheduler).isStarted();
                will(returnValue(true));

                one(mockScheduler).scheduleJob(with(any(JobDetail.class)), with(any(Trigger.class)));

            }
        });

        BarBuilder builder = new BarBuilder(mockFactory, request, mockHistoricalDataProvider);
        builder.addBarListener(mockBarListener);

        assertEquals(1, builder.listenerList.size());
        assertEquals(mockBarListener, builder.listenerList.get(0));

        builder.removeBarListener(mockBarListener);
        assertEquals(0, builder.listenerList.size());
        mockery.assertIsSatisfied();
    }

    @Test
    public void testSetHigh() throws Exception {
        final SchedulerFactory mockFactory = mockery.mock(SchedulerFactory.class);
        final Scheduler mockScheduler = mockery.mock(Scheduler.class);
        final IHistoricalDataProvider mockHistoricalDataProvider = mockery.mock(IHistoricalDataProvider.class);
        Ticker ticker = new Ticker("qqq").setInstrumentType(InstrumentType.STOCK);
        final RealtimeBarRequest request = new RealtimeBarRequest(1, ticker, 1, LengthUnit.MINUTE);
        String jobName = RealtimeBarUtil.getJobName(request);
        final Trigger trigger = RealtimeBarUtil.getTrigger(jobName, request.getTimeInterval(), request.getTimeUnit());
        final List<BarData> barList = new ArrayList<BarData>();
        barList.add(new BarData(ZonedDateTime.now(), new BigDecimal(2), new BigDecimal(3), new BigDecimal(2),
                new BigDecimal(2), new BigDecimal(10)));

        mockery.checking(new Expectations() {

            {
                one(mockFactory).getScheduler();
                will(returnValue(mockScheduler));

                one(mockHistoricalDataProvider).requestHistoricalData(request.getTicker(), 1, LengthUnit.DAY,
                        request.getTimeInterval(), request.getTimeUnit(), IHistoricalDataProvider.ShowProperty.TRADES,
                        false);
                will(returnValue(barList));

                one(mockScheduler).scheduleJob(with(any(JobDetail.class)), with(any(Trigger.class)));

                one(mockScheduler).isStarted();
                will(returnValue(true));

            }
        });

        BarBuilder builder = new BarBuilder(mockFactory, request, mockHistoricalDataProvider);
        assertEquals(new BigDecimal(3), builder.high);
        builder.setHigh(new BigDecimal(100.0));
        assertEquals(new BigDecimal(100.0), builder.high);

        builder.setHigh(new BigDecimal(50.0));
        assertEquals(new BigDecimal(100.0), builder.high);

        builder.setHigh(new BigDecimal(200.0));
        assertEquals(new BigDecimal(200.0), builder.high);

        mockery.assertIsSatisfied();
    }

    @Test
    public void testSetLow() throws Exception {
        final SchedulerFactory mockFactory = mockery.mock(SchedulerFactory.class);
        final Scheduler mockScheduler = mockery.mock(Scheduler.class);
        final IHistoricalDataProvider mockHistoricalDataProvider = mockery.mock(IHistoricalDataProvider.class);
        Ticker ticker = new Ticker("qqq").setInstrumentType(InstrumentType.STOCK);
        final RealtimeBarRequest request = new RealtimeBarRequest(1, ticker, 1, LengthUnit.MINUTE);
        String jobName = RealtimeBarUtil.getJobName(request);
        final Trigger trigger = RealtimeBarUtil.getTrigger(jobName, request.getTimeInterval(), request.getTimeUnit());
        final List<BarData> barList = new ArrayList<BarData>();
        barList.add(new BarData(ZonedDateTime.now(), new BigDecimal(2), new BigDecimal(3), new BigDecimal(200.0),
                new BigDecimal(2), new BigDecimal(10)));

        mockery.checking(new Expectations() {

            {
                one(mockFactory).getScheduler();
                will(returnValue(mockScheduler));

                one(mockHistoricalDataProvider).requestHistoricalData(request.getTicker(), 1, LengthUnit.DAY,
                        request.getTimeInterval(), request.getTimeUnit(), IHistoricalDataProvider.ShowProperty.TRADES,
                        false);
                will(returnValue(barList));

                one(mockScheduler).scheduleJob(with(any(JobDetail.class)), with(any(Trigger.class)));

                one(mockScheduler).isStarted();
                will(returnValue(true));

            }
        });

        BarBuilder builder = new BarBuilder(mockFactory, request, mockHistoricalDataProvider);
        assertEquals(new BigDecimal(200.0), builder.low);
        builder.setLow(new BigDecimal(100.0));
        assertEquals(new BigDecimal(100.0), builder.low);

        builder.setLow(new BigDecimal(50.0));
        assertEquals(new BigDecimal(50.0), builder.low);

        builder.setHigh(new BigDecimal(200.0));
        assertEquals(new BigDecimal(50.0), builder.low);

        mockery.assertIsSatisfied();
    }

    @Test
    public void testSetOpen() throws Exception {
        final SchedulerFactory mockFactory = mockery.mock(SchedulerFactory.class);
        final Scheduler mockScheduler = mockery.mock(Scheduler.class);
        final IHistoricalDataProvider mockHistoricalDataProvider = mockery.mock(IHistoricalDataProvider.class);
        Ticker ticker = new Ticker("qqq").setInstrumentType(InstrumentType.STOCK);
        final RealtimeBarRequest request = new RealtimeBarRequest(1, ticker, 1, LengthUnit.MINUTE);
        String jobName = RealtimeBarUtil.getJobName(request);
        final Trigger trigger = RealtimeBarUtil.getTrigger(jobName, request.getTimeInterval(), request.getTimeUnit());
        final List<BarData> barList = new ArrayList<BarData>();
        barList.add(new BarData(ZonedDateTime.now(), new BigDecimal(2), new BigDecimal(3), new BigDecimal(2),
                new BigDecimal(2), new BigDecimal(10)));

        mockery.checking(new Expectations() {

            {
                one(mockFactory).getScheduler();
                will(returnValue(mockScheduler));

                one(mockHistoricalDataProvider).requestHistoricalData(request.getTicker(), 1, LengthUnit.DAY,
                        request.getTimeInterval(), request.getTimeUnit(), IHistoricalDataProvider.ShowProperty.TRADES,
                        false);
                will(returnValue(barList));

                one(mockScheduler).scheduleJob(with(any(JobDetail.class)), with(any(Trigger.class)));

                one(mockScheduler).isStarted();
                will(returnValue(true));

            }
        });

        BarBuilder builder = new BarBuilder(mockFactory, request, mockHistoricalDataProvider);
        assertEquals(new BigDecimal(2.0), builder.open);

        builder.openInitialized = false;
        builder.setOpen(new BigDecimal(100.0));
        assertEquals(new BigDecimal(100.0), builder.open);

        builder.setOpen(new BigDecimal(50.0));
        assertEquals(new BigDecimal(100.0), builder.open);

        mockery.assertIsSatisfied();
    }

    @Test
    public void testSetClose() throws Exception {
        final SchedulerFactory mockFactory = mockery.mock(SchedulerFactory.class);
        final Scheduler mockScheduler = mockery.mock(Scheduler.class);
        final IHistoricalDataProvider mockHistoricalDataProvider = mockery.mock(IHistoricalDataProvider.class);
        Ticker ticker = new Ticker("qqq").setInstrumentType(InstrumentType.STOCK);
        final RealtimeBarRequest request = new RealtimeBarRequest(1, ticker, 1, LengthUnit.MINUTE);
        String jobName = RealtimeBarUtil.getJobName(request);
        final Trigger trigger = RealtimeBarUtil.getTrigger(jobName, request.getTimeInterval(), request.getTimeUnit());
        final List<BarData> barList = new ArrayList<BarData>();
        barList.add(new BarData(ZonedDateTime.now(), new BigDecimal(2), new BigDecimal(3), new BigDecimal(2),
                new BigDecimal(2), new BigDecimal(10)));

        mockery.checking(new Expectations() {

            {
                one(mockFactory).getScheduler();
                will(returnValue(mockScheduler));

                one(mockHistoricalDataProvider).requestHistoricalData(request.getTicker(), 1, LengthUnit.DAY,
                        request.getTimeInterval(), request.getTimeUnit(), IHistoricalDataProvider.ShowProperty.TRADES,
                        false);
                will(returnValue(barList));

                one(mockScheduler).scheduleJob(with(any(JobDetail.class)), with(any(Trigger.class)));

                one(mockScheduler).isStarted();
                will(returnValue(true));

            }
        });

        BarBuilder builder = new BarBuilder(mockFactory, request, mockHistoricalDataProvider);
        assertEquals(new BigDecimal(2.0), builder.close);
        builder.setClose(new BigDecimal(100.0));
        assertEquals(new BigDecimal(100.0), builder.close);

        builder.setClose(new BigDecimal(50.0));
        assertEquals(new BigDecimal(50.0), builder.close);

        mockery.assertIsSatisfied();
    }

    @Test
    public void testSetVolume() throws Exception {
        final SchedulerFactory mockFactory = mockery.mock(SchedulerFactory.class);
        final Scheduler mockScheduler = mockery.mock(Scheduler.class);
        final IHistoricalDataProvider mockHistoricalDataProvider = mockery.mock(IHistoricalDataProvider.class);
        Ticker ticker = new Ticker("qqq").setInstrumentType(InstrumentType.STOCK);
        final RealtimeBarRequest request = new RealtimeBarRequest(1, ticker, 1, LengthUnit.MINUTE);
        String jobName = RealtimeBarUtil.getJobName(request);
        final Trigger trigger = RealtimeBarUtil.getTrigger(jobName, request.getTimeInterval(), request.getTimeUnit());
        final List<BarData> barList = new ArrayList<BarData>();
        barList.add(new BarData(ZonedDateTime.now(), new BigDecimal(2), new BigDecimal(3), new BigDecimal(2),
                new BigDecimal(2), new BigDecimal(10)));

        mockery.checking(new Expectations() {

            {
                one(mockFactory).getScheduler();
                will(returnValue(mockScheduler));

                one(mockHistoricalDataProvider).requestHistoricalData(request.getTicker(), 1, LengthUnit.DAY,
                        request.getTimeInterval(), request.getTimeUnit(), IHistoricalDataProvider.ShowProperty.TRADES,
                        false);
                will(returnValue(barList));

                one(mockScheduler).scheduleJob(with(any(JobDetail.class)), with(any(Trigger.class)));

                one(mockScheduler).isStarted();
                will(returnValue(true));

            }
        });

        BarBuilder builder = new BarBuilder(mockFactory, request, mockHistoricalDataProvider);
        assertEquals(new BigDecimal(10), builder.volume);
        builder.setVolume(new BigDecimal(100));
        assertEquals(new BigDecimal(110), builder.volume);

        builder.setVolume(new BigDecimal(50));
        assertEquals(new BigDecimal(160), builder.volume);

        mockery.assertIsSatisfied();
    }

    @Test
    public void testBuildBarAndFireEvents() throws Exception {
        final SchedulerFactory mockFactory = mockery.mock(SchedulerFactory.class);
        final Scheduler mockScheduler = mockery.mock(Scheduler.class);
        final IHistoricalDataProvider mockHistoricalDataProvider = mockery.mock(IHistoricalDataProvider.class);
        Ticker ticker = new Ticker("qqq").setInstrumentType(InstrumentType.STOCK);
        final RealtimeBarRequest request = new RealtimeBarRequest(1, ticker, 1, LengthUnit.MINUTE);
        String jobName = RealtimeBarUtil.getJobName(request);
        final Trigger trigger = RealtimeBarUtil.getTrigger(jobName, request.getTimeInterval(), request.getTimeUnit());
        final List<BarData> barList = new ArrayList<BarData>();
        barList.add(new BarData(ZonedDateTime.now(), new BigDecimal(2), new BigDecimal(3), new BigDecimal(2),
                new BigDecimal(2), new BigDecimal(10)));
        BigDecimal open = new BigDecimal(1.0);
        BigDecimal high = new BigDecimal(2.0);
        BigDecimal low = new BigDecimal(1.0);
        BigDecimal close = new BigDecimal(2.0);
        BigDecimal volume = new BigDecimal(100);

        mockery.checking(new Expectations() {

            {
                one(mockFactory).getScheduler();
                will(returnValue(mockScheduler));

                one(mockHistoricalDataProvider).requestHistoricalData(request.getTicker(), 1, LengthUnit.DAY,
                        request.getTimeInterval(), request.getTimeUnit(), IHistoricalDataProvider.ShowProperty.TRADES,
                        false);
                will(returnValue(barList));

                one(mockScheduler).scheduleJob(with(any(JobDetail.class)), with(any(Trigger.class)));

                one(mockScheduler).isStarted();
                will(returnValue(true));

            }
        });

        MockFireEventBarBuilder builder = new MockFireEventBarBuilder(mockFactory, request, mockHistoricalDataProvider);
        builder.openInitialized = false;
        builder.highInitialized = false;
        builder.lowInitialized = false;
        builder.volume = BigDecimal.ZERO;

        builder.setOpen(open);
        builder.setHigh(high);
        builder.setLow(low);
        builder.setClose(close);
        builder.setVolume(volume);

        ZonedDateTime barDate = RealtimeBarUtil.getBarDate();

        builder.buildBarAndFireEvents();
        assertEquals(builder.close, builder.open);
        assertEquals(builder.close, builder.high);
        assertEquals(builder.close, builder.low);
        assertEquals(new BigDecimal(0), builder.volume);
        assertFalse(builder.highInitialized);
        assertFalse(builder.lowInitialized);
        assertFalse(builder.openInitialized);

        BarData expectedBar = new BarData();
        expectedBar.setOpen(open);
        expectedBar.setHigh(high);
        expectedBar.setLow(low);
        expectedBar.setClose(close);
        expectedBar.setVolume(volume);
        expectedBar.setDateTime(barDate);

        assertEquals(expectedBar, builder.getBar());
        mockery.assertIsSatisfied();
    }

    @Test
    public void testFireEvent() throws Exception {
        final SchedulerFactory mockFactory = mockery.mock(SchedulerFactory.class);
        final Scheduler mockScheduler = mockery.mock(Scheduler.class);
        final IHistoricalDataProvider mockHistoricalDataProvider = mockery.mock(IHistoricalDataProvider.class);
        Ticker ticker = new Ticker("qqq").setInstrumentType(InstrumentType.STOCK);
        final RealtimeBarRequest request = new RealtimeBarRequest(1, ticker, 1, LengthUnit.MINUTE);
        String jobName = RealtimeBarUtil.getJobName(request);
        final Trigger trigger = RealtimeBarUtil.getTrigger(jobName, request.getTimeInterval(), request.getTimeUnit());
        final RealtimeBarListener mockListener = mockery.mock(RealtimeBarListener.class);
        final List<BarData> barList = new ArrayList<BarData>();
        barList.add(new BarData(ZonedDateTime.now(), new BigDecimal(2), new BigDecimal(3), new BigDecimal(2),
                new BigDecimal(2), new BigDecimal(10)));
        BigDecimal open = new BigDecimal(1.0);
        BigDecimal high = new BigDecimal(2.0);
        BigDecimal low = new BigDecimal(1.0);
        BigDecimal close = new BigDecimal(2.0);
        BigDecimal volume = new BigDecimal(100);

        final BarData expectedBar = new BarData();
        expectedBar.setOpen(open);
        expectedBar.setHigh(high);
        expectedBar.setLow(low);
        expectedBar.setClose(close);
        expectedBar.setVolume(volume);
        expectedBar.setDateTime(ZonedDateTime.now());

        mockery.checking(new Expectations() {

            {
                one(mockFactory).getScheduler();
                will(returnValue(mockScheduler));

                one(mockHistoricalDataProvider).requestHistoricalData(request.getTicker(), 1, LengthUnit.DAY,
                        request.getTimeInterval(), request.getTimeUnit(), IHistoricalDataProvider.ShowProperty.TRADES,
                        false);
                will(returnValue(barList));

                one(mockScheduler).scheduleJob(with(any(JobDetail.class)), with(any(Trigger.class)));

                one(mockScheduler).isStarted();
                will(returnValue(true));
                one(mockListener).realtimeBarReceived(request.getRequestId(), request.getTicker(), expectedBar);

            }
        });

        BarBuilder builder = new BarBuilder(mockFactory, request, mockHistoricalDataProvider);
        builder.addBarListener(mockListener);

        builder.fireEvent(expectedBar);

        mockery.assertIsSatisfied();
    }

    @Test
    public void testGetListenerCount() throws Exception {
        final SchedulerFactory mockFactory = mockery.mock(SchedulerFactory.class);
        final Scheduler mockScheduler = mockery.mock(Scheduler.class);
        final IHistoricalDataProvider mockHistoricalDataProvider = mockery.mock(IHistoricalDataProvider.class);
        Ticker ticker = new Ticker("qqq").setInstrumentType(InstrumentType.STOCK);
        final RealtimeBarRequest request = new RealtimeBarRequest(1, ticker, 1, LengthUnit.MINUTE);
        String jobName = RealtimeBarUtil.getJobName(request);
        final Trigger trigger = RealtimeBarUtil.getTrigger(jobName, request.getTimeInterval(), request.getTimeUnit());
        final RealtimeBarListener mockListener = mockery.mock(RealtimeBarListener.class);
        final List<BarData> barList = new ArrayList<BarData>();
        barList.add(new BarData(ZonedDateTime.now(), new BigDecimal(2), new BigDecimal(3), new BigDecimal(2),
                new BigDecimal(2), new BigDecimal(10)));

        mockery.checking(new Expectations() {

            {
                one(mockFactory).getScheduler();
                will(returnValue(mockScheduler));

                one(mockHistoricalDataProvider).requestHistoricalData(request.getTicker(), 1, LengthUnit.DAY,
                        request.getTimeInterval(), request.getTimeUnit(), IHistoricalDataProvider.ShowProperty.TRADES,
                        false);
                will(returnValue(barList));

                one(mockScheduler).scheduleJob(with(any(JobDetail.class)), with(any(Trigger.class)));

                one(mockScheduler).isStarted();
                will(returnValue(true));

            }
        });

        BarBuilder builder = new BarBuilder(mockFactory, request, mockHistoricalDataProvider);

        assertEquals(0, builder.getListenerCount());
        mockery.assertIsSatisfied();
    }

    @Test
    public void testStop() throws Exception {
        final SchedulerFactory mockFactory = mockery.mock(SchedulerFactory.class);
        final Scheduler mockScheduler = mockery.mock(Scheduler.class);
        final IHistoricalDataProvider mockHistoricalDataProvider = mockery.mock(IHistoricalDataProvider.class);
        Ticker ticker = new Ticker("qqq").setInstrumentType(InstrumentType.STOCK);
        final RealtimeBarRequest request = new RealtimeBarRequest(1, ticker, 1, LengthUnit.MINUTE);
        String jobName = RealtimeBarUtil.getJobName(request);
        final Trigger trigger = RealtimeBarUtil.getTrigger(jobName, request.getTimeInterval(), request.getTimeUnit());
        final JobDetail job = RealtimeBarUtil.buildJob(jobName, null);
        final RealtimeBarListener mockListener = mockery.mock(RealtimeBarListener.class);
        final List<BarData> barList = new ArrayList<BarData>();
        barList.add(new BarData(ZonedDateTime.now(), new BigDecimal(2), new BigDecimal(3), new BigDecimal(2),
                new BigDecimal(2), new BigDecimal(10)));

        mockery.checking(new Expectations() {

            {
                one(mockFactory).getScheduler();
                will(returnValue(mockScheduler));

                one(mockHistoricalDataProvider).requestHistoricalData(request.getTicker(), 1, LengthUnit.DAY,
                        request.getTimeInterval(), request.getTimeUnit(), IHistoricalDataProvider.ShowProperty.TRADES,
                        false);
                will(returnValue(barList));

                one(mockScheduler).scheduleJob(with(any(JobDetail.class)), with(any(Trigger.class)));

                one(mockScheduler).isStarted();
                will(returnValue(true));

                one(mockScheduler).deleteJob(job.getKey());
            }
        });

        BarBuilder builder = new BarBuilder(mockFactory, request, mockHistoricalDataProvider);
        builder.stop();

        mockery.assertIsSatisfied();
    }

    @Test
    public void testStop_ThrowsException() throws Exception {
        final SchedulerFactory mockFactory = mockery.mock(SchedulerFactory.class);
        final Scheduler mockScheduler = mockery.mock(Scheduler.class);
        final IHistoricalDataProvider mockHistoricalDataProvider = mockery.mock(IHistoricalDataProvider.class);
        Ticker ticker = new Ticker("qqq").setInstrumentType(InstrumentType.STOCK);
        final RealtimeBarRequest request = new RealtimeBarRequest(1, ticker, 1, LengthUnit.MINUTE);
        String jobName = RealtimeBarUtil.getJobName(request);
        final Trigger trigger = RealtimeBarUtil.getTrigger(jobName, request.getTimeInterval(), request.getTimeUnit());
        final JobDetail job = RealtimeBarUtil.buildJob(jobName, null);
        final RealtimeBarListener mockListener = mockery.mock(RealtimeBarListener.class);
        final List<BarData> barList = new ArrayList<BarData>();
        barList.add(new BarData(ZonedDateTime.now(), new BigDecimal(2), new BigDecimal(3), new BigDecimal(2),
                new BigDecimal(2), new BigDecimal(10)));

        mockery.checking(new Expectations() {

            {
                one(mockFactory).getScheduler();
                will(returnValue(mockScheduler));

                one(mockHistoricalDataProvider).requestHistoricalData(request.getTicker(), 1, LengthUnit.DAY,
                        request.getTimeInterval(), request.getTimeUnit(), IHistoricalDataProvider.ShowProperty.TRADES,
                        false);
                will(returnValue(barList));

                one(mockScheduler).scheduleJob(with(any(JobDetail.class)), with(any(Trigger.class)));

                one(mockScheduler).isStarted();
                will(returnValue(true));

                one(mockScheduler).deleteJob(job.getKey());
                will(throwException(new SchedulerException()));
            }
        });

        BarBuilder builder = new BarBuilder(mockFactory, request, mockHistoricalDataProvider);
        try {
            builder.stop();
            fail();
        } catch (IllegalStateException ex) {
            // this should happen
        }

        mockery.assertIsSatisfied();
    }

    @Test
    public void testGetValue_Currency() {
        testBarBuilder.isCurrency = true;
        Ticker ticker = new Ticker("XYZ").setInstrumentType(InstrumentType.CURRENCY);
        ZonedDateTime now = ZonedDateTime.now();
        Map<QuoteType, BigDecimal> quoteValues = new HashMap<>();

        Level1Quote quote = new Level1Quote(ticker, now, quoteValues);

        assertEquals(BigDecimal.ZERO, testBarBuilder.getValue(quote));

        quoteValues.put(QuoteType.BID, BigDecimal.ONE);
        quote = new Level1Quote(ticker, now, quoteValues);
        assertEquals(BigDecimal.ZERO, testBarBuilder.getValue(quote));

        quoteValues.clear();
        testBarBuilder.lastBid = BigDecimal.ZERO;
        testBarBuilder.lastAsk = BigDecimal.ZERO;
        quoteValues.put(QuoteType.ASK, BigDecimal.TEN);
        quote = new Level1Quote(ticker, now, quoteValues);
        assertEquals(BigDecimal.ZERO, testBarBuilder.getValue(quote));

        testBarBuilder.lastBid = BigDecimal.ZERO;
        testBarBuilder.lastAsk = BigDecimal.ZERO;
        quoteValues.put(QuoteType.BID, BigDecimal.ONE);
        quote = new Level1Quote(ticker, now, quoteValues);
        assertEquals(new BigDecimal(5.5), testBarBuilder.getValue(quote));
    }

    @Test
    public void testGetValue_NotCurrency_Midpoint() {
        testBarBuilder.isCurrency = false;
        testBarBuilder.showProperty = IHistoricalDataProvider.ShowProperty.MIDPOINT;
        Ticker ticker = new Ticker("XYZ").setInstrumentType(InstrumentType.STOCK);
        ZonedDateTime now = ZonedDateTime.now();
        Map<QuoteType, BigDecimal> quoteValues = new HashMap<>();

        Level1Quote quote = new Level1Quote(ticker, now, quoteValues);
        assertEquals(BigDecimal.ZERO, testBarBuilder.getValue(quote));

        quoteValues.put(QuoteType.MIDPOINT, BigDecimal.ONE);

        assertEquals(BigDecimal.ONE, testBarBuilder.getValue(quote));
    }

    @Test
    public void testGetValue_NotCurrency_NotMidpoint() {
        testBarBuilder.isCurrency = false;
        testBarBuilder.showProperty = IHistoricalDataProvider.ShowProperty.TRADES;
        Ticker ticker = new Ticker("XYZ").setInstrumentType(InstrumentType.STOCK);
        ZonedDateTime now = ZonedDateTime.now();
        Map<QuoteType, BigDecimal> quoteValues = new HashMap<>();
        quoteValues.put(QuoteType.LAST, BigDecimal.ONE);

        Level1Quote quote = new Level1Quote(ticker, now, quoteValues);

        assertEquals(BigDecimal.ONE, testBarBuilder.getValue(quote));

    }

    @Test
    public void testValidQuote() {
        Map<QuoteType, BigDecimal> quoteValues = new HashMap<>();
        testBarBuilder.isCurrency = true;
        Level1Quote quote = new Level1Quote(null, null, quoteValues);

        assertFalse(testBarBuilder.validQuote(quote));

        quoteValues.put(QuoteType.BID, BigDecimal.ONE);
        assertTrue(testBarBuilder.validQuote(quote));

        quoteValues.clear();
        quoteValues.put(QuoteType.ASK, BigDecimal.ONE);
        quote = new Level1Quote(null, null, quoteValues);
        assertTrue(testBarBuilder.validQuote(quote));

        testBarBuilder.showProperty = IHistoricalDataProvider.ShowProperty.MIDPOINT;
        testBarBuilder.isCurrency = false;
        quoteValues.clear();
        quoteValues.put(QuoteType.MIDPOINT, BigDecimal.ONE);
        quote = new Level1Quote(null, null, quoteValues);
        assertTrue(testBarBuilder.validQuote(quote));

        testBarBuilder.showProperty = IHistoricalDataProvider.ShowProperty.TRADES;
        testBarBuilder.isCurrency = false;
        quoteValues.clear();
        quoteValues.put(QuoteType.LAST, BigDecimal.ONE);
        quote = new Level1Quote(null, null, quoteValues);
        assertTrue(testBarBuilder.validQuote(quote));

    }

    private static class MockFireEventBarBuilder extends BarBuilder {

        private BarData bar = null;

        public MockFireEventBarBuilder(SchedulerFactory schedulerFactory, RealtimeBarRequest request,
                IHistoricalDataProvider dataProvider) {
            super(schedulerFactory, request, dataProvider);
        }

        @Override
        protected void fireEvent(BarData bar) {
            this.bar = bar;
        }

        public BarData getBar() {
            return bar;
        }
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sumzerotrading.realtime.bar.ib;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
//import org.apache.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;

import com.sumzerotrading.data.BarData;
import com.sumzerotrading.data.InstrumentType;
import com.sumzerotrading.historicaldata.IHistoricalDataProvider;
import com.sumzerotrading.marketdata.ILevel1Quote;
import com.sumzerotrading.marketdata.QuoteType;
import com.sumzerotrading.realtime.bar.RealtimeBarListener;
import com.sumzerotrading.realtime.bar.RealtimeBarRequest;
import com.sumzerotrading.realtime.bar.ib.util.RealtimeBarUtil;

/**
 *
 * @author Rob Terpilowski
 */
public class BarBuilder implements IBarBuilder {

    protected Logger logger = Logger.getLogger(BarBuilder.class);
    protected BigDecimal open = null;
    protected BigDecimal high = null;
    protected BigDecimal low = null;
    protected BigDecimal close = null;
    protected BigDecimal volume = BigDecimal.ZERO;
    protected Scheduler scheduler;
    protected List<RealtimeBarListener> listenerList = new ArrayList<RealtimeBarListener>();
    protected RealtimeBarRequest realtimeBarRequest;
    protected JobDetail job;
    protected boolean openInitialized = false;
    protected boolean highInitialized = false;
    protected boolean lowInitialized = false;
    protected BigDecimal lastBid = BigDecimal.ZERO;
    protected BigDecimal lastAsk = BigDecimal.ZERO;
    protected boolean isCurrency = false;
    protected int timeInterval = 0;
    protected IHistoricalDataProvider.ShowProperty showProperty;

    // For unit tests
    protected BarBuilder() {
    }

    public BarBuilder(SchedulerFactory schedulerFactory, RealtimeBarRequest request,
            IHistoricalDataProvider historicalDataProvider) {
        try {
            timeInterval = request.getTimeInterval();
            BarData.LengthUnit lengthUnit = request.getTimeUnit();

            if (lengthUnit == lengthUnit.HOUR) {
                lengthUnit = BarData.LengthUnit.MINUTE;
                timeInterval = timeInterval * 60;
            }

            IHistoricalDataProvider.ShowProperty showProperty = IHistoricalDataProvider.ShowProperty.TRADES;
            if (request.getTicker().getInstrumentType() == InstrumentType.CURRENCY) {
                showProperty = IHistoricalDataProvider.ShowProperty.MIDPOINT;
                isCurrency = true;
            }
            this.realtimeBarRequest = request;
            String jobName = RealtimeBarUtil.getJobName(request);
            scheduler = schedulerFactory.getScheduler();
            job = RealtimeBarUtil.buildJob(jobName, this);
            showProperty = request.getShowProperty();
            List<BarData> bars = historicalDataProvider.requestHistoricalData(request.getTicker(), 1,
                    BarData.LengthUnit.DAY, request.getTimeInterval(), request.getTimeUnit(), showProperty, false);
            BarData firstBar = bars.get(bars.size() - 1);
            setOpen(firstBar.getOpen());
            setHigh(firstBar.getHigh());
            setLow(firstBar.getLow());
            setClose(firstBar.getClose());
            setVolume(firstBar.getVolume());
            scheduler.scheduleJob(job, RealtimeBarUtil.getTrigger(jobName, timeInterval, lengthUnit));
            if (!scheduler.isStarted()) {
                scheduler.start();
            }
        } catch (SchedulerException ex) {
            // logger.error(ex, ex);
            ex.printStackTrace();
            throw new IllegalStateException(ex);
        }
    }

    public void quoteRecieved(ILevel1Quote quote) {
        if (quote.containsType(QuoteType.VOLUME)) {
            setVolume(quote.getValue(QuoteType.VOLUME));
        } else if (quote.containsType(QuoteType.LAST_SIZE)) {
            setVolume(quote.getValue(QuoteType.LAST_SIZE));
        }

        if (validQuote(quote)) {
            BigDecimal value = getValue(quote);
            setHigh(value);
            setLow(value);
            setClose(value);
            setOpen(value);
        }
    }

    protected BigDecimal getValue(ILevel1Quote quote) {
        if (isCurrency) {
            if (quote.containsType(QuoteType.BID)) {
                lastBid = quote.getValue(QuoteType.BID);
            }
            if (quote.containsType(QuoteType.ASK)) {
                lastAsk = quote.getValue(QuoteType.ASK);
            }
            if (lastBid.doubleValue() > 0 && lastAsk.doubleValue() > 0) {
                return new BigDecimal(lastAsk.doubleValue() - ((lastAsk.doubleValue() - lastBid.doubleValue()) / 2.0));
            } else {
                return BigDecimal.ZERO;
            }
        } else {
            if (showProperty == IHistoricalDataProvider.ShowProperty.MIDPOINT) {
                if (quote.containsType(QuoteType.MIDPOINT)) {
                    return quote.getValue(QuoteType.MIDPOINT);
                } else {
                    return BigDecimal.ZERO;
                }
            } else {
                return quote.getValue(QuoteType.LAST);
            }

        }
    }

    protected boolean validQuote(ILevel1Quote quote) {
        if (isCurrency) {
            return quote.containsType(QuoteType.BID) || quote.containsType(QuoteType.ASK);
        } else if (showProperty == IHistoricalDataProvider.ShowProperty.MIDPOINT) {
            return quote.containsType(QuoteType.MIDPOINT);
        } else {
            return quote.containsType(QuoteType.LAST);
        }
    }

    @Override
    public void addBarListener(RealtimeBarListener listener) {
        synchronized (listenerList) {
            listenerList.add(listener);
        }
    }

    @Override
    public void removeBarListener(RealtimeBarListener listener) {
        synchronized (listenerList) {
            listenerList.remove(listener);
        }
    }

    protected final void setHigh(BigDecimal price) {
        if (price.equals(BigDecimal.ZERO)) {
            return;
        }
        if (!highInitialized) {
            high = price;
            highInitialized = true;
        } else if (price.doubleValue() > high.doubleValue()) {
            high = price;
        }
    }

    protected final void setLow(BigDecimal price) {
        if (price.equals(BigDecimal.ZERO)) {
            return;
        }
        if (!lowInitialized) {
            low = price;
            lowInitialized = true;
        } else if (price.doubleValue() < low.doubleValue()) {
            low = price;
        }
    }

    protected final void setOpen(BigDecimal price) {
        if (price.equals(BigDecimal.ZERO)) {
            return;
        }
        if (!openInitialized) {
            this.open = price;
            openInitialized = true;
        }
    }

    protected final void setClose(BigDecimal price) {
        if (price.equals(BigDecimal.ZERO)) {
            return;
        }
        close = price;
    }

    protected final void setVolume(BigDecimal volume) {
        this.volume = this.volume.add(volume);
    }

    public void buildBarAndFireEvents() {

        BarData bar = new BarData();
        bar.setOpen(open);
        bar.setHigh(high);
        bar.setLow(low);
        bar.setClose(close);
        bar.setVolume(volume);
        bar.setDateTime(RealtimeBarUtil.getBarDate());
        open = close;
        high = close;
        low = close;
        volume = BigDecimal.ZERO;
        openInitialized = false;
        highInitialized = false;
        lowInitialized = false;

        fireEvent(bar);
    }

    protected void fireEvent(BarData bar) {
        synchronized (listenerList) {
            for (RealtimeBarListener listener : listenerList) {
                try {
                    listener.realtimeBarReceived(realtimeBarRequest.getRequestId(), realtimeBarRequest.getTicker(),
                            bar);
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                }
            }
        }
    }

    public int getListenerCount() {
        return listenerList.size();
    }

    public void stop() {
        try {
            scheduler.deleteJob(job.getKey());
        } catch (SchedulerException ex) {
            throw new IllegalStateException(ex);
        }
    }

}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sumzerotrading.realtime.bar.ib.util;

import com.sumzerotrading.data.BarData.LengthUnit;
import com.sumzerotrading.realtime.bar.RealtimeBarRequest;
import com.sumzerotrading.realtime.bar.ib.BarBuilderJob;
import com.sumzerotrading.realtime.bar.ib.IBarBuilder;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

/**
 *
 * @author Rob Terpilowski
 */
public class RealtimeBarUtil {

    public static final String BAR_BUILDER_DATA = "BarBuilder";
    protected static ZonedDateTime testDateTime = null;

    public static JobDetail buildJob(String jobName, IBarBuilder barBuilder) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(BAR_BUILDER_DATA, barBuilder);
        return JobBuilder.newJob(BarBuilderJob.class).withIdentity(jobName).usingJobData(jobDataMap).build();
    }

    public static Trigger getTrigger(String jobName, int interval, LengthUnit units) {
        // always assume minutes, for now
        return TriggerBuilder.newTrigger().withIdentity(jobName).startAt(getFirstScheduledDate(new Date(), interval))
                .forJob(jobName)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInMinutes(interval).repeatForever())
                .build();

    }

    public static Date getFirstScheduledDate(Date date, int interval) {
        ZonedDateTime ldt = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        ldt = ldt.withNano(0).withSecond(0).plusMinutes(1);

        while (ldt.getMinute() % interval != 0) {
            ldt = ldt.plusMinutes(1);
        }

        return Date.from(ldt.toInstant());

    }

    public static String getJobName(RealtimeBarRequest realtimeBarRequest) {
        return "ticker: " + realtimeBarRequest.getTicker().getSymbol() + " type: "
                + realtimeBarRequest.getTicker().getInstrumentType() + " barLength:"
                + realtimeBarRequest.getTimeInterval() + realtimeBarRequest.getTimeUnit();
    }

    public static ZonedDateTime getBarDate() {
        ZonedDateTime dateTime;
        if (testDateTime == null) {
            dateTime = ZonedDateTime.now();
        } else {
            dateTime = testDateTime;
        }

        int currentSecond = dateTime.getSecond();
        dateTime = dateTime.withNano(0).withSecond(0);

        // Round up to the next minute if more than 30 seconds in.
        if (currentSecond >= 30) {
            dateTime = dateTime.plusMinutes(1);
        }

        return dateTime;

    }

}

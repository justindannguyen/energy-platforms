/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.server.api;

import java.util.Calendar;
import java.util.Date;

/**
 * @author tuan3.nguyen@gmail.com
 */
public interface Dates {
  public static Date nextMonth(final Date date) {
    final Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.add(Calendar.MONTH, 1);
    return calendar.getTime();
  }

  public static Date startOfMonth(final Date date) {
    final Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.set(Calendar.MILLISECOND, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.HOUR, 0);
    calendar.set(Calendar.DAY_OF_MONTH, 1);
    return calendar.getTime();
  }

  public static Date withoutMinute(final Date date) {
    final Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.set(Calendar.MILLISECOND, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MINUTE, 0);
    return calendar.getTime();
  }


}

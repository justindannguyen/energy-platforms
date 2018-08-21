/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.server.stream.aggregation;

import java.util.Calendar;
import java.util.Date;

/**
 * @author tuan3.nguyen@gmail.com
 */
public class Dates {
  public static final Date withoutMinute(final long ms) {
    final Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(ms);
    calendar.set(Calendar.MILLISECOND, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MINUTE, 0);
    return calendar.getTime();
  }
}

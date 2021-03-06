package org.platform.modules.personal.dao;

import java.util.Date;

import org.platform.modules.abstr.dao.IGenericDAO;
import org.platform.modules.personal.entity.Calendar;
import org.springframework.data.jpa.repository.Query;

public interface ICalendarDAO extends IGenericDAO<Calendar, Long> {

	@Query("select count(id) from Calendar where userId=?1 and ((startDate=?2 and (startTime is null or startTime<?3)) or (startDate > ?2 and startDate<=(?2+?4)) or (startDate<?2 and (startDate+length)>?2) or ((startDate+length)=?2 and (endTime is null or endTime>?3)))")
    Long countRecentlyCalendar(Long userId, Date nowDate, Date nowTime, Integer interval);
}

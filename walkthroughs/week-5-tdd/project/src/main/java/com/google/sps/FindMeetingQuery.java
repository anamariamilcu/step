// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.Collection;
import java.util.ArrayList;

public final class FindMeetingQuery {

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {

    ArrayList<String> mandatoryAttendees = new ArrayList<>(request.getAttendees());
    ArrayList<String> attendeesIncludingOptional = 
        new ArrayList<>(request.getOptionalAttendees());
    attendeesIncludingOptional.addAll(mandatoryAttendees);
    
    /* Try to find some time ranges that fits everyone. */
    Collection<TimeRange> timeRangesForEveryone = queryForListOfAttendees(events, 
        attendeesIncludingOptional, (int) request.getDuration());
    if (timeRangesForEveryone.size() > 0) {
      return timeRangesForEveryone;
    }
    /* If there is no time slot in which everyone is available, take into account
       only mandatory attendees.  */
    return queryForListOfAttendees(events, mandatoryAttendees, (int) request.getDuration());

  }

  public Collection<TimeRange> queryForListOfAttendees(Collection<Event> events,
      Collection<String> attendees, int duration) {
    
    /* Initializing an array that will provide information for each minute in a day. 
       dayMinutes[j] will represent what is happening in minute j, where j is
       from 0 to 3599. */
    ArrayList<MinuteDetails> dayMinutes = new ArrayList<>();
    for (int i = 0; i <= TimeRange.END_OF_DAY  + 1; i++) {
      /* Initially, consider that each minute of the day is available for a metting. 
         If that is so, the next available minute would be the next one. */ 
      dayMinutes.add(new MinuteDetails(true, i + 1));
    }
    /* Set in the array the minutes in which all mandatory attendees are not available. */
    setMandatoryAttendeesAvailability(dayMinutes, events, attendees);

    return findAavailableTimeRanges(dayMinutes, duration);
  }

  /* Set in dayMinutes the minutes in which the meeting can't be held and the next availability
     field for every minute. */
  private void setMandatoryAttendeesAvailability(ArrayList<MinuteDetails> dayMinutes,
      Collection<Event> events, Collection<String> attendees) {
        for (Event event : events) {
          for (String person : attendees) {
            if (event.attends(person)) {
              /* Set all the minutes of the current event as unavailabe in dayMinutes. */
              int start = event.getWhen().start();
              int end = event.getWhen().end();
              /* Update the next availability for the minute before the event. */
              if (start > 0 && dayMinutes.get(start - 1).getNextAvailableMinute() < end) {
                dayMinutes.get(start - 1).setNextAvailableMinute(end);
              }
              for (int i = start; i < end; i++) {
                if (i <= TimeRange.END_OF_DAY) {
                  dayMinutes.get(i).setMandatoryAvailability(false);
                  /* Update the next availability for the current minute only if the setted
                  availability does not point to an even later time. */
                  if (dayMinutes.get(i).getNextAvailableMinute() < end) {
                    dayMinutes.get(i).setNextAvailableMinute(end);    
                  }
                } else {
                  break;
                }
              }
              /* There is no point in continuing with checking if other persons are in the
                current event, since those time slots are already set as unavailable. */ 
              break;
            }
          }
        }
  }

  /* Returns a list of time ranges which fulfill all the requierments given in the request. */

  private Collection<TimeRange> findAavailableTimeRanges(ArrayList<MinuteDetails> dayMinutes,
      int duration) {

      Collection<TimeRange> timeRanges = new ArrayList<>();
      /* If the duration exceeds a day, there is no possible time range. */
      if (duration > TimeRange.WHOLE_DAY.duration()) {
        return timeRanges;
      }
      
      int currentStart = TimeRange.START_OF_DAY;
      /* Current start has to always be at an available spot. */ 
      if (!dayMinutes.get(currentStart).getMandatoryAvailability()) {
        currentStart = dayMinutes.get(currentStart).getNextAvailableMinute();
      }
      /* The last minute in the range must have this formula to have a range with a number
         of minutes equal to duratio, as the number of minutes between start and end is
         currentEnd - currentStart + 1= duration, because we want to include the  ends too. */
      int currentEnd = currentStart + duration - 1;
      /* This variabile keeps track if we have found other suitable ending since we started
         analyzing the current start, it it used when current end reaches a minute that is not
         available. */
      int prevEnd = -1;
      

      while (currentStart < TimeRange.END_OF_DAY) {
        if (currentEnd <= (TimeRange.END_OF_DAY + 1)) {
          /* If current end is good, try to expand the range. */
          if (dayMinutes.get(currentEnd).getMandatoryAvailability()) {
            /* Set previous ending to the last ending that fits and increment current ending. */
            prevEnd = currentEnd;
            currentEnd++;
          } else {
            /* If it does exist a previous suitable ending of the range starting from currentStart. */
            if (prevEnd != -1) {
              /* This means we found a range that is suitable, but can't expand it anymore,
                 so report it. */
              timeRanges.add(TimeRange.fromStartDuration(currentStart, currentEnd - currentStart));
              /* We will analyse a new starting point, so previous ending is not set. */
              prevEnd = -1;
            }
            /* Initialize the new range that we wish to analyze. */
            currentStart = dayMinutes.get(currentEnd).getNextAvailableMinute();
            currentEnd = currentStart + duration - 1;
          }
        } else {
          /* If the current ending exceeded the minutes of a day, but the previous ending
             was good, create a time range with the prev end. */
          if (currentEnd == (TimeRange.END_OF_DAY + 2) && prevEnd != -1) {
            timeRanges.add(TimeRange.fromStartDuration(currentStart, currentEnd - currentStart - 1));
          }
          break;
        }
      }

    return timeRanges;
  }

}

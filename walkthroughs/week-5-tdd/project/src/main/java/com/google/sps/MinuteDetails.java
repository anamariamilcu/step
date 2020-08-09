package com.google.sps;
  
  
/**
 * Gives details about what is happening in a minute of the day.
 */
public final class MinuteDetails {
  /* Is set true if all mandatory attendes are available in this minute. */
  private boolean mandatoryAvailability;
  /* The next minute in the day in which every mandatory person is free. */
  private int nextAvailableMinute;

  public MinuteDetails(boolean mandatoryAvailability, int nextAvailableMinute) {
    this.mandatoryAvailability = mandatoryAvailability;
    this.nextAvailableMinute = nextAvailableMinute;
  }

  public boolean getMandatoryAvailability() {
    return mandatoryAvailability;
  }

  public int getNextAvailableMinute() {
    return nextAvailableMinute;
  }

  public void setMandatoryAvailability(boolean mandatoryAvailability) {
    this.mandatoryAvailability = mandatoryAvailability;
  }

  public void setNextAvailableMinute(int nextAvailableMinute) {
    this.nextAvailableMinute = nextAvailableMinute;
  }

}
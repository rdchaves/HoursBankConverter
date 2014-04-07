package rodrigo.chaves.hoursbankconverter.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;

public class Day {

	private Date value;
	private List<Date> checkpoints = new ArrayList<Date>();

	public Day(Date value) {
		this.value = DateUtils.truncate(value, Calendar.DATE);
	}

	public Date getValue() {
		return value;
	}

	public void setValue(Date value) {
		this.value = value;
	}

	public List<Date> getCheckpoints() {
		return checkpoints;
	}

	public void setCheckpoints(List<Date> checkpoints) {
		this.checkpoints = checkpoints;
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return this.value.equals(((Day) obj).getValue());
	}

	@Override
	public String toString() {
		return this.value.toString();
	}
}
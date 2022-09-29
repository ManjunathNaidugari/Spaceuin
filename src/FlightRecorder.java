
public interface FlightRecorder {

	void recordArrival(Beacon beacon);

	void recordDeparture(Beacon beacon);

	FlightRecorder createCopy();

	void tellStory();
}

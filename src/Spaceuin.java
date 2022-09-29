
import java.util.*;

public class Spaceuin extends Thread {

    private Beacon start;
    private Beacon to;
    private Set<Beacon> visited;

    // For stopping all Threads at the end
    private Spaceuin parentThread;
    private List<Spaceuin> childThreads;

    // For telling the way afterward
    private FlightRecorder flightRecorder;

    // Standard constructor for first Pingu
    public Spaceuin(Beacon start, Beacon to, FlightRecorder flightRecorder) {
        this(start, to, flightRecorder, null);
    }

    // Constructor for every other Pingu
    public Spaceuin(Beacon start, Beacon to, FlightRecorder flightRecorder, Spaceuin parentThread) {
        this.start = start;
        this.to = to;
        this.flightRecorder = flightRecorder;
        visited = new HashSet<>();

        this.parentThread = parentThread;
        childThreads = new ArrayList<>();

    }

    @Override
    public void run() {
        search(start);
    }

    private void search(Beacon nextBeacon) {
        List<BeaconConnection> currConnections;
        BeaconConnection beaconConnection;
        Beacon helper;
        Beacon after = null;
        synchronized (nextBeacon) {
            if (isInterrupted()) {return;}
            flightRecorder.recordArrival(nextBeacon);
            if (nextBeacon == to) {
                markArrival(this);
                flightRecorder.tellStory();
                return;
            }
            currConnections = nextBeacon.connections();
            if (!visited.contains(nextBeacon)) {visited.add(nextBeacon);}
            for (BeaconConnection currConnection : currConnections) {
                beaconConnection = currConnection;
                helper = beaconConnection.beacon();

                if (!visited.contains(helper) && beaconConnection.type() == ConnectionType.WORMHOLE) {
                    synchronized (this) {
                        if (isInterrupted()) {
                            return;
                        }
                        Spaceuin newSpaceuin = new Spaceuin(helper, to, flightRecorder.createCopy(), this);
                        childThreads.add(newSpaceuin);
                        newSpaceuin.start();
                    }
                }
            }

            after = getBeacon(nextBeacon, currConnections, null);
        }
        while (after != null) {
            search(after);
            after = null;
            synchronized (nextBeacon) {
                if (isInterrupted()) {return;}
                flightRecorder.recordArrival(nextBeacon);
                after = getBeacon(nextBeacon, currConnections, null);
            }
        }


    }

    private Beacon getBeacon(Beacon next, List<BeaconConnection> currConnections, Beacon beacon) {
        BeaconConnection beaconConnection;
        Beacon helper;
        for (BeaconConnection currConnection : currConnections) {
            beaconConnection = currConnection;
            helper = beaconConnection.beacon();
            if (!visited.contains(helper) && beaconConnection.type() == ConnectionType.NORMAL) {
                beacon = helper;
                break;
            }
        }
        flightRecorder.recordDeparture(next);
        return beacon;
    }

    private synchronized void markArrival(Spaceuin spaceuin) {
        if (parentThread != null && parentThread != spaceuin) {
            parentThread.markArrival(this);
        }
        for (Spaceuin childThread : childThreads) {
            if (spaceuin != childThread) {
                childThread.markArrival(this);
            }
        }
        interrupt();
    }
}

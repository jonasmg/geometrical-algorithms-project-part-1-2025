// import java.io.*;
// import org.w3c.dom.events.Event;
import java.util.*;


class Point {
    double x, y;
    Point(double x, double y) { this.x = x; this.y = y; }
}

class Segment {
    Point a, b;
    Segment(Point a, Point b) { this.a = a; this.b = b; }
}

class Event implements Comparable<Event> {
    double angle;
    double distance;
    Segment seg;
    boolean start; // true = start point, false = end point

    Event(double angle, double distance, Segment seg, boolean start) {
        this.angle = angle;
        this.distance = distance;
        this.seg = seg;
        this.start = start;
    }

    @Override
    public int compareTo(Event other) {
        int cmp = Double.compare(this.angle, other.angle);
        if (cmp != 0) return cmp;

        // Same angle → start events come before end events
        if (this.start != other.start) {
            return this.start ? 1 : -1;
        }

        // Optional: tie-break by distance (closer first)
        return Double.compare(this.distance, other.distance);
    }
}

public class VisibleSegmentsAlgorithm {
    // Placeholder for the actual implementation of the visible segments algorithm
    public static List<Segment> computeVisibleSegments(Point p, List<Segment> segments) {
        // For all points in segments, compute angle from p, and save angle together with segment
        List<Event> eventQueue = new ArrayList<>();

        for (Segment s : segments) {
            double angleA = Math.toDegrees(Math.atan2(s.a.y - p.y, s.a.x - p.x));
            double angleB = Math.toDegrees(Math.atan2(s.b.y - p.y, s.b.x - p.x));

            if (angleA < 0) angleA += 360;
            if (angleB < 0) angleB += 360;

            double distanceA = Math.hypot(s.a.x - p.x, s.a.y - p.y);
            double distanceB = Math.hypot(s.b.x - p.x, s.b.y - p.y);

            // Case angle does not cross the 0 degree line
            if (Math.abs(angleA - angleB) <= 180) {
                if (angleA < angleB) {
                    eventQueue.add(new Event(angleA, distanceA, s, true));
                    eventQueue.add(new Event(angleB, distanceB, s, false));
                } else {
                    eventQueue.add(new Event(angleB, distanceB, s, true));
                    eventQueue.add(new Event(angleA, distanceA, s, false));
                }
            } else { // Case angle crosses the 0 degree line
                if (angleA < angleB) {
                    eventQueue.add(new Event(angleB, distanceB, s, false));        // end at high angle
                    eventQueue.add(new Event(0.0, distanceA, s, true));      // start again at 0
                    eventQueue.add(new Event(angleA, distanceA, s, true));         // original start
                    eventQueue.add(new Event(360.0, distanceB, s, false));   // end of circle
                } else {
                    eventQueue.add(new Event(angleA, distanceA, s, false));
                    eventQueue.add(new Event(0.0, distanceB, s, true));
                    eventQueue.add(new Event(angleB, distanceB, s, true));
                    eventQueue.add(new Event(360.0, distanceA, s, false));
                }
            }
        }

        // Sort all events by angle
        Collections.sort(eventQueue);

        // Initialize active segments list in BST
        TreeSet<Segment> activeSegments = new TreeSet<>(new Comparator<Segment>() {
            @Override
            public int compare(Segment s1, Segment s2) {
                // Compare segments based on their distance to point p
                double dist1 = Math.min(Math.hypot(s1.a.x - p.x, s1.a.y - p.y),
                                        Math.hypot(s1.b.x - p.x, s1.b.y - p.y));
                double dist2 = Math.min(Math.hypot(s2.a.x - p.x, s2.a.y - p.y),
                                        Math.hypot(s2.b.x - p.x, s2.b.y - p.y));
                return Double.compare(dist1, dist2);
            }
        });

        List<Segment> visibleSegments = new ArrayList<>();
        for (Event e : eventQueue) {
            if (e.start) {
                // Add segment to active list
                activeSegments.add(e.seg);
            } else {
                // Remove segment from active list
                activeSegments.remove(e.seg);
            }

            // The closest segment in the active list is visible
            if (!activeSegments.isEmpty()) {
                Segment closest = activeSegments.first();
                if (!visibleSegments.contains(closest)) {
                    visibleSegments.add(closest);
                }
            }
        }

        // Return visible segments
        return visibleSegments;

    }
}
import java.io.*;
import java.util.*;

import org.w3c.dom.events.Event;

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
    Segment seg;
    boolean start; // true = start point, false = end point

    Event(double angle, Segment seg, boolean start) {
        this.angle = angle;
        this.seg = seg;
        this.start = start;
    }

    @Override
    public int compareTo(Event other) {
        return Double.compare(this.angle, other.angle);
    }
}

public class VisibleSegmentsAlgorithm {
    // Placeholder for the actual implementation of the visible segments algorithm
    public static List<Segment> computeVisibleSegments(Point p, List<Segment> segments) {
        // For all points in segments, compute angle from p, and save angle together with segment
        List<Event> events = new ArrayList<>();

        for (Segment s : segments) {
            double angleA = Math.atan2(s.a.y - p.y, s.a.x - p.x);
            double angleB = Math.atan2(s.b.y - p.y, s.b.x - p.x);

            // Normalize to [0, 2π)
            if (angleA < 0) angleA += 2 * Math.PI;
            if (angleB < 0) angleB += 2 * Math.PI;

            // Ensure start < end
            if (angleB < angleA) {
                double tmp = angleA; angleA = angleB; angleB = tmp;
            }

            events.add(new Event(angleA, s, true));  // entering sweep
            events.add(new Event(angleB, s, false)); // leaving sweep
        }

        // Sort all events by angle
        Collections.sort(events);


        // Return empty list for now, should return all visible segments
        // Return simple segments for testing
        return List.of(
            new Segment(new Point(-0.52, 2.78), new Point(1.72, 0.72))
        );

    }
}
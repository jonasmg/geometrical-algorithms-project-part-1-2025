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

        // If same angle:
        if (this.start != other.start) {
            // Start events first
            return this.start ? 1 : -1;
        }

        // Both start or both end:
        // For start events → closer first
        // For end events → farther first
        if (this.start) {
            return Double.compare(this.distance, other.distance); // closer first
        } else {
            return Double.compare(other.distance, this.distance); // farther first
        }
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

            if (angleA < angleB) {
                if (angleB - angleA <= 180) {
                    // normal interval
                    eventQueue.add(new Event(angleA, distanceA, s, true));
                    eventQueue.add(new Event(angleB, distanceB, s, false));
                } else {
                    eventQueue.add(new Event(angleB, distanceB, s, true));
                    eventQueue.add(new Event(angleA, distanceA, s, false));
                }
            } else {
                if (angleA - angleB <= 180) {
                    // normal interval
                    eventQueue.add(new Event(angleB, distanceB, s, true));
                    eventQueue.add(new Event(angleA, distanceA, s, false));
                } else {
                    eventQueue.add(new Event(angleA, distanceA, s, true));
                    eventQueue.add(new Event(angleB, distanceB, s, false));
                }
            }
        }

        // Sort all events by angle
        Collections.sort(eventQueue);

        // Initialize active segments list in BST
        TreeSet<Segment> activeSegments = new TreeSet<>(new Comparator<Segment>() {
            @Override
            public int compare(Segment s1, Segment s2) {
                // Check if segment cut
                boolean s1Crosses = crossesZeroLineRight(p, s1);
                boolean s2Crosses = crossesZeroLineRight(p, s2);

                double dist1 = Math.min(Math.hypot(s1.a.x - p.x, s1.a.y - p.y),
                                        Math.hypot(s1.b.x - p.x, s1.b.y - p.y));
                double dist2 = Math.min(Math.hypot(s2.a.x - p.x, s2.a.y - p.y),
                                        Math.hypot(s2.b.x - p.x, s2.b.y - p.y));

                if (s1Crosses) {
                    System.out.println("Seg is crossing");
                    System.out.println("A: (" + s1.a.x + ", " + s1.a.y + "), B: (" + s1.b.x + ", " + s1.b.y + ")");
                    dist1 = Math.min(dist1, Math.abs(crossesZeroLineRightX(p, s1) - p.x));
                    // Print distance
                    System.out.println("Shortest distance to p on seg: " + dist1);
                }

                if (s2Crosses) {
                    System.out.println("Seg is crossing");
                    System.out.println("A: (" + s2.a.x + ", " + s2.a.y + "), B: (" + s2.b.x + ", " + s2.b.y + ")");
                    dist2 = Math.min(dist2, Math.abs(crossesZeroLineRightX(p, s2) - p.x));
                }

                // Compare segments based on their distance to point p
                return Double.compare(dist1, dist2);
            }
        });

        List<Segment> visibleSegments = new ArrayList<>();

        // start by adding all segments that cut through the initial ray at angle 0°
        for (Event e : eventQueue) {
            Segment s = e.seg;

            if (crossesZeroLineRight(p, s)) {
                activeSegments.add(s);
            }
        }

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

    public static boolean crossesZeroLineRight(Point p, Segment s) {
        // Check if segment crosses horizontal line y = p.y
        if ((s.a.y - p.y) * (s.b.y - p.y) <= 0) {
            double intersectX;
            if (s.a.y == s.b.y) {
                // Horizontal segment, check both endpoints
                intersectX = Math.max(s.a.x, s.b.x);
            } else {
                // Linear interpolation to find x where y = p.y
                intersectX = s.a.x + (s.b.x - s.a.x) * ((p.y - s.a.y) / (s.b.y - s.a.y));
            }

            // Return true if intersection is right of p
            return intersectX > p.x;
        }

        // Does not cross horizontal line at p.y
        return false;
    }

    public static double crossesZeroLineRightX(Point p, Segment s) {
        // Check if segment crosses horizontal line y = p.y
        if ((s.a.y - p.y) * (s.b.y - p.y) <= 0) {
            double intersectX;
            if (s.a.y == s.b.y) {
                // Horizontal segment, check both endpoints
                intersectX = Math.max(s.a.x, s.b.x);
            } else {
                // Linear interpolation to find x where y = p.y
                intersectX = s.a.x + (s.b.x - s.a.x) * ((p.y - s.a.y) / (s.b.y - s.a.y));
            }

            return intersectX;
        }

        // Does not cross horizontal line at p.y
        return 0.0;
    }

}
import java.io.*;
import java.util.*;

class Point {
    double x, y;
    Point(double x, double y) { this.x = x; this.y = y; }
}

class Segment {
    Point a, b;
    Segment(Point a, Point b) { this.a = a; this.b = b; }
}

public class FileReaderUtil {

    public static class Data {
        public final Point p;
        public final List<Segment> segments;
        public Data(Point p, List<Segment> segments) {
            this.p = p;
            this.segments = segments;
        }
    }

    public static Data readFile(String filePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new java.io.FileReader(filePath))) {
            String line = br.readLine();
            if (line == null) throw new IOException("File is empty");

            // First line is the special point p
            String[] tokens = line.trim().split("\\s+");
            if (tokens.length != 2) throw new IOException("First line must have 2 numbers");
            Point p = new Point(Double.parseDouble(tokens[0]), Double.parseDouble(tokens[1]));

            // Read remaining lines as segments
            List<Segment> segments = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                tokens = line.trim().split("\\s+");
                if (tokens.length != 4) throw new IOException("Segment line must have 4 numbers");
                double x1 = Double.parseDouble(tokens[0]);
                double y1 = Double.parseDouble(tokens[1]);
                double x2 = Double.parseDouble(tokens[2]);
                double y2 = Double.parseDouble(tokens[3]);
                segments.add(new Segment(new Point(x1, y1), new Point(x2, y2)));
            }

            return new Data(p, segments);
        }
    }

    // Example usage
    public static void main(String[] args) throws IOException {
        Data data = readFile("tests/test1.txt");
        System.out.println("Special point p: (" + data.p.x + ", " + data.p.y + ")");
        System.out.println("Segments:");
        for (Segment s : data.segments) {
            System.out.printf("(%.1f, %.1f) -> (%.1f, %.1f)%n", s.a.x, s.a.y, s.b.x, s.b.y);
        }
    }
}

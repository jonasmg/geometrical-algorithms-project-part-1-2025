import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        String[] testFiles = {
            "tests/test1.txt",
            "tests/test2.txt",
            "tests/test3.txt",
            "tests/test4.txt",
            "tests/test5.txt",
            "tests/test6.txt",
            "tests/test7.txt",
            "tests/test8.txt"
        };
        for (String testFile : testFiles) {
            System.out.println("Processing " + testFile);
            processTestFile(testFile);
            System.out.println();
        }
    }

    static void processTestFile(String filename) throws IOException, InterruptedException {

         // Read point and segments from file
        FileReaderUtil.Data data = FileReaderUtil.readFile(filename);

        // Special point
        Point p = data.p;

        // List of segments
        List<Segment> segments = data.segments;

        // Calculate visible segments
        List<Segment> visible = VisibleSegmentsAlgorithm.computeVisibleSegments(p, segments);

        // Determine obscured segments
        List<Segment> obscured = new ArrayList<>();
        for (Segment s : segments) {
            boolean isVisible = false;
            for (Segment v : visible) {
                if ((s.a.x == v.a.x && s.a.y == v.a.y && s.b.x == v.b.x && s.b.y == v.b.y) ||
                    (s.a.x == v.b.x && s.a.y == v.b.y && s.b.x == v.a.x && s.b.y == v.a.y)) {
                    isVisible = true;
                    break;
                }
            }
            if (!isVisible) {
                obscured.add(s);
            }
        }

        String outputTexFile = "output/out-" + filename.substring(filename.lastIndexOf("test"), filename.lastIndexOf(".txt")) + ".tex";
        writeTikZ(p, obscured, visible, outputTexFile);
        compilePDF(outputTexFile);
    }

    static void writeTikZ(Point p, List<Segment> obscured, List<Segment> visible, String filename) throws IOException {

        // Compute bounding box and collect points
        double minX = p.x, minY = p.y, maxX = p.x, maxY = p.y;
        Set<Point> points = new HashSet<>();
        points.add(p); // include the special point
        for (Segment s : obscured) {
            minX = Math.min(minX, Math.min(s.a.x, s.b.x));
            minY = Math.min(minY, Math.min(s.a.y, s.b.y));
            maxX = Math.max(maxX, Math.max(s.a.x, s.b.x));
            maxY = Math.max(maxY, Math.max(s.a.y, s.b.y));
            points.add(s.a);
            points.add(s.b);
        }

        for (Segment s : visible) {
            minX = Math.min(minX, Math.min(s.a.x, s.b.x));
            minY = Math.min(minY, Math.min(s.a.y, s.b.y));
            maxX = Math.max(maxX, Math.max(s.a.x, s.b.x));
            maxY = Math.max(maxY, Math.max(s.a.y, s.b.y));
            points.add(s.a);
            points.add(s.b);
        }

        double margin = 1.0;
        minX -= margin; minY -= margin;
        maxX += margin; maxY += margin;

        try (PrintWriter out = new PrintWriter(new FileWriter(filename))) {
            out.println("\\documentclass{standalone}");
            out.println("\\usepackage{tikz}");
            out.println("\\begin{document}");
            out.println("\\begin{tikzpicture}[scale=0.8]");

            // Grid
            out.printf("\\draw[step=1.0,gray,thin] (%.2f, %.2f) grid (%.2f, %.2f);\n", minX, minY, maxX, maxY);

            // Axes
            out.printf("\\draw[->] (%.2f,0) -- (%.2f,0) node[right]{x};\n", minX-0.5, maxX+0.5);
            out.printf("\\draw[->] (0,%.2f) -- (0,%.2f) node[right]{y};\n", minY-0.5, maxY+0.5);

            // Draw all obscured (gray thin)
            for (Segment s : obscured) {
                out.printf("\\draw[black, very thick, dotted] (%.2f,%.2f) -- (%.2f,%.2f);\n", s.a.x, s.a.y, s.b.x, s.b.y);
            }

            // Draw visible segments (red thick)
            for (Segment s : visible) {
                out.printf("\\draw[black, thick] (%.2f,%.2f) -- (%.2f,%.2f);\n", s.a.x, s.a.y, s.b.x, s.b.y);
            }

            // Draw points with coordinates
            for (Point pt : points) {
                out.printf("\\filldraw (%.2f,%.2f) circle [radius=0.1] node[above right]{\\tiny (%.1f, %.1f)};\n",
                        pt.x, pt.y, pt.x, pt.y);
            }

            // Highlight special point p by adding extra "p" above left
            out.printf("\\node[above left, tiny] at (%.2f, %.2f) {p};\n", p.x, p.y);

            out.println("\\end{tikzpicture}");
            out.println("\\end{document}");
        }
    }

    static void compilePDF(String texFile) throws IOException, InterruptedException {
        System.out.println("Compiling " + texFile + " to PDF...");
        ProcessBuilder pb = new ProcessBuilder(
            "pdflatex", 
            "-output-directory=output",  // specify output directory
            "-interaction=batchmode",  // suppress output log
            texFile
        );
        pb.inheritIO();  // still shows basic errors
        Process p = pb.start();
        int exitCode = p.waitFor();
        if (exitCode == 0) {
            System.out.println("PDF generated successfully: output/" + texFile.replace(".tex", ".pdf"));

            // Delete .aux and .log files
            new File(texFile.replace(".tex", ".aux")).delete();
            new File(texFile.replace(".tex", ".log")).delete();
        } else {
            System.out.println("Error during PDF compilation.");
            // Delete
            new File(texFile.replace(".tex", ".aux")).delete();
            new File(texFile.replace(".tex", ".log")).delete();
        }
    }
}

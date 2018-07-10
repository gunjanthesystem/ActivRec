package org.giscience.utils.geogrid.examples;

import org.giscience.utils.geogrid.grids.ISEA3H;
import org.giscience.utils.geogrid.cells.GridCellIDType;

import java.io.File;
import java.io.IOException;

/**
 * Run this class in order to compute the number of cells in a grid for a given resolution, and check for non-unique IDs
 * per face of the icosahedron.
 *
 * The output is written to result.txt, the result, to result.log.
 *
 * @author Franz-Benjamin Mocnik
 */
public class SaveGridToDisk {
    private static final String path = "data";
    private static final int resolutionMax = 14;

    public static void main(String[] args) throws Exception {
        if (!new File(SaveGridToDisk.path).exists()) new File(SaveGridToDisk.path).mkdirs();

        for (int r = 1; r <= resolutionMax; r++) {
            SaveGridToDisk._log("# resolution %d", r);

            long x = System.currentTimeMillis();

            ISEA3H g = new ISEA3H(r);
            g.cellIDs(String.format("%s/grid-%d", path, r), GridCellIDType.NON_ADAPTIVE);

            SaveGridToDisk._log("computation:  %d ms", System.currentTimeMillis() - x);
            x = System.currentTimeMillis();

            SaveGridToDisk._exec("echo \"# resolution %d\" >> result.txt", r);
            SaveGridToDisk._exec("sort --numeric-sort --merge --batch-size=100000 grid-%d.* --unique | wc -l >> result.txt", r);
            SaveGridToDisk._exec("for i in {0..19}; do sort --numeric-sort --merge --batch-size=100000 grid-%d.face$i.* | uniq -d |  wc -l; done >> result.txt", r);
            SaveGridToDisk._exec("rm grid-%d.*", r);

            SaveGridToDisk._log("evaluation:   %d ms", System.currentTimeMillis() - x);
        }
    }

    private static void _log(String message, Object... args) throws IOException, InterruptedException {
        System.out.format(message + "\n", args);
        SaveGridToDisk._exec("echo \"%s\" >> result.log", String.format(message, args));
    }

    private static void _exec(String cmd, Object... args) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", String.format(cmd, args));
        processBuilder.directory(new File(SaveGridToDisk.path));
        Process process = processBuilder.start();
        process.waitFor();
    }
}

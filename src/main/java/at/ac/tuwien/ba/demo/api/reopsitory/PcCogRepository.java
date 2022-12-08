package at.ac.tuwien.ba.demo.api.reopsitory;

import org.geotools.coverage.grid.GridCoverage2D;

import java.io.IOException;
import java.net.URL;

public interface PcCogRepository {

    /**
     * fetches a cloud optimized geotiff and returns a {@link GridCoverage2D}.
     * @param href the {@link URL} of the requested geotiff
     * @return a lazy loaded {@link GridCoverage2D}
     * @throws IOException if the coverage could not be fetched.
     */
    GridCoverage2D fetchCoverageFromUrl(URL href) throws IOException;

}

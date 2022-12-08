package at.ac.tuwien.ba.demo.api.endpoint.v1.mapper;

import at.ac.tuwien.ba.demo.api.exception.ServiceException;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
public class GeoTiffMapper {

    public byte[] coverageToBinary(GridCoverage2D coverage2D) throws ServiceException {

        var stream = new ByteArrayOutputStream();
        GeoTiffWriter writer = null;
        try {
            writer = new GeoTiffWriter(stream);
            writer.write(coverage2D, null);
        } catch (IOException e) {
            throw new ServiceException("could not export GeoTiff", e);
        } finally {
            if (writer != null){
                writer.dispose();
            }
        }

        return stream.toByteArray();
    }
}

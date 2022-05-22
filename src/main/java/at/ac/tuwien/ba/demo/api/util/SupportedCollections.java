package at.ac.tuwien.ba.demo.api.util;

import lombok.Getter;

import java.util.Map;
import java.util.Set;

@Getter
public enum SupportedCollections {

    // https://docs.sentinel-hub.com/api/latest/data/sentinel-2-l2a/
    SENTINEL_2_L2A(
            "sentinel-2-l2a",
            "B08",
            "B04",
            "visual",
            "SCL",
            Set.of(4, 5, 6, 7)
    );

    private final String collectionId;
    private final String nirBand;
    private final String redBand;
    private final String tciBand;
    private final String cloudBand;
    private final Set<Integer> cloudFreePixels;

    public static final Map<String, SupportedCollections> map =
            Map.of(SENTINEL_2_L2A.getCollectionId(), SENTINEL_2_L2A);

    SupportedCollections(
            String collectionId,
            String nirBand,
            String redBand,
            String tciBand,
            String cloudBand,
            Set<Integer> cloudFreePixels
    ){

        this.collectionId = collectionId;
        this.nirBand = nirBand;
        this.redBand = redBand;
        this.tciBand = tciBand;
        this.cloudBand = cloudBand;
        this.cloudFreePixels = cloudFreePixels;
    }

}

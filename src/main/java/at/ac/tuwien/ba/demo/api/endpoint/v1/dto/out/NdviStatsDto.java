package at.ac.tuwien.ba.demo.api.endpoint.v1.dto.out;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Setter
@Getter
@ToString
public class NdviStatsDto {

    private Float ndviAvg;
    private Float ndviMin;
    private Float ndviMax;

    private String itemId;

}
